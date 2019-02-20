package biz.kindler.rigi.modul.vbl;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.ThreeLinesDataHolder;
import biz.kindler.rigi.modul.clock.TimeAndDateModel;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import biz.kindler.rigi.settings.PublicTransportPreferenceFragment;
import biz.kindler.rigi.settings.SoundPreferenceFragment2;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 28.11.16.
 */

public class VBLModel2 extends BaseModel implements Response.Listener<JSONObject>, Response.ErrorListener  {

    private final static String TAG = VBLModel.class.getSimpleName();

   // private static String               TRANSPORT_URL       = "http://transport.opendata.ch/v1/connections?from=008589655&to=008505000&limit=6&transportations[]=bus&fields[]=connections/from/departure&fields[]=connections/to/arrival";
    private static String               TRANSPORT_URL       = "https://free.viapi.ch/v1/connection?from=Buchrain,%20Dorf&to=Luzern,%20Bahnhof";
    private static String               SHOW_IN_LIST_TIME   = "06:00"; // todo: in settings
    // https://www.viadi-api.ch/docs/
    // curl -H "API-Key: XXXXXXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX" "http://free.viapi.ch/v1/connection?from=Buchrain,%20Dorf&to=Luzern,%20Bahnhof"
    // headers:
    // API-Key: XXXXXXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
    // Accept-Language: de

    private ThreeLinesDataHolder        mDataHolder;
    private RequestQueue                mVolleyRequestQueue;
    private ArrayList<Connection2>      mConnArrList;
    private SimpleDateFormat            mTimeFormatter;


    public VBLModel2( Context ctx) {
        super(ctx, MainActivity.VBL);

        mDataHolder = new VBLDataHolder();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(TimeAndDateModel.ACTION_DAY_SEGMENT);
        intentFilter.addAction(MainListAdapter.ACTION_CHANGED_IN_LIST_MODUL + MainActivity.VBL);
        ctx.registerReceiver(this, intentFilter);

        mVolleyRequestQueue = Volley.newRequestQueue(ctx);

        mConnArrList = new ArrayList<>();

        mTimeFormatter = new SimpleDateFormat("HH:mm");
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
    }

    @Override
    protected void initItems() throws Exception {}

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
            doUpdateFromWeb();
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
            doUpdateFromWeb();
    }

    private void doUpdateFromWeb() {
        String apiKey = getApiKey();
        if( apiKey != null && apiKey.length() > 0) {
            Log.i(TAG, "Public Transport apiKey: " + apiKey);
            //sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "transport.opendata.ch updating...", "[BEFORE UPDATE ConnArrList size=" + mConnArrList.size() + "]");
            Log.d(TAG, "free.viapi.ch updating... [BEFORE UPDATE ConnArrList size=" + mConnArrList.size() + "]");
            mVolleyRequestQueue.add(new JsonObjectRequest(Request.Method.GET, TRANSPORT_URL, null, this, this) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("API-Key", getApiKey());
                    params.put("Accept-Language", "de");
                    return params;
                }

                ;
            });
        } else {
            Log.w(TAG, "Public Transport NO apiKey");
            handleError( "no API key");
        }
    }

    private String getApiKey() {
        return getPrefs().getString( PublicTransportPreferenceFragment.API_KEY, "");
    }

    public static String getFromTitle( Context ctx) {
        String fromTxt = getPrefs(ctx).getString( PublicTransportPreferenceFragment.FROM_LOCATION, "?");
        int pos = fromTxt.indexOf( ",");
        return pos > 0 ? fromTxt.substring( 0, pos) : fromTxt;
    }

    public static String getToTitle( Context ctx) {
        String toTxt = getPrefs(ctx).getString( PublicTransportPreferenceFragment.TO_LOCATION, "?");
        int pos = toTxt.indexOf( ",");
        return pos > 0 ? toTxt.substring( 0, pos) : toTxt;
    }

    @Override
    public void onResponse(final JSONObject response) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArr = response.getJSONArray( "connections");
                    int arrSize = jsonArr.length();
                    mConnArrList.clear();
                    int idxCnt = 0;
                    for( int cnt=0; cnt<arrSize; cnt++) {
                        Connection2 conn = new Connection2(jsonArr.getJSONObject(cnt));
                        if( conn.getTransportType() == Connection2.BUS) {
                            mConnArrList.add(idxCnt, conn);
                            idxCnt++;
                        }
                    }

                    updateDataHolder( true);
                    sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "free.viapi.ch", "updated successfully");// response.toString().substring(0, 100) + "...");
                    Log.d(TAG, "free.viapi.ch updated [AFTER UPDATE ConnArrList size=" + mConnArrList.size() + "] jsonArr size: " + arrSize);
                } catch (Exception e) {
                    handleError( e.getMessage());
                }
            }
        };
        new Thread(runnable).start();
    }


    private void updateDataHolder( boolean sortList) {
        if( mConnArrList.size() > 0 && mConnArrList.get(0) != null && mConnArrList.get(0).isDepartureLessThan(0))
            mConnArrList.remove(0);

       // if( mConnArrList.size() <3 && isModulInList( MainActivity.VBL))
       //     doUpdateFromWeb();

        if (mDataHolder != null) {
            mDataHolder.setLine1( mConnArrList.size() > 0 ? getTextForConnection(mConnArrList.get(0)) : new String[] {"-", "",""});
            mDataHolder.setLine2( mConnArrList.size() > 1 ? getTextForConnection(mConnArrList.get(1)) : new String[] {"-", "",""});
            mDataHolder.setLine3( mConnArrList.size() > 2 ? getTextForConnection(mConnArrList.get(2)) : new String[] {"-", "",""});
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

        handleError( "free.viapi.ch FAILED: " + errMsg + " [ConnArrList size=" + mConnArrList.size() + "]");
    }

    private void handleError( String errorMsg) {
        if( mDataHolder != null) {
            mDataHolder.setLine2(new String[] {errorMsg, "", ""});
            sendUpdateListItemBroadcast();
        }
        Log.e( TAG, "handleError:" + errorMsg);
        sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "", errorMsg);
    }

    private String[] getTextForConnection( Connection2 conn) {
        return new String[]{getTextForDepartureInMinutes(conn.getDepartureInMinutes()), "Abfahrt " + conn.getDepartureClientFormat() + conn.getDelay( " +", true), "Ankunft " + conn.getArrivalClientFormat()};
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

}
