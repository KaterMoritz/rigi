package biz.kindler.rigi;

import biz.kindler.rigi.modul.system.Log;
import tylerjroach.com.eventsource_android.EventSource;
import tylerjroach.com.eventsource_android.EventSourceHandler;
import tylerjroach.com.eventsource_android.MessageEvent;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 03.07.17.
 */

public class SSEItem implements EventSourceHandler {

    private final static String 	TAG = SSEItem.class.getSimpleName();

    private EventSource eventSource;
    private String      mHost;


    public SSEItem( String host) {
        mHost = host;
    }

    public void connect() {
        /*
        eventSource = new EventSource.Builder(mHost + "/rest/items")
                .eventHandler(this)
                .headers(new ArrayMap<String, String>())
                .build();
        eventSource.connect(); */
    }

    @Override
    public void onConnect() {
        Log.d( TAG, "SSE onConnect");
    }

    @Override
    public void onMessage(String event, MessageEvent message) {
        Log.d( TAG, "SSE onMessage: " + event.toString());
        Log.d( TAG, "SSE onMessage lastEventId: " + message.lastEventId);
        Log.d( TAG, "SSE onMessage data: " +  message.data);
    }

   // @Override
    public void onComment(String comment) {
        //comments only received if exposeComments turned on
        Log.d(TAG, "SSE onComment: " + comment);
    }

    @Override
    public void onError(Throwable t) {
        //ignore ssl NPE on eventSource.close()
        Log.d(TAG, "SSE onError: " + t.getMessage());
    }

    @Override
    public void onClosed(boolean willReconnect) {
        Log.d(TAG, "SSE onClosed: reconnect? " + willReconnect);
    }


}

