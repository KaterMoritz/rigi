package biz.kindler.rigi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import biz.kindler.rigi.modul.weather.WeatherView;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 25.11.16.
 */

public class HttpItem extends BroadcastReceiver {

    private final static String TAG = HttpItem.class.getSimpleName();

    public static String        ACTION_UPDATE_STATE         = "update-item-state";
    public static String        ACTION_UPDATE_STATE_WEATHER = "update-item-state-weather";
    public static String        KEY_ITEM_NAME               = "item-name";
    public static String        KEY_ITEM_STATE              = "item-state";
    public static String        KEY_TRACK_ID                = "track-id"; // sending back id from request to identify request (optional)

    public static String        UNKNOWN                     = "unknown";

    public static final int     CMD_ON_OFF                  = 1;
    public static final int     CMD_UP_DOWN_STOP            = 2;
    public static final int     REQUEST_FOR_STRING          = 0;
    public static final int     REQUEST_FOR_ON_OFF          = 1;
    public static final int     REQUEST_FOR_COND_TRANSL     = 2;
    public static final int     REQUEST_FOR_TEMPERATUR      = 3;
    public static final int     REQUEST_FOR_IMAGE           = 4;


    private static int          START_DELAY                 = 30000;

    protected String            name; // item name
    protected String            link;
    protected Context           mCtx;

    private String              mBroadcastAction = ACTION_UPDATE_STATE;
    private Timer               mUpdateTimer;
    private int                 mReqType;
    private int                 mAddReqType; // only for weather (translated) and weather image

    private RequestQueue        mVolleyRequestQueue;
    private SharedPreferences   mPrefs;

    private String              mAtmosphereTrackingId;


    public void setHttpConn( int requestType, int intervallMinutes) {
        mReqType = requestType;

        if( mVolleyRequestQueue == null && mCtx != null)
            mVolleyRequestQueue = Volley.newRequestQueue(mCtx);

        if( mUpdateTimer == null && intervallMinutes > 0) {
            mUpdateTimer = new Timer();
            mUpdateTimer.schedule( new UpdateTimerTask(null), START_DELAY, intervallMinutes * 60000);
        }
    }

    public void addAdditionalRequest( int requestType) {
        mAddReqType = requestType;
    }

    public void sendStatusRequestNow() {
        new UpdateTimerTask(null).run();
    }

    public void sendStatusRequestNow( String trackId) {
        new UpdateTimerTask(trackId).run();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // syncron request
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String sendStateRequestSyncron() {
            String url = link + (ItemManager.mHabVersion2 ? "/state" : "/state?type=json");
        try {
            HttpURLConnection conn = getConnObj(url, mAtmosphereTrackingId);
            InputStream in = new BufferedInputStream(conn.getInputStream());
            mAtmosphereTrackingId = conn.getHeaderField("X-Atmosphere-tracking-id");
            return convertStreamToString(in);
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(TAG, name + " exc:" + errMsg);
            sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), name, errMsg);
            return UNKNOWN;
        }
    }

    private HttpURLConnection getConnObj( String url, String trackingId) throws Exception {
        URL urlObj = new URL( url);
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
        conn.setUseCaches(false);
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(10000);
        conn.setRequestMethod("GET");
       // conn.setRequestProperty("Accept","application/json");
       // conn.setRequestProperty("X-Atmosphere-Framework","1.0");
      //  conn.setRequestProperty("X-Atmosphere-Transport", "streaming"); // "websocket" "long-polling" "streaming"
        if( trackingId != null)
            conn.setRequestProperty("X-Atmosphere-tracking-id", trackingId);
        return conn;
    }

    private String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null)
                sb.append(line).append('\n');
        } finally {
            is.close();
        }
        return sb.toString().trim();
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setBroadcastAction( String action) {
        mBroadcastAction = action;
    }

    private StringRequest getRequest(final String url, final int type, final String trackId) {
        return new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String respText = "";
                        if( type == REQUEST_FOR_ON_OFF || type == CMD_ON_OFF) {
                            if( response.equals( "ON") || response.equals( "OFF"))
                                respText = response;
                        }
                        else if (type == REQUEST_FOR_STRING)
                            respText = response;
                        else if (type == REQUEST_FOR_TEMPERATUR)
                            respText = response == null || response.length() == 0 ? "-°C" : getTempRound(response) + "°C";
                        else if (type == REQUEST_FOR_COND_TRANSL)
                            respText = response == null || response.length() == 0 ? "-" : getPrefs().getString( WeatherView.PREFS_PREFIX_WEATHER_TXT + response, "?");
                        else if (type == REQUEST_FOR_IMAGE)
                            respText = String.valueOf(getPrefs().getInt( WeatherView.PREFS_PREFIX_WEATHER_IMG + response, R.drawable.unknown));

                        if( respText.length() > 0)
                            sendBroadcastFromHttp( name, respText, trackId);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String respText = "";
                if (type == REQUEST_FOR_TEMPERATUR)
                    respText = "-°C";
                else if (type == REQUEST_FOR_COND_TRANSL)
                    respText = "-";
                else if (type == REQUEST_FOR_IMAGE)
                    respText = String.valueOf(R.drawable.unknown);

                sendBroadcastFromHttp( name, respText, trackId);
                String errMsg = "FAILED request: [url:" + url + "] " + (error == null || error.getMessage() == null ? "" : error.getMessage());
                Log.e(TAG, name + " err:" + errMsg);
                sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), name, errMsg);
            }
        });
    }

    protected SharedPreferences getPrefs() {
        if( mPrefs == null)
            mPrefs = mCtx.getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE);
        return mPrefs;
    }

    private String getTempRound(String temp) {
        try {
            Double d = Double.parseDouble(temp);
            int rounded = (int) Math.round(d);
            return String.valueOf(rounded);
        } catch (Exception ex) {
            Log.w(TAG, "getTempRound (" + temp + ") exc: " + ex.getMessage());
            return "";
        }
    }

    public void sendCmdOn() {
        sendCmdValue( "ON");
        // "http://172.16.0.187:8080/rest/items/Briefkasten"
        //http://192.168.178.36:8081/CMD?TV_Steckdose=ON
    }

    public void sendCmdOff() {
        sendCmdValue( "OFF");
    }

    public void sendCmdUp() {
        sendCmdValue( "UP");
    }

    public void sendCmdDown() {
        sendCmdValue( "DOWN");
    }
    public void sendCmdStop() {
        sendCmdValue( "STOP");
    }

    public void sendCmdValue( String value) {
        String cmdPath = link.replace( "rest/items/", "CMD?");
        String cmdUrl = cmdPath + "=" + value;

        if( mVolleyRequestQueue == null && mCtx != null)
            mVolleyRequestQueue = Volley.newRequestQueue(mCtx);

        mVolleyRequestQueue.add( getRequest( cmdUrl, CMD_ON_OFF, null));
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Broadcast
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected void sendBroadcastFromHttp( String itemName, String state, String trackId) {
        Intent bc = new Intent();
        bc.setAction( mBroadcastAction);
        bc.putExtra( KEY_ITEM_NAME, itemName);
        bc.putExtra( KEY_ITEM_STATE, state);
        bc.putExtra( KEY_TRACK_ID, trackId);
        if(mCtx == null)
            Log.w( TAG, "CommunicationItem: " + itemName + " - Context is null");
        else
            mCtx.sendBroadcast(bc);
    }

    protected void sendSystemBroadcast( String action, String className, String objectName, String message) {
        Intent bc = new Intent();
        bc.setAction( action);
        bc.putExtra( SystemModel.KEY_CLASS, className);
        bc.putExtra( SystemModel.KEY_OBJECT, objectName);
        bc.putExtra( SystemModel.KEY_MESSAGE, message);
        mCtx.sendBroadcast(bc);
    }

    protected void registerTick() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        mCtx.registerReceiver(this, intentFilter);
    }

    protected void unregisterTick() {
        mCtx.unregisterReceiver( this);
    }

    // for override
    protected void onTick() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        onTick();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Timertask
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class UpdateTimerTask extends TimerTask {
        private String mReqId;

        public UpdateTimerTask( String reqId) {
            mReqId = reqId;
        }

        public void run() {
            try {
                String url = link + "/state?type=json";
                mVolleyRequestQueue.add( getRequest( url, mReqType, mReqId));
                if( mAddReqType > 0)
                    mVolleyRequestQueue.add( getRequest( url, mAddReqType, mReqId));
            } catch (Exception e) {
                Log.w( TAG, "CommunicationItem: " + name + " exc: " + e.getMessage());
                sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), name, e.getMessage());
            }
        }
    }

}
