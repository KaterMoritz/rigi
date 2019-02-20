package biz.kindler.rigi;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.OptionsBuilder;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.impl.DefaultOptions;
import org.atmosphere.wasync.impl.DefaultOptionsBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 24.11.16.
 */

public class WebSocketItem extends HttpItem {

    private final static String 	TAG = WebSocketItem.class.getSimpleName();

    private static int      BONG_INTERVALL      = 60000; /*180000*/; // ms // prevent websocket from disconnect, like a ping

    private boolean         mWSConn;
    private String          mWSStateUrl;

    private Client                                                  mClient;
    private OptionsBuilder<DefaultOptions, DefaultOptionsBuilder>   mCientOptions;
    private RequestBuilder                                          mRequest;
    private org.atmosphere.wasync.Socket                            mWebSocket;
    private Timer                                                   mBongTimer;  // prevent websocket from disconnect, like a ping

    private boolean         mNotiONEvent;
    private boolean         mNotiOFFEvent;
    private boolean         mNotiConnStatusEvent;
    private boolean         mKeepConnection;
    private boolean         mIsConnected;
   // private boolean         mNotiExceptions;


    public void setOnNotification( boolean whenOn) {
        mNotiONEvent = whenOn;
    }

    public void setOffNotification( boolean whenOff) {
        mNotiOFFEvent = whenOff;
    }

    public void setSendConnStatusEvents( boolean status) {
        mNotiConnStatusEvent = status;
    }

    /*3public void setSendExceptionEvents( boolean status) {
        mNotiExceptions = status;
    } */


    public void setContext(Context ctx) {
        mCtx = ctx;
    }

    public void setWebSocketConn( boolean status, boolean keepConnection) {

        new WebSocket().execute(true, status, keepConnection);

        /*
        mWSConn = status;
        mKeepConnection = keepConnection;
        Log.d(TAG, "ItemName: " + name + ", setWebSocketConn: [status=" + status + ",keepConnection=" + keepConnection + "]");
        if( status) {

            if( mKeepConnection && mBongTimer == null) {
                mBongTimer = new Timer();
                mBongTimer.schedule( new BongTimerTask(), BONG_INTERVALL, BONG_INTERVALL);
            }

            if (mWSStateUrl == null) {
                mWSStateUrl = link.replace("http", "ws") + "/state" + (ItemManager.mHabVersion2 ? "" : "?Accept=application/json");
                Log.d(TAG, "ItemName: " + name + ", mWSStateUrl: " + mWSStateUrl);
            }

            if (mClient == null)
                mClient = ClientFactory.getDefault().newClient();

            if (mCientOptions == null)
                mCientOptions = mClient.newOptionsBuilder().reconnect(true).reconnectAttempts(1440).pauseBeforeReconnectInSeconds(60);  // 1440 try reconnect 1 day every minute

            if (mRequest == null)
                mRequest = mClient.newRequestBuilder().method(org.atmosphere.wasync.Request.METHOD.GET).uri(mWSStateUrl)
                        .encoder(new Encoder<String, Reader>() {        // Stream the request body
                            @Override
                            public Reader encode(String s) {
                                Log.d(TAG, "ItemName: " + name + ", encode: " + s);
                                return new StringReader(s);
                            }
                        })
                        .decoder(new Decoder<String, Reader>() {
                            @Override
                            public Reader decode(Event type, String s) {
                                handleMessage(s);
                                Log.d(TAG, "ItemName: " + name + ",type: " + type.name() + ", decode: " + s);
                                return new StringReader(s);
                            }
                        })
                        .transport(org.atmosphere.wasync.Request.TRANSPORT.WEBSOCKET)                        // Try WebSocket
                        .transport(org.atmosphere.wasync.Request.TRANSPORT.LONG_POLLING);                    // Fallback to Long-Polling

            if (mWebSocket == null)
                mWebSocket = mClient.create(mCientOptions.build());

            new WebSocket().execute();
           // connectWebSocket();
        } else
            mWebSocket.close();
*/
    }

    private void initWebSocketConn( boolean status, boolean keepConnection) {
        mWSConn = status;
        mKeepConnection = keepConnection;
        Log.d(TAG, "ItemName: " + name + ", setWebSocketConn: [status=" + status + ",keepConnection=" + keepConnection + "]");
        if( status) {

            if( mKeepConnection && mBongTimer == null) {
                mBongTimer = new Timer();
                mBongTimer.schedule( new BongTimerTask(), BONG_INTERVALL, BONG_INTERVALL);
            }

            if (mWSStateUrl == null) {
                mWSStateUrl = link.replace("http", "ws") + "/state" + (ItemManager.mHabVersion2 ? "" : "?Accept=application/json");
                Log.d(TAG, "ItemName: " + name + ", mWSStateUrl: " + mWSStateUrl);
            }

            if (mClient == null)
                mClient = ClientFactory.getDefault().newClient();

            if (mCientOptions == null)
                mCientOptions = mClient.newOptionsBuilder().reconnect(true).reconnectAttempts(1440).pauseBeforeReconnectInSeconds(60);  // 1440 try reconnect 1 day every minute

            if (mRequest == null)
                mRequest = mClient.newRequestBuilder().method(org.atmosphere.wasync.Request.METHOD.GET).uri(mWSStateUrl)
                        .encoder(new Encoder<String, Reader>() {        // Stream the request body
                            @Override
                            public Reader encode(String s) {
                                Log.d(TAG, "ItemName: " + name + ", encode: " + s);
                                return new StringReader(s);
                            }
                        })
                        .decoder(new Decoder<String, Reader>() {
                            @Override
                            public Reader decode(Event type, String s) {
                                handleMessage(s);
                                Log.d(TAG, "ItemName: " + name + ",type: " + type.name() + ", decode: " + s);
                                return new StringReader(s);
                            }
                        })
                        .transport(org.atmosphere.wasync.Request.TRANSPORT.WEBSOCKET)  ;                      // Try WebSocket
                        //.transport(org.atmosphere.wasync.Request.TRANSPORT.LONG_POLLING);                    // Fallback to Long-Polling

            if (mWebSocket == null)
                mWebSocket = mClient.create(mCientOptions.build());

           // new WebSocket().execute();
            // connectWebSocket();
        } else
            mWebSocket.close();

    }

    private void connectWebSocket() {
        try {
            mWebSocket.on(Event.CLOSE.name(), new Function<String>() {
                @Override
                public void on(String t) {
                    mIsConnected = false;
                    Log.d(TAG, "ItemName: " + name + ",connectWebSocket,Event.CLOSE: [on:" + t + "]");
                    if( mNotiConnStatusEvent)
                        sendBroadcast( ACTION_UPDATE_STATE, name, Event.CLOSE.name());
                }
            }).on(Event.REOPENED.name(), new Function<String>() {
                @Override
                public void on(String t) {
                    Log.d(TAG, "ItemName: " + name + ",connectWebSocket,Event.REOPENED: [on:" + t + "]");
                    if( mNotiConnStatusEvent)
                        sendBroadcast( ACTION_UPDATE_STATE, name, Event.REOPENED.name());
                    sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), name, "REOPENED");
                }
            }).on(new Function<IOException>() {
                @Override
                public void on(IOException ioe) {
                    Log.d(TAG, "ItemName: " + name + ",IOException: [on:" + ioe.getMessage() + ",cause:" + ioe.getCause().toString() + "]");
                    sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), name, ioe.getMessage());
                }
            }).on(Event.OPEN.name(), new Function<String>() {
                @Override
                public void on(String t) {
                    mIsConnected = true;
                    Log.d(TAG, "ItemName: " + name + ",Event.OPEN: [on:" + t + "]");
                    if( mNotiConnStatusEvent)
                        sendBroadcast( ACTION_UPDATE_STATE, name, Event.OPEN.name());
                }
            }).on(Event.MESSAGE.name(), new Function<String>() {
                @Override
                public void on(final String t) {
                    Log.d(TAG, "ItemName: " + name + ",Event.MESSAGE: [on:" + t + "]");
                }
            }).on(new Function<Throwable>() {
                @Override
                public void on(Throwable t) {
                    Log.d(TAG, "ItemName: " + name + ",Function.Throwable: [on:" + t + ",cause:" + t.getCause().toString() + "]");
                    sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), name, t.getMessage());
                }
            }).open(mRequest.build());

        } catch (IOException e) {
            String excMsg = e.getMessage();
            Log.w( TAG, "ItemName: " + name + ",connectWebSocket item:" + name + " [exc:" + excMsg + ",cause:" + e.getCause().toString() + "]");
            sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), name, excMsg);
        }
    }

    private void handleMessage( String msg) {
        if( mNotiONEvent && msg.equals( "ON")) {
            Log.d( TAG, "ItemName: " + name + ",handleMessage ON");
            sendBroadcast(ACTION_UPDATE_STATE, name, msg);
        }
        else if( mNotiOFFEvent && msg.equals( "OFF")) {
            Log.d( TAG, "ItemName: " + name + ",handleMessage OFF");
            sendBroadcast(ACTION_UPDATE_STATE, name, msg);
        }
    }

    protected void sendBroadcast( String action, String itemName, String state) {
        Intent bc = new Intent();
        bc.setAction( action);
        bc.putExtra( KEY_ITEM_NAME, itemName);
        bc.putExtra( KEY_ITEM_STATE, state);
        if(mCtx == null)
            Log.w( TAG, "CommunicationItem: " + itemName + " - Context is null");
        else
            mCtx.sendBroadcast(bc);
    }

    public String getWebSocketUrl() {
        return mWSStateUrl;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Timertask
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class BongTimerTask extends TimerTask {

        public void run() {
            if( mWebSocket != null) {
                try {
                    mWebSocket.fire("bong");
                } catch (IOException e) {
                    mIsConnected = false;
                    Log.w( TAG, "CommunicationItem: " + name + " [failed to bong]");
                    sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "failed to bong: " + name, e.getMessage());
                }

                if( mKeepConnection && ! mIsConnected)
                    new WebSocket().execute(false, true, mKeepConnection);
                    //connectWebSocket();
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // WebSocket AsyncTask
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class WebSocket extends AsyncTask<Boolean, Void, Void> {
        boolean doInitWebSocket;

        @Override
        protected Void doInBackground(Boolean... param) { // param[0] = init Socket or only reconnect | param[1] = status | param[2] = keepConnection
            doInitWebSocket = param[0];
            boolean status = param[1]; // do connect or disconnect
            boolean keepConnection = param[2]; // do bong when true

            if( doInitWebSocket)
                initWebSocketConn( status, keepConnection);
            return null;
        }

        @Override
        protected void onPostExecute( Void param) {
            connectWebSocket();
        }
    }

}
