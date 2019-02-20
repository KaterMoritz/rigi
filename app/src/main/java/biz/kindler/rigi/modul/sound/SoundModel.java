package biz.kindler.rigi.modul.sound;


import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.OneButtonDataHolder;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.settings.SoundPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 15.08.17.
 */

public class SoundModel extends BaseModel implements BluetoothProfile.ServiceListener, Runnable {

    public final static int  DETAIL_ACTIVITY_RESULT_CODE = 299;

    private final static String TAG = SoundModel.class.getSimpleName();

    private BluetoothAdapter mBtAdapter;
    private BluetoothA2dp mA2dpService;

    private AudioManager mAudioManager;
    private MediaPlayer mPlayer;
    private boolean mIsA2dpReady = false;
    private boolean mPlaying;


    // Button Text
    private static final String  PLAY    = "Play";
    private static final String  STOP    = "Stop";
    // hab item
    private static final String  MEDIA_PLUG_SWITCH  = "TV_Steckdose";
    private static final String  ONKYO_POWER        = "OnkyoPower"; // to switch on from standby
    private static final String  ONKYO_SOURCE_AUX   = "OnkyoSourceAux"; // ON set to AUX
    private static final String  STREAM_DATA_SOURCE = "StreamDataSource";

    private static final String  DEFAULT_STATION_ID             = "1702206";
    private static final String  DEFAULT_STATION_NAME           = "1.FM - Chillout Lounge (www.1.fm)";
    private static final String  DEFAULT_STATION_LOGO           = "http://i.radionomy.com/document/radios/4/4bfa/4bfa5a33-ef4d-4caa-b0cd-99be7cb93aee.jpg";
    private static final String  DEFAULT_STATION_DATA_SOURCE    = "http://185.33.21.112:80/chilloutlounge_128";


    private OneButtonDataHolder mDataHolder;
    private int                 mIdleTimeCnt;
    private boolean             mRequestOffWithDelay;
    private String              mStationName;
    private String              mStationId;
    private static final int    IDLE_TIME    = 10; // 10 timeticks (10 minutes)

    public SoundModel(Context ctx) {
        super(ctx, MainActivity.SOUND);

        mDataHolder = new SoundDataHolder();

        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.SOUND);
        intentFilter.addAction(MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.SOUND);
        intentFilter.addAction(MainActivity.ACTION_ACTIVITY_RESULT + MainActivity.SOUND);
        intentFilter.addAction(MainListAdapter.ACTION_CHANGED_IN_LIST_MODUL + MainActivity.SOUND);
        intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(MEDIA_PLUG_SWITCH);
        ctx.registerReceiver(this, intentFilter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if( mBtAdapter == null) {
            mDataHolder.setInfo("no bluetooth adapter");
            sendUpdateListItemBroadcast();
        }

        if( getPrefs().getString( Station.ID, null) == null) {
            // first start
            storePrefsString(Station.ID, DEFAULT_STATION_ID);
            storePrefsString(Station.NAME, DEFAULT_STATION_NAME);
            storePrefsString(Station.LOGO, DEFAULT_STATION_LOGO);
            storePrefsString(STREAM_DATA_SOURCE, DEFAULT_STATION_DATA_SOURCE);
        }
    }

    public OneButtonDataHolder getDataHolder() {
        return mDataHolder;
    }

    @Override
    protected void initItems() throws Exception {}

    protected void onDestroy() {
        mBtAdapter.closeProfileProxy(BluetoothProfile.A2DP, mA2dpService);
        releaseMediaPlayer(true);
        getContext().unregisterReceiver(this);
        // super.onDestroy();
    }

    protected void onPause() {
        releaseMediaPlayer(false);
        // super.onPause();
    }

    private void releaseMediaPlayer( boolean switchBTOff) {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }

        if( getPrefs().getBoolean(SoundPreferenceFragment.BTSOUNDSWITCH, false) && switchBTOff) {
            mAudioManager.setBluetoothA2dpOn(false);
            mBtAdapter.closeProfileProxy(BluetoothProfile.A2DP, mA2dpService);
            mBtAdapter.disable();
        }

        mPlaying = false;
        mDataHolder.setHighlighted(false);
        sendUpdateListItemBroadcast();
    }

    private void playMusic() {
        if( mPlayer == null)
            new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            mAudioManager.setMode( AudioManager.MODE_NORMAL);
            mAudioManager.setStreamVolume( AudioManager.STREAM_MUSIC, Integer.parseInt( getPrefs().getString( SoundDetailActivity.VOLUME_LOCAL, "2")), 0);
            mPlayer = new MediaPlayer();
            //  mPlayer.setDataSource("http://yp.shoutcast.com/sbin/tunein-station.pls?k=[APIKEY]&id=99226864");
            // mPlayer.setDataSource( "http://yp.shoutcast.com/sbin/tunein-station.pls?id=99226864");
            //  mPlayer.setDataSource( "http://s2.voscast.com:7016/"); // funzt
           // mPlayer.setDataSource( "http://listen.shoutcast.com/alphafm101-7");
            mPlayer.setDataSource( getPrefs().getString( STREAM_DATA_SOURCE, DEFAULT_STATION_DATA_SOURCE)); // title: 1.FM - Chillout Lounge (www.1.fm)
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // mPlayer.setAudioAttributes(AudioAttributes.CONTENT_TYPE_MUSIC);
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer player) {
                    player.start();
                    mDataHolder.setHighlighted(true);
                    showInfoOnGUI( mStationName == null ? "" : mStationName);
                    mPlaying = true;
                }
            });
            showInfoOnGUI( "warte auf SHOUTcast stream...");
            mPlayer.prepare();
            //mPlayer.prepareAsync();

        } catch(Exception ex) {
            Log.w(TAG, ex.getMessage());
            showInfoOnGUI( ex.getMessage());
        }
    }

    private void showInfoOnGUI( String msg) {
        mDataHolder.setInfo( msg);
        sendUpdateListItemBroadcast();
    }

    private void updateButtonText( String txt) {
        mDataHolder.setButtonText( txt);
        sendUpdateListItemBroadcast();
    }

    void setIsA2dpReady(boolean ready) {
        mIsA2dpReady = ready;
        Toast.makeText(getContext(), "A2DP is " + (ready ? "ready" : "NOT ready"), Toast.LENGTH_SHORT).show();
    }

    private void onPlayAction() {
        mRequestOffWithDelay = false;
        mIdleTimeCnt = IDLE_TIME;

        if( getPrefs().getBoolean(SoundPreferenceFragment.BTSOUNDSWITCH, false)) {
            doSwitchPowerOutlet(true, 1);
            doSwitchReceiver(true, 6);
            doSelectSourceReceiver(12);
        }

        updateButtonText(STOP);
        if( ! getPrefs().getBoolean(SoundPreferenceFragment.BTSOUNDSWITCH, false))
            playMusic();
        else if( mBtAdapter != null) {
            if( ! mBtAdapter.isEnabled()) {
                mBtAdapter.enable();
                startProfileProxyWithDelay( 5);
            }
            else
                startProfileProxyWithDelay( 5);
        }
    }

    private BluetoothProfile.ServiceListener getBTServiceListener() {
        return this;
    }

    private void startProfileProxyWithDelay( final int delaySec) {
        final Handler handler = new Handler();
        handler.postDelayed( new Runnable() {
            @Override
            public void run() {
                mBtAdapter.getProfileProxy(getContext(), getBTServiceListener(), BluetoothProfile.A2DP);
                Log.w(TAG, "mBtAdapter.getProfileProxy");
            }
        }, delaySec * 1000);
    }

    private void onStopAction() {
        mRequestOffWithDelay = true;
        updateButtonText(PLAY);
        releaseMediaPlayer( false);
        if( getPrefs().getBoolean(SoundPreferenceFragment.BTSOUNDSWITCH, false))
            mDataHolder.setButtonInfo( "Receiver Aus in " + mIdleTimeCnt + " Min");
        sendUpdateListItemBroadcast();
    }

    private void doSwitchPowerOutlet( final boolean status, int delaySec) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDataHolder.setButtonInfo( "Steckdose " + (status ? "EIN" : "AUS"));
                sendItemCmdBroadcast( MEDIA_PLUG_SWITCH, status ? "ON" : "OFF");
                sendUpdateListItemBroadcast();
                Log.i(TAG, "Steckdose " + (status ? "EIN" : "AUS"));
            }
        }, delaySec * 1000);
    }

    private void doSwitchReceiver( final boolean status, int delaySec) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDataHolder.setButtonInfo( "Receiver " + (status ? "EIN" : "AUS"));
                sendItemCmdBroadcast( ONKYO_POWER, status ? "ON" : "OFF");
                sendUpdateListItemBroadcast();
                Log.i(TAG, "Receiver " + (status ? "EIN" : "AUS"));
            }
        }, delaySec * 1000);
    }

    private void doSelectSourceReceiver( int delaySec) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDataHolder.setButtonInfo( "Source AUX");
                sendItemCmdBroadcast( ONKYO_SOURCE_AUX, "ON");
                sendUpdateListItemBroadcast();
                Log.i(TAG, "Source AUX");
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        mDataHolder.setButtonInfo( "");
                        sendUpdateListItemBroadcast();
                    }
                }, 5000);
            }
        }, delaySec * 1000);
    }


    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "receive intent for action : " + action);
        if(action.equals(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.SOUND)) {
            String currBtnState = mDataHolder.getButtonText();
            if( currBtnState.equals(PLAY))
                onPlayAction();
            else
                onStopAction();
        }
        else if(action.equals( MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.SOUND))
            handleOnPanelClick();
        else if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_DISCONNECTED);
            if (state == BluetoothA2dp.STATE_CONNECTED) {
                setIsA2dpReady(true);
                playMusic();
            } else if (state == BluetoothA2dp.STATE_DISCONNECTED) {
                setIsA2dpReady(false);
            }
        }
        else if (action.equals(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_NOT_PLAYING);
            if (state == BluetoothA2dp.STATE_PLAYING) {
                Log.d(TAG, "A2DP start playing");
               // Toast.makeText(getContext(), "A2dp is playing", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "A2DP stop playing");
                // Toast.makeText(getContext(), "A2dp is stopped", Toast.LENGTH_SHORT).show();
            }
        }
        else if( action.equals( Intent.ACTION_TIME_TICK)) {
            if( mRequestOffWithDelay) {
                if (mIdleTimeCnt == 0 && isModulInList()) {
                    doSwitchPowerOutlet(false, 1);
                    mRequestOffWithDelay = false;
                }
                else {
                    mIdleTimeCnt--;
                    mDataHolder.setButtonInfo("Receiver Aus in " + mIdleTimeCnt + " Min");
                }
                sendUpdateListItemBroadcast();
            }

            if( mPlaying)
                new NowPlayingUpdater().execute( getPrefs().getString(Station.NAME, ""));
        }
        else if( action.equals(MainActivity.ACTION_ACTIVITY_RESULT + MainActivity.SOUND)) {
            mStationName = intent.getStringExtra( Station.NAME);
            mStationId = intent.getStringExtra( Station.ID);
            String stationGenre = intent.getStringExtra( Station.GENRE);
            String stationLogo = intent.getStringExtra( Station.LOGO);

            storePrefsString(Station.ID, mStationId);
            storePrefsString(Station.NAME, mStationName);
            storePrefsString(Station.GENRE, stationGenre);
            storePrefsString(Station.LOGO, stationLogo);

            showInfoOnGUI( mStationName);

            new StationDownloader().execute( mStationId);
            new LogoDownloader().execute( stationLogo);
        }
        else if( action.equals(MainListAdapter.ACTION_CHANGED_IN_LIST_MODUL + MainActivity.SOUND)) {
            handleOnShowOrHideOnModulList( intent.getBooleanExtra( MainListAdapter.KEY_SHOW_IN_LIST, false));
        }
    }

    private void handleOnPanelClick() {
        Intent soundDetailIntent = new Intent("soundDetailIntent", null, getContext(), SoundDetailActivity.class);
        ((Activity)getContext()).startActivityForResult(soundDetailIntent, DETAIL_ACTIVITY_RESULT_CODE);
    }

    private void handleOnShowOrHideOnModulList( boolean show) {
        if( show) {
            mStationName = getPrefs().getString(Station.NAME, mStationName);
            String storedLogoUrl = getPrefs().getString(Station.LOGO, null);
            if( storedLogoUrl != null)
                new LogoDownloader().execute( storedLogoUrl);
            showInfoOnGUI( mStationName);
        }
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile a2dp) {
        Log.d(TAG, "a2dp service connected. profile = " + profile);

        //String btSoundAdapterName = getContext().getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE).getString(SoundPreferenceFragment.MYBTSOUNDADAPTER, null);
        String btSoundAdapterName = getPrefs().getString( SoundPreferenceFragment.MYBTSOUNDADAPTER, null);
        Log.d(TAG, "connecting to BT sound adapter: " + (btSoundAdapterName == null ? "null" : btSoundAdapterName));

        if( btSoundAdapterName == null) {
            Log.w(TAG, "no Bluetooth adapter defined in settings");
            showInfoOnGUI( "no Bluetooth adapter defined in settings");
            return;
        }

        Method connect = getConnectMethod();
        BluetoothDevice device = findBondedDeviceByName(mBtAdapter, btSoundAdapterName);//"PX-3342");

        if (connect == null || device == null) {
            Log.w(TAG, "connect or device is null");
            showInfoOnGUI( btSoundAdapterName + " nicht verbunden");
        }

        BluetoothA2dp proxy = (BluetoothA2dp)a2dp;

        try {
            connect.setAccessible(true);
            connect.invoke(proxy, device);
            showInfoOnGUI( btSoundAdapterName + " verbunden");
        } catch (InvocationTargetException ex) {
            Log.e(TAG, "Unable to invoke connect(BluetoothDevice) method on proxy. " + ex.toString());
            showInfoOnGUI( btSoundAdapterName + " Fehler");
        } catch (IllegalAccessException ex) {
            Log.e(TAG, "Illegal Access! " + ex.toString());
            showInfoOnGUI( btSoundAdapterName + " Fehler");
        }

        List<BluetoothDevice> connectedDevices = a2dp.getConnectedDevices();
        Iterator<BluetoothDevice> connectedDevicesIter = connectedDevices.iterator();
        while( connectedDevicesIter.hasNext()) {
            BluetoothDevice btDev = connectedDevicesIter.next();
            Log.d(TAG, "connected device: " + btDev.getName());
        }

        if (profile == BluetoothProfile.A2DP) {
            mA2dpService = (BluetoothA2dp) a2dp;

            int audioMode = mAudioManager.getMode();
            Log.i(TAG, "audion mode: " + getAudioModeAsString(audioMode));

            if (mAudioManager.isBluetoothA2dpOn()) {
                setIsA2dpReady(true);
                playMusic();
            } else {
                Log.d(TAG, "bluetooth a2dp is not on while service connected");
            }
        }
    }

    private String getAudioModeAsString( int mode) {
        switch(mode) {
            case AudioManager.MODE_NORMAL : return "MODE_NORMAL";
            case AudioManager.MODE_RINGTONE : return "MODE_RINGTONE";
            case AudioManager.MODE_IN_CALL : return "MODE_IN_CALL";
            case AudioManager.MODE_IN_COMMUNICATION : return "MODE_IN_COMMUNICATION";
            default : return "UNKNOWN translation: " + mode;
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {
        setIsA2dpReady(false);
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // some codes from here:
    // https://github.com/kcoppock/bluetooth-a2dp/blob/master/BluetoothConnector/src/main/java/com/kcoppock/bluetoothconnector/BluetoothActivity.java
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Wrapper around some reflection code to get the hidden 'connect()' method
     * @return the connect(BluetoothDevice) method, or null if it could not be found
     */
    private Method getConnectMethod () {
        try {
            return BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
        } catch (NoSuchMethodException ex) {
            Log.e(TAG, "Unable to find connect(BluetoothDevice) method in BluetoothA2dp proxy.");
            return null;
        }
    }

    /**
     * Search the set of bonded devices in the BluetoothAdapter for one that matches
     * the given name
     * @param adapter the BluetoothAdapter whose bonded devices should be queried
     * @param name the name of the device to search for
     * @return the BluetoothDevice by the given name (if found); null if it was not found
     */
    private static BluetoothDevice findBondedDeviceByName (BluetoothAdapter adapter, String name) {
        for (BluetoothDevice device : getBondedDevices(adapter)) {
            if (name.matches(device.getName())) {
                Log.d(TAG, String.format("Found device with name %s and address %s.", device.getName(), device.getAddress()));
                return device;
            }
        }
        Log.w(TAG, String.format("Unable to find device with name %s.", name));
        return null;
    }

    /**
     * Safety wrapper around BluetoothAdapter#getBondedDevices() that is guaranteed
     * to return a non-null result
     * @param adapter the BluetoothAdapter whose bonded devices should be obtained
     * @return the set of all bonded devices to the adapter; an empty set if there was an error
     */
    private static Set<BluetoothDevice> getBondedDevices (BluetoothAdapter adapter) {
        Set<BluetoothDevice> results = adapter.getBondedDevices();
        if (results == null) {
            results = new HashSet<BluetoothDevice>();
        }
        return results;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Station downloader
    //
    // curl http://yp.shoutcast.com/sbin/tunein-station.pls?id=99226864
    // [playlist]
    // numberofentries=1
    // File1=http://listen.shoutcast.com/alphafm101-7
    // Title1=Alpha FM 101,7
    // Length1=-1
    // Version=2
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class StationDownloader extends AsyncTask<String, String, ArrayList<String>> {

        private String BASE_URL = "http://yp.shoutcast.com/sbin/tunein-station.pls?id=";

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            String stationId = params[0];
            String fullUrl = BASE_URL + stationId;

            try {
                Log.d(TAG, "stationId download doInBackground [url: " + fullUrl + "]");
                return convertStreamToLines( new URL(fullUrl).openStream());
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute( ArrayList<String> stationData) {
            if( stationData != null) {

                String streamDataSource = findValueForKey(stationData, "File1");
                storePrefsString(STREAM_DATA_SOURCE, streamDataSource);

                releaseMediaPlayer(false);
                if (mDataHolder.getButtonText().equals(STOP)) {  // STOP means playing
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if( getPrefs().getBoolean(SoundPreferenceFragment.BTSOUNDSWITCH, false) && mAudioManager.isBluetoothA2dpOn())
                                playMusic();
                            else
                                onPlayAction();
                        }
                    }, 3000);
                }
            }
        }

        private String findValueForKey( ArrayList<String> arrList, String key) {
            for( String entry : arrList) {
                String[] kv = entry.split( "=");
                if( kv[0].equals( key))
                    return kv[1];
            }
            return null;
        }

        private ArrayList convertStreamToLines(InputStream is) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            ArrayList<String> arrList = new ArrayList();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    arrList.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return arrList;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Logo downloader
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class LogoDownloader extends AsyncTask<String, String, Drawable> {

        @Override
        protected Drawable doInBackground(String... params) {
            String url = params[0];
            try {
                Log.d(TAG, "Logo download doInBackground [url: " + url + "]");
                return Drawable.createFromStream(((java.io.InputStream) new java.net.URL(url).getContent()), "stationLogo");
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute( Drawable drawImg) {
            mDataHolder.setImgResId( drawImg == null ? R.drawable.music : -1);
            mDataHolder.setImgDrawable( drawImg);
            sendUpdateListItemBroadcast();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Now playing downloader
    // http://api.shoutcast.com/legacy/stationsearch?k=[APIKEY]&search=Chaabi%20Maroc%20by%20Yabiladi.com
    // k: api key
    // search: station name
    // response:
    // <stationlist>
    // <tunein base="/sbin/tunein-station.pls" base-m3u="/sbin/tunein-station.m3u" base-xspf="/sbin/tunein-station.xspf"/>
    // <station name="Chaabi Maroc by Yabiladi.com" mt="audio/mpeg" id="1518956" br="128" genre="Ambient" genre2="Classical" logo="http://i.radionomy.com/document/radios/8/8256/8256aa3e-d2e3-48c6-8b70-25396fd35cfb.jpg" ct="Mustapha El Berkani - Thala Fina" lc="90"/>
    // </stationlist>
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class NowPlayingUpdater extends AsyncTask<String, String, String> {

        private String BASE_URL = "http://api.shoutcast.com/legacy/stationsearch?k=[APIKEY]&search=";

        public static final String STATION = "station";
        public static final String CT = "ct";

        @Override
        protected String doInBackground(String... params) {
            String stationName = params[0];
            String fullUrl = BASE_URL + stationName;
            String nowPlaying = null;

            if( stationName != null && stationName.length() > 0) {
                try {
                    Log.d(TAG, "Now playing download doInBackground [url: " + fullUrl + "]");
                    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser parser = factory.newPullParser();
                    InputStream stream = new URL(fullUrl).openConnection().getInputStream();
                    parser.setInput(stream, null);

                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        String tagname = parser.getName();
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                if (tagname.equalsIgnoreCase(STATION)) {
                                    nowPlaying = parser.getAttributeValue(null, CT);
                                    break;
                                }
                            case XmlPullParser.END_TAG:
                                break;

                            default:
                                break;
                        }
                        eventType = parser.next();
                    }

                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                }
            }
            return nowPlaying;
        }

        @Override
        protected void onPostExecute( String nowPlaying) {
            if( nowPlaying != null && nowPlaying.length() > 0) {
                showInfoOnGUI( nowPlaying);
                // just show for a few time
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        showInfoOnGUI( mStationName == null ? "" : mStationName);
                    }
                }, 15000);
            }
        }
    }



}
