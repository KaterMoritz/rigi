package biz.kindler.rigi.modul.vbl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.ThreeLinesDataHolder;
import biz.kindler.rigi.modul.clock.TimeAndDateModel;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 28.11.16.
 */

public class VBLModel extends BaseModel implements Response.Listener<JSONObject>, Response.ErrorListener  {

    private final static String TAG = VBLModel.class.getSimpleName();

    private static String               TRANSPORT_URL       = "http://transport.opendata.ch/v1/connections?from=008589655&to=008505000&limit=6&transportations[]=bus&fields[]=connections/from/departure&fields[]=connections/to/arrival";

    private ThreeLinesDataHolder        mDataHolder;
    private RequestQueue                mVolleyRequestQueue;
    private ArrayList<Connection>       mConnArrList;


    public VBLModel( Context ctx) {
        super(ctx, MainActivity.VBL);

        mDataHolder = new VBLDataHolder();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(TimeAndDateModel.ACTION_DAY_SEGMENT);
        ctx.registerReceiver(this, intentFilter);

        mVolleyRequestQueue = Volley.newRequestQueue(ctx);

        mConnArrList = new ArrayList<>();
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
            updateDataHolder(false);
        else if (action.equals(TimeAndDateModel.ACTION_DAY_SEGMENT)) {
            if( intent.getBooleanExtra(TimeAndDateModel.KEY_SUNRISE_TIME, false))
                handleSunrise();
            else if( intent.getBooleanExtra(TimeAndDateModel.KEY_LUNCH_TIME, false))
                handleLunchtime();
        }
    }

    private void handleSunrise() {
        // todo && settings true for auto show/hide
        sendUpdateListItemBroadcast( true);
    }

    private void handleLunchtime() {
        // todo && settings true for auto show/hide
        sendUpdateListItemBroadcast( false);
    }


    private void doUpdateFromWeb() {
        //sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "transport.opendata.ch updating...", "[BEFORE UPDATE ConnArrList size=" + mConnArrList.size() + "]");
        Log.d(TAG, "transport.opendata.ch updating... [BEFORE UPDATE ConnArrList size=" + mConnArrList.size() + "]");
        mVolleyRequestQueue.add( new JsonObjectRequest(Request.Method.GET, TRANSPORT_URL, null, this, this));
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

                    for( int cnt=0; cnt<arrSize; cnt++)
                        mConnArrList.add( cnt, new Connection(jsonArr.getJSONObject( cnt)));

                    updateDataHolder( true);
                    sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "transport.opendata.ch", "updated successfully");// response.toString().substring(0, 100) + "...");
                    Log.d(TAG, "transport.opendata.ch updated [AFTER UPDATE ConnArrList size=" + mConnArrList.size() + "] jsonArr size: " + arrSize);
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

        if( mConnArrList.size() <4 && isModulInList())
            doUpdateFromWeb();

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

        handleError( "transport.opendata.ch FAILED: " + errMsg + " [ConnArrList size=" + mConnArrList.size() + "]");
    }

    private void handleError( String errorMsg) {
        if( mDataHolder != null) {
            mDataHolder.setLine2(new String[] {errorMsg, "", ""});
            sendUpdateListItemBroadcast();
        }
        Log.e( TAG, "handleError:" + errorMsg);
        sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "", errorMsg);
    }

    private String[] getTextForConnection( Connection conn) {
        return new String[] {getTextForDepartureInMinutes( conn.getDepartureInMinutes()),  "Abfahrt " + conn.getDepartureClientFormat(), "Ankunft " + conn.getArrivalClientFormat()};
    }

    private String getTextForDepartureInMinutes( int min) {
        if( min == 60)
            return "in 1 Stunde";
        else if( min > 60 && min < 120)
            return "in 1 Stunde " + (min - 60) + " Minuten";
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
