package biz.kindler.rigi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import tylerjroach.com.eventsource_android.EventSourceHandler;
import tylerjroach.com.eventsource_android.MessageEvent;


//import tylerjroach.com.eventsource_android.EventSource;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 24.11.16.
 */

public class ItemManager2 extends BroadcastReceiver implements EventSourceHandler {

    private final static String 	        TAG = ItemManager.class.getSimpleName();

    public static  String                   ITEM_NAME                   = "item-name";
    public static  String                   ITEM_CMD                    = "item-cmd";
    public static  String                   VALUE                       = "value";
    public static  String                   ACTION_ITEM_READ            = "item-read-value";
    public static  String                   ACTION_ITEM_CMD             = "item-cmd";
    public static  String                   ACTION_SYSTEM               = "action-system";
    public static  String                   SSE_STATE                   = "sse-state";
    public static  String                   SSE_CONNECTED               = "sse-connected";
    public static  String                   SSE_DISCONNECTED            = "sse-disconnected";
    public static  String                   SSE_INFO                    = "sse-info";

    public static  String                   ACTION_HABITEMS_LOADED      =     "action-habitems-loaded";
    public static  String                   KEY_LIST_SIZE               =     "key-list-size";


    private Context                         mCtx;
    private String                          mHost;
    private boolean                         mSSEconnected;



   // private EventSource eventSource;


    public ItemManager2( Context ctx) {
        mCtx = ctx;
        mHost = Util.getHost( ctx);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_ITEM_READ);
        intentFilter.addAction(ACTION_ITEM_CMD);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        ctx.registerReceiver(this, intentFilter);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendSystemBroadcast( SSE_DISCONNECTED, mHost == null || mHost.length() == 0 ? "no openhab host in settings" : "not init");
               // sendItemLoadedBroadcast( 0);
            }
        }, 5000);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Broadcast
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void sendItemLoadedBroadcast( int listSize) {
        Intent bc = new Intent();
        bc.setAction( ACTION_HABITEMS_LOADED);
        bc.putExtra( KEY_LIST_SIZE, listSize);
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

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if( action.equals(ACTION_ITEM_READ)) {
            String itemName = intent.getStringExtra(ITEM_NAME);
            new ReadItemState().execute( itemName);
        }
        else if( action.equals(ACTION_ITEM_CMD)) {
            String itemName = intent.getStringExtra(ITEM_NAME);
            String cmd = intent.getStringExtra(ITEM_CMD);
            new ItemCmd().execute( new String[] {itemName, cmd});
        }
        else if( action.equals(Intent.ACTION_TIME_TICK)) {
           if( ! mSSEconnected && mHost != null && mHost.length() > 0)
               new SSESocket(this).execute();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // HTTP read state request
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class ReadItemState extends AsyncTask<String, Void, String> {

        private String mItemName;

        @Override
        protected String doInBackground(String... param) {
            mItemName = param[0];
            String url = mHost + "/rest/items/" + mItemName + "/state";
            String respValue = null;

            try {
                Log.d(TAG, " read itemValue request for item: " + mItemName +" [url: " + url + "]");
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setUseCaches(false);
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                InputStream in = new BufferedInputStream(conn.getInputStream());
                respValue = convertStreamToString(in);
            } catch( Exception ex) {
                Log.w(TAG, ex.getMessage());
            }

            return respValue;
        }

        @Override
        protected void onPostExecute( String itemValue) {
            if( itemValue != null)
                sendItemEventBroadcast(mItemName, itemValue);
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
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // HTTP send command to item
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class ItemCmd extends AsyncTask<String[], Void, Void> {

        @Override
        protected Void doInBackground(String[]... param) {
            String itemName = param[0][0];
            String itemCmd = param[0][1];
            String url = mHost + "/rest/items/" + itemName;

            try {
                URL urlObj = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
                conn.setUseCaches(false);
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "text/plain");
                conn.setRequestProperty("Content-Length", "" + itemCmd.getBytes().length);
                conn.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                writer.write( itemCmd);
                writer.close();

                int code = conn.getResponseCode();
                Log.d(TAG, "cmd itemValue request for item: " + itemName + " [cmd: " + itemCmd + ",url: " + url + "] OK: " + code);
            } catch (Exception ex) {
                Log.w(TAG, "cmd itemValue request for item: " + itemName + " [cmd: " + itemCmd + ",url: " + url + "] FAILED: exc:" + ex.getMessage());
            }

            return null;
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SSE Server Send Events
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class SSESocket extends AsyncTask<Void, Void, Void> {

        private EventSourceHandler mEventSourceHandler;

        public SSESocket( EventSourceHandler eventSourceHandler) {
            mEventSourceHandler = eventSourceHandler;
        }

        @Override
        protected Void doInBackground(Void... param) {
            String targetUrl = mHost + "/rest/events";
            new tylerjroach.com.eventsource_android.EventSource(targetUrl, mEventSourceHandler);
            return null;
        }
    }

    @Override
    public void onMessage(String event, MessageEvent message) throws Exception {
        JSONObject jObject = new JSONObject(message.data);
        String payload = jObject.getString("payload");
        String topic = jObject.getString("topic");
        String type = jObject.getString("type");

        Log.d(TAG, "topic: " + topic + ", payload: " + payload + ", type: " + type);

        String itemName = extractItemName( topic);
        Log.d(TAG, "extracted itemName: " + itemName);
        String itemValue = extractValue( payload);
        Log.d(TAG, "extracted itemValue: " + itemValue);

        sendItemEventBroadcast( itemName, itemValue);
    }

    @Override
    public void onConnect() {
        mSSEconnected = true;
        Log.d(TAG, "SSE Connected True");
        sendSystemBroadcast( SSE_CONNECTED, null);
        sendItemLoadedBroadcast( 0);
    }

    @Override
    public void onClosed(boolean willReconnect) {
        mSSEconnected = false;
        Log.d(TAG, "SSE Closed");
        sendSystemBroadcast( SSE_DISCONNECTED, "closed");
    }

    @Override
    public void onError(Throwable t) {
        mSSEconnected = false;
        Log.d(TAG, "SSE Error " + t.getMessage());
        String msg = t.getCause() != null && t.getCause().getMessage() != null ? t.getCause().getMessage() : t.getMessage();
        sendSystemBroadcast( SSE_DISCONNECTED, "error: " + msg);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // e.g.: smarthome/items/Garage_Licht/command | smarthome/items/ConditionToday/state | smarthome/items/WS_Regen/state | smarthome/items/CommonIdAfterTomorrow/statechanged
    private String extractItemName( String topic) {
        int startIdx = "smarthome/items/".length();
        int endIdx = topic.lastIndexOf( "/");
        return topic.substring( startIdx, endIdx);
    }

    protected void sendItemEventBroadcast( String action, String value) {
        Intent bc = new Intent();
        bc.setAction( action);
        bc.putExtra( VALUE, value);
        mCtx.sendBroadcast(bc);
    }

    protected void sendSystemBroadcast( String sseState, String addInfo) {
        Intent bc = new Intent();
        bc.setAction( ACTION_SYSTEM);
        bc.putExtra( SSE_STATE, sseState);
        bc.putExtra( SSE_INFO, addInfo == null ? "" : addInfo);
        mCtx.sendBroadcast(bc);
    }

    protected String extractValue( String payload) {
        if( payload != null) {
            payload = payload.replace("{", "");
            payload = payload.replace("}", "");
            payload = payload.replaceAll("\"", "");
            String[] keyValArr = payload.split(",");
            for (String keyVal : keyValArr) {
                String[] rowDataArr = keyVal.split(":");
                if (rowDataArr[0].contains("value"))
                    return rowDataArr[1];
            }
        }
        return null;
    }

}
