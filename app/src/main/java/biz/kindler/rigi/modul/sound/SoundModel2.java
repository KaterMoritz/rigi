package biz.kindler.rigi.modul.sound;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.OneButtonDataHolder;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.settings.SoundPreferenceFragment;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 11.09.17.
 */

public class SoundModel2 extends BaseModel {

    private final static String TAG = SoundModel.class.getSimpleName();

    public final static int     DETAIL_ACTIVITY_RESULT_CODE = 299;
    public final static String  ACTION_PLAYER_CMD   = "action-player-cmd";
    public final static String  CMD                 = "cmd";
    public final static String  CMD_PLAY            = "cmd-play";
    public final static String  CMD_STOP            = "cmd-stop";
    public final static String  CMD_CHANGE_STATION  = "cmd-change-station";
    public final static String  CMD_BT_ENABLE       = "cmd-bt-enable";  // true: enable BT, false disable BT

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
    private boolean             mPlaying;
    private Intent              mMusicPlayerServiceIntent;
    private static final int    IDLE_TIME    = 5; // 10 timeticks (10 minutes)

    public SoundModel2(Context ctx) {
        super(ctx, MainActivity.SOUND);

        mDataHolder = new SoundDataHolder();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.SOUND);
        intentFilter.addAction(MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.SOUND);
        intentFilter.addAction(MainActivity.ACTION_ACTIVITY_RESULT + MainActivity.SOUND);
        intentFilter.addAction(MainListAdapter.ACTION_CHANGED_IN_LIST_MODUL + MainActivity.SOUND);
        intentFilter.addAction(MusicPlayerService.ACTION_MUSICPLAYER_SERVICE);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(MEDIA_PLUG_SWITCH);
        ctx.registerReceiver(this, intentFilter);

        if( getPrefs().getString( Station.ID, null) == null) {
            // first start
            storePrefsString(Station.ID, DEFAULT_STATION_ID);
            storePrefsString(Station.NAME, DEFAULT_STATION_NAME);
            storePrefsString(Station.LOGO, DEFAULT_STATION_LOGO);
            storePrefsString(STREAM_DATA_SOURCE, DEFAULT_STATION_DATA_SOURCE);
        }

        mMusicPlayerServiceIntent = new Intent((Activity)getContext(), MusicPlayerService.class);
    }

    public OneButtonDataHolder getDataHolder() {
        return mDataHolder;
    }

    @Override
    protected void initItems() throws Exception {}

    private void showInfoOnGUI( String msg) {
        mDataHolder.setInfo( msg);
        sendUpdateListItemBroadcast();
    }

    private void showInfoOnGUI( String msg, boolean highlight) {
        mDataHolder.setInfo( msg);
        mDataHolder.setHighlighted( highlight);
        sendUpdateListItemBroadcast();
    }


    private void updateButtonText( String txt) {
        mDataHolder.setButtonText( txt);
        sendUpdateListItemBroadcast();
    }

    private void onPlayAction() {

        if( getPrefs().getBoolean(SoundPreferenceFragment.BTSOUNDSWITCH, false) && ! mRequestOffWithDelay)
            doSwitchPowerOutlet(true, 1);
        else
            mDataHolder.setButtonInfo("");

        mRequestOffWithDelay = false;
        mIdleTimeCnt = IDLE_TIME;

        updateButtonText(STOP);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                getContext().startService(mMusicPlayerServiceIntent);
            }
        }, 1000);

        /*if( ! getPrefs().getBoolean(SoundPreferenceFragment.BTSOUNDSWITCH, false))
            playMusic();
        else if( mBtAdapter != null) {
            if( ! mBtAdapter.isEnabled()) {
                mBtAdapter.enable();
                startProfileProxyWithDelay( 5);
            }
            else
                startProfileProxyWithDelay( 5);
        } */
    }

    private void onStopAction() {
        updateButtonText(PLAY);

        // getContext().stopService(mMusicPlayerServiceIntent);
        sendBTCmdBroadcast( CMD_STOP, false); // false: disable BT

        if( getPrefs().getBoolean(SoundPreferenceFragment.BTSOUNDSWITCH, false)) {
            mRequestOffWithDelay = true;
            mDataHolder.setButtonInfo("Receiver Aus in " + mIdleTimeCnt + " Min");
        }

        sendUpdateListItemBroadcast();
    }

    private void doSwitchPowerOutlet( final boolean status, int delaySec) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showButtonTextWithDelay( "Steckdose " + (status ? "EIN" : "AUS"), 0);
                sendItemCmdBroadcast( MEDIA_PLUG_SWITCH, status ? "ON" : "OFF");
                Log.i(TAG, "Steckdose " + (status ? "EIN" : "AUS"));
                showButtonTextWithDelay( "", 20);
                if( ! status) // if OFF
                    getContext().stopService(mMusicPlayerServiceIntent);
            }
        }, delaySec * 1000);
    }

    private void showButtonTextWithDelay( final String text, int delaySec) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mDataHolder.setButtonInfo( text);
                sendUpdateListItemBroadcast();
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
            if( mStationId == null || ! mStationId.equals( intent.getStringExtra( Station.ID)))  // station has changed
                handleNewStationSelected(intent.getStringExtra( Station.NAME), intent.getStringExtra( Station.ID), intent.getStringExtra( Station.GENRE), intent.getStringExtra( Station.LOGO));
        }
        else if( action.equals(MainListAdapter.ACTION_CHANGED_IN_LIST_MODUL + MainActivity.SOUND)) {
            handleOnShowOrHideOnModulList( intent.getBooleanExtra( MainListAdapter.KEY_SHOW_IN_LIST, false));
        }
        else if( action.equals(MusicPlayerService.ACTION_MUSICPLAYER_SERVICE)) {
            int status = intent.getIntExtra(MusicPlayerService.STATUS, -1);
            String txt = intent.getStringExtra(MusicPlayerService.TEXT);
            handleMusicServiceStatus(status, txt);
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

    private void handleNewStationSelected(String stationName, String stationId, String stationGenre, String stationLogo) {
     //   getContext().stopService(mMusicPlayerServiceIntent);

       // sendBTCmdBroadcast( CMD_STOP);

        mStationName = stationName;
        mStationId = stationId;

        storePrefsString(Station.ID, mStationId);
        storePrefsString(Station.NAME, mStationName);
        storePrefsString(Station.GENRE, stationGenre);
        storePrefsString(Station.LOGO, stationLogo);

        showInfoOnGUI( mStationName);

        new StationDownloader().execute(mStationId);
        new LogoDownloader().execute(stationLogo);

       // sendBTCmdBroadcast( CMD_PLAY);
        sendBTCmdBroadcast( CMD_CHANGE_STATION);
      //  handleMusicServiceStatus( MusicPlayerService.PREPARING, "");
    }

    private void handleMusicServiceStatus( int status, String txt) {
        System.out.println( "status:" + status + ",txt:" + txt);
        switch( status) {
            case MusicPlayerService.PREPARING :
                showInfoOnGUI( "warte auf SHOUTcast stream...");
                updateButtonText(STOP);
                break;
            case MusicPlayerService.PLAYING :
                mPlaying = true;
                showInfoOnGUI( mStationName, true);
                updateButtonText(STOP);
                break;
            case MusicPlayerService.STOPPED :
                mPlaying = false;
                showInfoOnGUI( mStationName, false);
                updateButtonText(PLAY);
                break;
            case MusicPlayerService.ERROR :
                mPlaying = false;
                showInfoOnGUI( txt, false);
                // askForBTSettingsDialog( 3);
                break;
            case MusicPlayerService.INFO :
                showInfoOnGUI( txt);
                break;
        }
    }

    private void askForBTSettingsDialog( int delaySec) {
        final SweetAlertDialog dlg = new SweetAlertDialog(getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getContext().getResources().getString( R.string.show_bluetooth_settings))
                .setCustomImage(R.drawable.bluetooth)
                .setConfirmButton("Ja", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        Intent intentOpenBluetoothSettings = new Intent();
                        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                        getContext().startActivity(intentOpenBluetoothSettings);
                    }
                })
                .setCancelButton("Nein", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                });
        new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {
                dlg.show();
            }
        }, delaySec * 1000);
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

              //  releaseMediaPlayer(false);
                if (mDataHolder.getButtonText().equals(STOP)) {  // STOP means playing
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if( ! mPlaying)
                                getContext().startService(mMusicPlayerServiceIntent);
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
    protected void sendBTCmdBroadcast( String cmd) {
        Intent bc = new Intent();
        bc.setAction( ACTION_PLAYER_CMD);
        bc.putExtra( CMD, cmd);
        getContext().sendBroadcast(bc);
    }

    protected void sendBTCmdBroadcast( String cmd, boolean btEnable) {
        Intent bc = new Intent();
        bc.setAction( ACTION_PLAYER_CMD);
        bc.putExtra( CMD, cmd);
        bc.putExtra( CMD_BT_ENABLE, btEnable);
        getContext().sendBroadcast(bc);
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
