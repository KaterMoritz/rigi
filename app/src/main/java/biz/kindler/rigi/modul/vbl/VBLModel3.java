package biz.kindler.rigi.modul.vbl;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;

//import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.ThreeLinesDataHolder;
import biz.kindler.rigi.modul.clock.TimeAndDateModel;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import biz.kindler.rigi.settings.PublicTransportPreferenceFragment;
import fr.arnaudguyon.xmltojsonlib.XmlToJson;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 28.11.16.
 */

public class VBLModel3 extends BaseModel implements Response.Listener<String>, Response.ErrorListener  {

    private final static String TAG = VBLModel.class.getSimpleName();

    // private static String               TRANSPORT_URL       = "http://transport.opendata.ch/v1/connections?from=008589655&to=008505000&limit=6&transportations[]=bus&fields[]=connections/from/departure&fields[]=connections/to/arrival";
    private static String               TRANSPORT_URL       = "https://api.opentransportdata.swiss/trias2020"; //https://free.viapi.ch/v1/connection?from=Buchrain,%20Dorf&to=Luzern,%20Bahnhof";
    private static String               SHOW_IN_LIST_TIME   = "05:00"; // todo: in settings
    // https://www.viadi-api.ch/docs/
    // curl -H "API-Key: XXXXXXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX" "http://free.viapi.ch/v1/connection?from=Buchrain,%20Dorf&to=Luzern,%20Bahnhof"
    // headers:
    // API-Key: XXXXXXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
    // Accept-Language: de

    private ThreeLinesDataHolder        mDataHolder;
    private RequestQueue                mVolleyRequestQueue;
    private ArrayList<Connection3>      mConnArrList;
    private SimpleDateFormat            mTimeFormatter;
    private String                      mRawRequestXML;
    private String                      mCurrRequestXML;
    private int                         mRequestCnt;
    private int                         mConnectionPointer;
    private String[]                    mFromLocationIds;
    private String[]                    mToLocationIds;



    public VBLModel3( Context ctx) {
        super(ctx, MainActivity.VBL);

        mDataHolder = new VBLDataHolder();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(TimeAndDateModel.ACTION_DAY_SEGMENT);
        intentFilter.addAction(MainListAdapter.ACTION_CHANGED_IN_LIST_MODUL + MainActivity.VBL);
        intentFilter.addAction(MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.VBL);
        ctx.registerReceiver(this, intentFilter);

        mVolleyRequestQueue = Volley.newRequestQueue(ctx);

        mConnArrList = new ArrayList<>();

        mTimeFormatter = new SimpleDateFormat("HH:mm");
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        storePrefsString(PublicTransportPreferenceFragment.TRANSPORT_REQUEST_SINCE, new SimpleDateFormat().format(new Date()));



        try {
            mFromLocationIds = getPrefs(ctx).getString( PublicTransportPreferenceFragment.FROM_LOCATION, "?").split(",");
            mToLocationIds = getPrefs(ctx).getString( PublicTransportPreferenceFragment.TO_LOCATION, "?").split(",");

            mRawRequestXML = getResourceAsString(R.raw.opentransportdata_request);
            mCurrRequestXML = String.format(mRawRequestXML, mFromLocationIds[mConnectionPointer], mToLocationIds[mConnectionPointer]);
        } catch (Exception ex) {
            Log.w(TAG, ex.getMessage());
        }

        initXMLParser();
     }

    @Override
    protected void initItems() throws Exception {}

    private GsonXml initXMLParser() {
        XmlParserCreator parserCreator = new XmlParserCreator() {
            @Override
            public XmlPullParser createParser() {
                try {
                    return XmlPullParserFactory.newInstance().newPullParser();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        return new GsonXmlBuilder().setXmlParserCreator(parserCreator).create();
    }

    public ThreeLinesDataHolder getDataHolder() {
        return mDataHolder;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIME_TICK))
            handleTimeTick();
        else if (action.equals(TimeAndDateModel.ACTION_DAY_SEGMENT)) {
            /* if( intent.getBooleanExtra(TimeAndDateModel.KEY_SUNRISE_TIME, false))
                handleSunrise();
            else */ if( intent.getBooleanExtra(TimeAndDateModel.KEY_LUNCH_TIME, false))
                handleLunchtime();
        }
        else if( action.equals( MainListAdapter.ACTION_CHANGED_IN_LIST_MODUL + MainActivity.VBL))
            handleShowInList( intent.getBooleanExtra( MainListAdapter.KEY_SHOW_IN_LIST, false));
        else if(action.equals( MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.VBL))
            handleOnPanelClick();
    }

    private void handleSunrise() {
        // todo && settings true for auto show/hide
        sendUpdateListItemBroadcast( true);
    }

    private void handleLunchtime() {
        // todo && settings true for auto show/hide
        sendUpdateListItemBroadcast( false);
    }

    private void handleShowInList( boolean showInList) {
        if( showInList && mConnArrList.size() < 4)
            doUpdateFromAPI();
    }

    private boolean isNowTime( String hh_mm) {
        String now = mTimeFormatter.format( new java.util.Date());
        return now.equals( hh_mm);
    }

    private void handleTimeTick() {
        updateDataHolder(false);
        if( isNowTime( SHOW_IN_LIST_TIME))
            sendUpdateListItemBroadcast( true);

        if( mConnArrList.size() <3 && isModulInList())
            doUpdateFromAPI();
    }

    private void handleOnPanelClick() {
        mConnectionPointer++;
        if( mConnectionPointer >= mFromLocationIds.length) {
            mConnectionPointer = 0;
        }

        mCurrRequestXML = String.format(mRawRequestXML, mFromLocationIds[mConnectionPointer], mToLocationIds[mConnectionPointer]);
        doUpdateFromAPI();
    }

    private void doUpdateFromAPI() {
        String apiKey = getApiKey();
        if( apiKey != null && apiKey.length() > 0) {
            Log.i(TAG, "Public Transport apiKey: " + apiKey);
            sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "opentransportdata.swiss updating...", "[BEFORE UPDATE ConnArrList size=" + mConnArrList.size() + "]");
            Log.d(TAG, "opentransportdata.swiss updating... [BEFORE UPDATE ConnArrList size=" + mConnArrList.size() + "]");
            incRequestCounter();
            mVolleyRequestQueue.add(new StringRequest(Request.Method.POST, TRANSPORT_URL, this, this) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", getApiKey());
                    params.put("Content-Type", "application/XML");
                    return params;
                }
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return  mCurrRequestXML.getBytes();
                }
            });
        } else {
            Log.w(TAG, "Public Transport NO apiKey");
            handleError( "no API key");
        }
    }

    private void incRequestCounter() {
        mRequestCnt++;
        storePrefsString(PublicTransportPreferenceFragment.TRANSPORT_REQUEST_CNT, String.valueOf(mRequestCnt));
    }

    private String getApiKey() {
        return getPrefs().getString( PublicTransportPreferenceFragment.API_KEY, "");
    }

    @Override
    public void onResponse(final String response) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                    try {
                        JSONArray tripResultArr = xmlToJSONArray( response);
                        Log.i(TAG,"tripResultArr:" + tripResultArr.length());
                        int arrSize = tripResultArr.length();
                        mConnArrList.clear();
                        int idxCnt = 0;
                        for( int cnt=0; cnt<arrSize; cnt++) {
                            JSONObject currTripObj = tripResultArr.getJSONObject(cnt).getJSONObject("trias:Trip");
                            Connection3 conn = new Connection3(currTripObj);
                            if( conn.isDirectConnection()) {
                                mConnArrList.add(idxCnt, conn);
                                idxCnt++;
                            }
                        }

                        updateDataHolder( true);
                        sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "opentransportdata.swiss", "updated successfully");// response.toString().substring(0, 100) + "...");
                        Log.d(TAG, "opentransportdata.swiss updated [AFTER UPDATE ConnArrList size=" + mConnArrList.size() + "] jsonArr size: " + arrSize);
                    } catch (Exception e) {
                        handleError( e.getMessage());
                    }
            }
        };
        new Thread(runnable).start();
    }

    private JSONArray xmlToJSONArray( String responseString) throws JSONException {
        XmlToJson xmlToJson = new XmlToJson.Builder(responseString).build();
        JSONObject rootObj = xmlToJson.toJson();
        JSONObject triasObj = rootObj.getJSONObject("trias:Trias");
        JSONObject serviceDeliveryObj = triasObj.getJSONObject("trias:ServiceDelivery");
        JSONObject deliveryPayloadObj = serviceDeliveryObj.getJSONObject("trias:DeliveryPayload");
        JSONObject tripResponseObj = deliveryPayloadObj.getJSONObject("trias:TripResponse");
        JSONArray tripResultArr = tripResponseObj.getJSONArray("trias:TripResult");
        return tripResultArr;
    }


    private void updateDataHolder( boolean sortList) {
        if( mConnArrList.size() > 0 && mConnArrList.get(0) != null && mConnArrList.get(0).isDepartureLessThan(0))
            mConnArrList.remove(0);

         //if( mConnArrList.size() <3 && isModulInList( MainActivity.VBL))
        //     doUpdateFromWeb();
        String transportTypTitle = "";

        if (mDataHolder != null) {
            if(mConnArrList.size() > 0) {
                Connection3 firstConn = mConnArrList.get(0);
                transportTypTitle = firstConn.getTransportType();
                mDataHolder.setTitle(firstConn.getTransportType() + " " + firstConn.getTransportLine() + ", " + firstConn.getDepartureStation() + " - " + firstConn.getArrivalStation());
            }
            mDataHolder.setLine1( mConnArrList.size() > 0 ? getTextForConnection(mConnArrList.get(0), transportTypTitle) : new String[] {"-", "",""});
            mDataHolder.setLine2( mConnArrList.size() > 1 ? getTextForConnection(mConnArrList.get(1), transportTypTitle) : new String[] {"-", "",""});
            mDataHolder.setLine3( mConnArrList.size() > 2 ? getTextForConnection(mConnArrList.get(2), transportTypTitle) : new String[] {"-", "",""});
            if( mConnArrList.size() > 0) {
                String transportType = mConnArrList.get(0).getTransportType();
                if( transportType != null)
                    mDataHolder.setImgResId( transportType.equals("Zug") ? R.drawable.sbb : R.drawable.vbl);
            }
        }
        sendUpdateListItemBroadcast();
    }

    private boolean isNowTimeForShowModul() {
        return true; // TODO
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        String errMsg = "-";
        if( error != null) {
            if( error.getMessage() != null)
                errMsg = error.getMessage();
            if( error.getCause() != null)
                errMsg = error.getCause().toString();
            else
                errMsg = error.toString();
        }

        handleError( "FAILED: " + errMsg + " [ConnArrList size=" + mConnArrList.size() + "]");
    }

    private void handleError( String errorMsg) {
        if( mDataHolder != null) {
            mDataHolder.setLine2(new String[] {errorMsg, "", ""});
            sendUpdateListItemBroadcast();
        }
        Log.e( TAG, "handleError:" + errorMsg);
        sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "", errorMsg);
    }

    private String[] getTextForConnection( Connection3 conn, String transportTypeTitle) {
        boolean showTransportTypInfo = conn.getTransportType() != null && ( ! conn.getTransportType().equalsIgnoreCase(transportTypeTitle));
        String[] dataArr = new String[3];
        dataArr[0] = getTextForDepartureInMinutes(conn.getDepartureInMinutes()) + " " + conn.getDelay() + (showTransportTypInfo ? " (" + conn.getTransportType() + ")" : "");
        dataArr[1] = "Abfahrt " + conn.getDepartureClientFormat();
        dataArr[2] = "Ankunft " + conn.getArrivalClientFormat();
        return dataArr;
    }

    private String getTextForDepartureInMinutes( int min) {
        if( min == 60)
            return "in 1 Stunde";
        else if( min > 60 && min < 120) {
            int minuten = min - 60;
            return "in 1 Stunde " + minuten + (minuten == 1 ? " Minute" : " Minuten");
        }
        else if( min >= 120 && min < 180)
            return "in 2 Stunden " + (min - 120) + " Minuten";
        else if( min >= 180 && min < 260)
            return "in 3 Stunden " + (min - 180) + " Minuten";
        else if( min > 1)
            return "in " + min + " Minuten";
        else if( min == 1)
            return "in " + min + " Minute";
        else if( min == 0)
            return "jetzt";
        else if( min < 0)
            return "verpasst";
        else
            return "error";
    }

    private String getResourceAsString(int resId) {
        try {
            Resources res = getContext().getResources();
            InputStream in_s = res.openRawResource(resId);

            byte[] b = new byte[in_s.available()];
            in_s.read(b);
            return new String(b);
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        }
    }

}

