package biz.kindler.rigi.modul.sound;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;

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
import biz.kindler.rigi.settings.SoundPreferenceFragment2;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 08.01.19
 */

public class SoundModel3 extends BaseModel implements SessionManagerListener, MediaPlayer.OnPreparedListener {

    private final static String TAG = SoundModel.class.getSimpleName();

    public final static int     DETAIL_ACTIVITY_RESULT_CODE = 299;

    // Button Text
    private static final String  PLAY    = "Play";
    private static final String  STOP    = "Stop";
    // hab item
    private static final String  MEDIA_PLUG_SWITCH  = "TV_Steckdose";
    private static final String  STREAM_DATA_SOURCE = "StreamDataSource";

    private static final String  DEFAULT_STATION_ID             = "1702206";
    private static final String  DEFAULT_STATION_NAME           = "1.FM - Chillout Lounge (www.1.fm)";
    private static final String  DEFAULT_STATION_LOGO           = "http://i.radionomy.com/document/radios/4/4bfa/4bfa5a33-ef4d-4caa-b0cd-99be7cb93aee.jpg";
    private static final String  DEFAULT_STATION_DATA_SOURCE    = "http://185.33.21.112:80/chilloutlounge_128";
    private static final String  SHOUTCAST_LOGO                 = "http://wiki.shoutcast.com/images/6/61/Logo_shoutcast.png";


    private OneButtonDataHolder mDataHolder;
    private String              mStationName;
    private String              mStationId;
    private boolean             mPlaying;
    private CastContext         mCastContext;
    private CastSession         mCastSession;
    private SessionManager      mSessionManager;
    private MediaPlayer         mLocalMediaPlayer;
    private WebImage            mShoutcastWebImage;

    public SoundModel3(Context ctx) {
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

        mShoutcastWebImage = new WebImage(Uri.parse(SHOUTCAST_LOGO));
    }

    public OneButtonDataHolder getDataHolder() {
        return mDataHolder;
    }

    @Override
    protected void initItems() throws Exception {}

    public boolean isPlayLocal() {
        return ! getPrefs().getBoolean(SoundPreferenceFragment2.SOUNDSWITCH, false);
    }

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
        String playUrl = getPrefs().getString(STREAM_DATA_SOURCE, DEFAULT_STATION_DATA_SOURCE);
        if( isPlayLocal())
            playLocal( playUrl);
        else
            playRemote( playUrl);
    }

    private void onStopAction() {
        if( isPlayLocal())
            stopLocal();
        else
            stopRemote();

        handleMusicServiceStatus(MusicPlayerService.STOPPED, "");
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
            if( currBtnState.equals(PLAY)) {
                if( getPrefs().getBoolean(SoundPreferenceFragment2.SOCKETSWITCH, false))
                    doSwitchPowerOutlet(true, 0);
                onPlayAction();
            }
            else {
                onStopAction();
                if( getPrefs().getBoolean(SoundPreferenceFragment2.SOCKETSWITCH, false))
                    doSwitchPowerOutlet(false, 1);
            }
        }
        else if(action.equals( MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.SOUND))
            handleOnPanelClick();
        else if( action.equals( Intent.ACTION_TIME_TICK)) {
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
        if( mPlaying) {
            if( isPlayLocal())
                stopLocal();
            else
                stopRemote();
        }

        mStationName = stationName;
        mStationId = stationId;

        storePrefsString(Station.ID, mStationId);
        storePrefsString(Station.NAME, mStationName);
        storePrefsString(Station.GENRE, stationGenre);
        storePrefsString(Station.LOGO, stationLogo);

        showInfoOnGUI( mStationName);

        new StationDownloader().execute(mStationId);
        new LogoDownloader().execute(stationLogo);

        handleMusicServiceStatus( MusicPlayerService.PREPARING, "");

        onPlayAction();
    }

    private void handleMusicServiceStatus( int status, String txt) {
        Log.d(TAG, "status:" + status + ",txt:" + txt);
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
                break;
            case MusicPlayerService.INFO :
                showInfoOnGUI( txt);
                break;
        }
    }

    @Override
    public void onSessionStarting(Session session) {
        Log.d(TAG, "onSessionStarting [" + session.toString() + "]");
    }

    @Override
    public void onSessionStarted(Session session, String s) {
        Log.d(TAG, "onSessionStarted [" + session.toString() + ", " + s + "]");
    }

    @Override
    public void onSessionStartFailed(Session session, int i) {
        Log.d(TAG, "onSessionStartFailed [" + session.toString() + ", " + i + "]");
    }

    @Override
    public void onSessionEnding(Session session) {
        Log.d(TAG, "onSessionEnding [" + session.toString() + "]");
    }

    @Override
    public void onSessionEnded(Session session, int i) {
        Log.d(TAG, "onSessionEnded [" + session.toString() + ", " + i + "]");
    }

    @Override
    public void onSessionResuming(Session session, String s) {
        Log.d(TAG, "onSessionResuming [" + session.toString() + ", " + s + "]");
    }

    @Override
    public void onSessionResumed(Session session, boolean b) {
        Log.d(TAG, "onSessionResumed [" + session.toString() + ", " + b + "]");
    }

    @Override
    public void onSessionResumeFailed(Session session, int i) {
        Log.d(TAG, "onSessionResumeFailed [" + session.toString() + ", " + i + "]");
    }

    @Override
    public void onSessionSuspended(Session session, int i) {
        Log.d(TAG, "onSessionSuspended [" + session.toString() + ", " + i + "]");
    }

    private void playLocal( final String url) {
        handleMusicServiceStatus( MusicPlayerService.PREPARING, "");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "play stream url: " + url);
                    mLocalMediaPlayer = new MediaPlayer();
                    mLocalMediaPlayer.setOnPreparedListener(SoundModel3.this);
                    mLocalMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mLocalMediaPlayer.setDataSource( url);
                    mLocalMediaPlayer.prepare(); // might take long! (for buffering, etc)
                } catch(Exception ex) {
                    Log.w(TAG, ex.getMessage());
                    showInfoOnGUI( ex.getMessage());
                }
            }
        }, 1000);
    }

    private void stopLocal() {
        Log.w(TAG, "stopLocal");
        if( mLocalMediaPlayer != null) {
            mLocalMediaPlayer.stop();
            mLocalMediaPlayer.release();
        }
        mLocalMediaPlayer = null;
        handleMusicServiceStatus(MusicPlayerService.STOPPED, "");
    }

    private void playRemote( final String url) {
        handleMusicServiceStatus( MusicPlayerService.PREPARING, "");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    mCastContext = CastContext.getSharedInstance(getContext());
                    mSessionManager = mCastContext.getSessionManager();
                    mCastSession = mSessionManager.getCurrentCastSession();
                    mSessionManager.addSessionManagerListener(SoundModel3.this);

                    MediaInfo mediaInfo = new MediaInfo.Builder(url)
                            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                            //.setContentType("audio/mp3")
                            .setContentType("audio")
                            .setMetadata(getMediaMetadata())
                            // .setStreamDuration(mSelectedMedia.getDuration() * 1000)
                            .build();
                    if (mCastSession != null) {
                        RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
                        remoteMediaClient.load(mediaInfo);
                        updateButtonText(STOP);
                        handleMusicServiceStatus(MusicPlayerService.PLAYING, "");
                    } else {
                        showInfoOnGUI("no cast device");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onPlayAction();
                            }
                        }, 5000); // retray to play every 5 seconds when no cast device found
                    }

                } catch (Exception ex) {
                    Log.w(TAG, ex.getMessage());
                    showInfoOnGUI(ex.getMessage());
                }
            }
        }, 1000);
    }

    private void stopRemote() {
        Log.w(TAG, "stopRemote");
        if( mCastSession != null)
            mCastSession.getRemoteMediaClient().stop();
        if( mSessionManager != null)
            mSessionManager.removeSessionManagerListener(this);
        handleMusicServiceStatus(MusicPlayerService.STOPPED, "");
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if( mLocalMediaPlayer != null) {
            mLocalMediaPlayer.start();
            handleMusicServiceStatus(MusicPlayerService.PLAYING, "");
        }
    }

    private MediaMetadata getMediaMetadata() {
        MediaMetadata metaData = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
        metaData.clearImages();
        Uri uri = null;

        try {
            String logo = getPrefs().getString(Station.LOGO, "");
            if( logo.length() > 0)
                metaData.addImage(new WebImage(Uri.parse(logo)));
            else
                Log.d(TAG, "station logo not available");

            Log.d(TAG, "stationName: " + mStationName + ", logo: " + logo);
            metaData.putString(MediaMetadata.KEY_TITLE, mStationName);

            if(! metaData.hasImages())
                metaData.addImage( mShoutcastWebImage);

        } catch(Exception ex) {
            Log.w(TAG, ex.getMessage());
        }
        return metaData;
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

                String url = getPrefs().getString(STREAM_DATA_SOURCE, null); // DEFAULT_STATION_DATA_SOURCE);0);
                if( url != null) {
                    if( isPlayLocal())
                        playLocal(url);
                    else
                        playRemote(url);
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
            String apiKey = getPrefs().getString( SoundPreferenceFragment2.SHOUTCAST_API_KEY, "");
            Log.i(TAG, "Shoutcast apiKey: " + apiKey);
            String nowPlaying = null;

            if( apiKey == null || apiKey.trim().length() == 0)
                return "no api key";
            else {
                String baseUrlWithApiKey = BASE_URL.replace( "[APIKEY]", apiKey);
                String fullUrl = baseUrlWithApiKey + stationName;
                Log.i(TAG, "Full URL: " + fullUrl);

                if (stationName != null && stationName.length() > 0) {
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
