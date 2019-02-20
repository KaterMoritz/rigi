package biz.kindler.rigi;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import tylerjroach.com.eventsource_android.EventSourceHandler;
import tylerjroach.com.eventsource_android.MessageEvent;

//import org.kaazing.net.sse.SseEventReader;
//import org.kaazing.net.sse.SseEventSource;
//import org.kaazing.net.sse.SseEventSourceFactory;
//import org.kaazing.net.sse.SseEventType;

//import tylerjroach.com.eventsource_android.EventSource;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 24.11.16.
 */

public class ItemManager implements EventSourceHandler {


    public static  String                   ACTION_HABITEMS_LOADED      =     "action-habitems-loaded";
    public static  String                   KEY_LIST_SIZE               =     "key-list-size";


    private final static String 	        TAG = ItemManager.class.getSimpleName();

    private static String                   ALL_ITEMS_PATH  = "/rest/items?type=json";
    private Hashtable<String, OpenHABItem>  mItemTable;
    private RequestQueue                    mVolleyRequestQueue;
    private Context                         mCtx;
    private String                          mHost;
    protected static final boolean          mHabVersion2 = true;


   // private EventSource eventSource;


    public ItemManager( Context ctx) {
        mCtx = ctx;
        mHost = Util.getHost( ctx);
        mItemTable = new Hashtable<>();
        mVolleyRequestQueue = Volley.newRequestQueue(ctx);


       // new Handler(Looper.getMainLooper()).post(new Runnable() {
         //   @Override
        //    public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
               // testSSE();
               // test();
               // test3();
                //test4();
        //    }
       // });

        new SSESocket().execute(false, true, true);
    }

    public void loadItemsFromServer() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                mVolleyRequestQueue.add(getRequestForAllItems());
            }
        });
    }

    public int getItemCount() {
        return mItemTable.size();
    }

    public OpenHABItem getItem( String itemName) {
        return mItemTable.get(itemName);
    }


    public void sendItemSwitchCmdOn( String itemName) {
        mItemTable.get(itemName).sendCmdOn();
    }

    public void sendItemSwitchCmdOff( String itemName) {
        mItemTable.get(itemName).sendCmdOff();
    }

    private JsonRequest getRequestForAllItems() {
        if( mHabVersion2)
            return getRequestHab2(mHost + "/rest/items");
        else
            return getRequestHab1(mHost + ALL_ITEMS_PATH);
    }

    private JsonObjectRequest getRequestHab1(final String url) {
        return new JsonObjectRequest(Request.Method.GET, url, null, new Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObj) {
                try {
                    JSONArray jsonArr = jsonObj.getJSONArray( "item");
                    for (int i = 0; i < jsonArr.length(); i++) {
                        JSONObject row = jsonArr.getJSONObject(i);
                        String itemName = row.getString("name");
                        mItemTable.put( itemName, new OpenHABItem(row));
                    }
                    sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "habItems", "habItems size: " + mItemTable.size());
                    sendItemLoadedBroadcast( mItemTable.size());
                } catch (JSONException e) {
                    String message = e.getMessage();
                    Log.e(TAG, message);
                    sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "habItems", message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = "FAILED request: [url:" + url + "] " + (error == null || error.getMessage() == null ? "" : error.getMessage());
                Log.e(TAG, message);
                sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "habItems", message);
            }
        });
    }

    private JsonArrayRequest getRequestHab2(final String url) {
        return new JsonArrayRequest(Request.Method.GET, url, null, new Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArr) {
                try {
                    //JSONArray jsonArr = jsonObj.getJSONArray( "items");
                    for (int i = 0; i < jsonArr.length(); i++) {
                        JSONObject row = jsonArr.getJSONObject(i);
                        String itemName = row.getString("name");
                        mItemTable.put( itemName, new OpenHABItem(row));
                    }
                    sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "habItems", "habItems size: " + mItemTable.size());
                    sendItemLoadedBroadcast( mItemTable.size());
                } catch (JSONException e) {
                    String message = e.getMessage();
                    Log.e(TAG, message);
                    sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "habItems", message);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = "FAILED request: [url:" + url + "] " + (error == null || error.getMessage() == null ? "" : error.getMessage());
                Log.e(TAG, message);
                sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "habItems", message);
            }
        });
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

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // testSSE
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void testSSE() {
        try {
            //String targetUrl = mHost + "/rest/events";//items/TemperatureToday/state";
            //String targetUrl = mHost + "/rest/items/Garage_Licht/state";
            //String targetUrl = mHost + "/rest/events?topics=smarthome/items/Garage_Licht/state";
            String targetUrl = "http://www.w3schools.com/html/demo_sse.php";

           // eventSource = new EventSource( targetUrl, this);
            //eventSource = new EventSource(Executors.newCachedThreadPool(), 100, URI.create( targetUrl), this, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnect() throws Exception {
        Log.d(TAG, "onConnect");
    }

    @Override
    public void onMessage(String event, MessageEvent message) throws Exception {
        Log.d(TAG, "onMessage: [event:" + event + ",message:" + message.toString() + "]");
    }

    @Override
    public void onError(Throwable t) {
        Log.d(TAG, "onError: " + t.getMessage());
    }

    @Override
    public void onClosed(boolean willReconnect) {
        Log.d(TAG, "onClosed: [willReconnect:" + willReconnect + "]");
    }

    ///////////////////////////////////////////////////////////////////////////
    private void test() {
        try {
            URL url = new URL( mHost + "/rest/items");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            Log.d("SSE", "http response: " + urlConnection.getResponseCode());

            //Object inputStream = urlConnection.getContent();
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            Log.d("SSE reading stream", readStrem(inputStream)+"");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("SSE activity", "Error on url openConnection: "+e.getMessage());
            e.printStackTrace();
        }

    }

    private String readStrem(InputStream inputStream) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try{
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while((line = reader.readLine()) != null){
                Log.d("ServerSentEvents", "SSE event: "+line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(reader != null){
                try{
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

    /////////////////////////////////////////////////////////
    private void test3() {
        //String targetUrl = "http://www.w3schools.com/html/demo_sse.php";
        String targetUrl = mHost + "/rest/items";
/*
        Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();
        WebTarget target = client.target(targetUrl);
        eventSource = EventSource.target(target).build();
        eventSource.register(new EventListener() {
            @Override
            public void onEvent(InboundEvent inboundEvent) {
                Log.d(TAG, "inboundEvent:" + inboundEvent.toString());
            }
        });
        eventSource.open(); */
    }

    private void test4() {
        /*
        try {

            //String targetUrl = "http://www.w3schools.com/html/demo_sse.php";
           // String targetUrl = mHost + "/rest/items";
            String targetUrl = mHost + "/rest/events";


            final SseEventSourceFactory sseEventSourceFactory = SseEventSourceFactory.createEventSourceFactory();
            final SseEventSource sseEventSource = sseEventSourceFactory.createEventSource(new URI( targetUrl));
            sseEventSource.connect();

            final SseEventReader sseEventReader = sseEventSource.getEventReader();

            SseEventType type = sseEventReader.next();
            while (type != SseEventType.EOS) {
                Log.d(TAG, "new event");
                if (type != null && type.equals(SseEventType.DATA)) {
                    CharSequence data = sseEventReader.getData();
                    Log.d(TAG, data.toString());
                } else {
                    Log.d(TAG, "type null or not data: " + type);
                }
                type = sseEventReader.next();
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }


    public class SSESocket extends AsyncTask<Boolean, Void, Void> {
        boolean doInitWebSocket;

        @Override
        protected Void doInBackground(Boolean... param) { // param[0] = init Socket or only reconnect | param[1] = status | param[2] = keepConnection
           // doInitWebSocket = param[0];
          //  boolean status = param[1]; // do connect or disconnect
            //boolean keepConnection = param[2]; // do bong when true
            //test4();
           // if( doInitWebSocket)
            //    initWebSocketConn( status, keepConnection);
            String targetUrl = mHost + "/rest/events";
           // String targetUrl = mHost + "/rest/events?topics=smarthome/items/Garage_Licht/state";
           // String targetUrl = "http://www.w3schools.com/html/demo_sse.php";

            tylerjroach.com.eventsource_android.EventSource eventSource = new tylerjroach.com.eventsource_android.EventSource(targetUrl, new SSEHandler());
            //eventSource.connect();
           // EventSource eventSource = new EventSource(Executors.newCachedThreadPool(), 100, URI.create( targetUrl), this, null);
            return null;
        }

        @Override
        protected void onPostExecute( Void param) {
            //connectWebSocket();
        }
    }


    private class SSEHandler implements EventSourceHandler {

        public SSEHandler() {
        }

        @Override
        public void onConnect() {
            Log.d(TAG, "SSE Connected True");
        }

        @Override
        public void onMessage(String event, MessageEvent message) throws Exception {
            Log.d(TAG, "SSE Message" + event);
            Log.d(TAG, "SSE Message: " + message.lastEventId);
            Log.d(TAG, "SSE Message: " + message.data);
        }
/*
        @Override
        public void onMessage(String event, com.tylerjroach.eventsource.MessageEvent message) throws Exception {
            Log.d(TAG, "SSE Message" + event);
            Log.d(TAG, "SSE Message: " + message.lastEventId);
            Log.d(TAG, "SSE Message: " + message.data);
        }

        @Override
        public void onComment(String comment) {
            //comments only received if exposeComments turned on
            Log.d(TAG, "SSE Comment " + comment);
        } */

        @Override
        public void onError(Throwable t) {
            //ignore ssl NPE on eventSource.close()
            Log.d(TAG, "SSE Error " + t.getMessage());
        }

        @Override
        public void onClosed(boolean willReconnect) {
            Log.d(TAG, "SSE Closed reconnect? " + willReconnect);
        }
    }

}
