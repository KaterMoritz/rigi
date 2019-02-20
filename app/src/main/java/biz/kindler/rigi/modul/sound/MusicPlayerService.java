package biz.kindler.rigi.modul.sound;

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import biz.kindler.rigi.settings.SoundPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 11.09.17.
 */

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, BluetoothProfile.ServiceListener {

    public final static String TAG = MusicPlayerService.class.getSimpleName();

    // Status
    public final static int ERROR = -1;
    public final static int PREPARING = 1;
    public final static int PLAYING = 2;
    public final static int STOPPED = 3;
    public final static int INFO = 4;
    // Plug State
    // public final static int      PLUG_UNKNOWN                   = 0;
    // public final static int      PLUG_OFF                       = 1;
    // public final static int      PLUG_ON                        = 2;
    // Command State
    public final static int CMD_IDLE = 0;
    public final static int CMD_STOP = 1;
    public final static int CMD_PLAY = 2;
    // Bluetooth State
    public final static int BT_UNKNOWN = 0;
    public final static int BT_DISABLED = 1;
    public final static int BT_ENABLING = 2;
    public final static int BT_ENABLED = 3;
    // public final static int      BT_DISCONNECTED                = 4;
    // public final static int      BT_CONNECTING                  = 5;
    //  public final static int      BT_CONNECTED                   = 6;
    // Player State
    public final static int PLAYER_STOPPED = 0;
    public final static int PLAYER_CONNECTING = 1;
    public final static int PLAYER_PLAYING = 2;

    // broadcast keys
    public final static String ACTION_MUSICPLAYER_SERVICE = "action_mucic_player_service";
    public final static String STATUS = "status";
    public final static String TEXT = "text";

    private static final String STREAM_DATA_SOURCE = "StreamDataSource";
    private static final String DEFAULT_STATION_DATA_SOURCE = "http://185.33.21.112:80/chilloutlounge_128";
    private static final String BLUETOOTH_ENABLED = "bluetooth_enabled";

    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private BluetoothAdapter mBtAdapter;
    private BluetoothA2dp mA2dpService;
    private MyBroadcastReceiver mBCReceiver;
    private SimpleDateFormat mDateTimeFormatter = new SimpleDateFormat("HH:mm.ss");

    //  private int                 mPlugState;
    private int mCmdState;
    private int mBTEnableState;
    private int mBTProfileState;
    private int mPlayerState;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // mMediaPlayer = new MediaPlayer();// raw/s.mp3
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mBCReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SoundModel2.ACTION_PLAYER_CMD);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mBCReceiver, intentFilter);
    }

    private void handleQuarterWatchdog() {
        Log.d(TAG, "handleQuarterWatchdog: [" + mDateTimeFormatter.format(new Date()) + "] CmdState:" + getCmdStateAsString(mCmdState) + ", BTEnabledState:" + getBTEnabledStateAsString(mBTEnableState) + ", BTProfileState:" + getBTProfileStateAsString(mBTProfileState) + ", PlayerState:" + getPlayerStateAsString(mPlayerState));

        if (mCmdState == CMD_PLAY && (mBTEnableState == BT_UNKNOWN || mBTEnableState == BT_DISABLED))
            setBTEnable(true);
        else if (mCmdState == CMD_PLAY && mBTEnableState == BT_ENABLED && mBTProfileState == BluetoothProfile.STATE_DISCONNECTED && mPlayerState == PLAYER_STOPPED)
            connectBT();
        else if (mCmdState == CMD_PLAY && mBTEnableState == BT_ENABLED && mBTProfileState == BluetoothProfile.STATE_CONNECTING && mPlayerState == PLAYER_STOPPED)
            checkBTProfileState();
        else if (mCmdState == CMD_PLAY && mBTEnableState == BT_ENABLED && mBTProfileState == BluetoothProfile.STATE_CONNECTED && mPlayerState == PLAYER_STOPPED)
            startMediaPlayer();

    }

    private void checkBTProfileState() {
        mBTProfileState = mBtAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
    }
/*
    private String getPlugStateAsString( int cmdState) {
        switch( cmdState) {
            case PLUG_UNKNOWN : return "PLUG_UNKNOWN";
            case PLUG_OFF : return "PLUG_OFF";
            case PLUG_ON : return "PLUG_ON";
            default: return "?";
        }
    } */

    public int getCmdState() {
        return mCmdState;
    }

    private String getCmdStateAsString(int cmdState) {
        switch (cmdState) {
            case CMD_IDLE:
                return "CMD_IDLE";
            case CMD_STOP:
                return "CMD_STOP";
            case CMD_PLAY:
                return "CMD_PLAY";
            default:
                return "?";
        }
    }

    private String getBTEnabledStateAsString(int btEnableState) {
        switch (btEnableState) {
            case BT_UNKNOWN:
                return "BT_UNKNOWN";
            case BT_DISABLED:
                return "BT_DISABLED";
            case BT_ENABLING:
                return "BT_ENABLING";
            case BT_ENABLED:
                return "BT_ENABLED";
            //   case BT_DISCONNECTED : return "BT_DISCONNECTED";
            //   case BT_CONNECTING   : return "BT_CONNECTING";
            //   case BT_CONNECTED    : return "BT_CONNECTED";
            default:
                return "?";
        }
    }

    private String getBTProfileStateAsString(int btProfileState) {
        switch (btProfileState) {
            case BluetoothProfile.STATE_DISCONNECTING:
                return "A2DP_DISCONNECTING";
            case BluetoothProfile.STATE_DISCONNECTED:
                return "A2DP_DISCONNECTED";
            case BluetoothProfile.STATE_CONNECTING:
                return "A2DP_CONNECTING";
            case BluetoothProfile.STATE_CONNECTED:
                return "A2DP_CONNECTED";
            default:
                return "?";
        }
    }

    private String getPlayerStateAsString(int playerState) {
        switch (playerState) {
            case PLAYER_STOPPED:
                return "PLAYER_STOPPED";
            case PLAYER_CONNECTING:
                return "PLAYER_CONNECTING";
            case PLAYER_PLAYING:
                return "PLAYER_PLAYING";
            default:
                return "?";
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mCmdState = CMD_PLAY;
        mBTEnableState = BT_ENABLING;
        setBTEnable(true);

        handleTimeTick(); // to get started now
        /*
        if( getPrefs().getBoolean(SoundPreferenceFragment.BTSOUNDSWITCH, false))
            checkBluetoothReady();

        else if ( ! mMediaPlayer.isPlaying())
            startMediaPlayer();
*/
        return START_NOT_STICKY;
    }

    @Override
    public boolean stopService(Intent intent) {
        System.out.println("stopService");
        return super.stopService(intent);
    }

    private void setBTEnable(boolean status) {
        if (mBtAdapter == null)
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter == null)
            sendUpdateStatusBroadcast(ERROR, "no bluetooth adapter");
        else if (status && !mBtAdapter.isEnabled())
            mBtAdapter.enable();
        else if (!status && mBtAdapter.isEnabled())
            mBtAdapter.disable();
        else if (mBtAdapter.isEnabled())
            mBTEnableState = BT_ENABLED;
    }

    private void checkBTEnableState() {
        if (mBtAdapter == null)
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter == null) {
            sendUpdateStatusBroadcast(ERROR, "no bluetooth adapter");
            mBTEnableState = BT_DISABLED;
        } else
            mBTEnableState = mBtAdapter.isEnabled() ? BT_ENABLED : BT_DISABLED;
    }

    private void connectBT() {
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(getPrefs().getString(SoundDetailActivity.VOLUME_LOCAL, "2")), 0);
        // Establish connection to the proxy.
        mBtAdapter.getProfileProxy(this, this, BluetoothProfile.A2DP);
        // mBTState = BT_CONNECTING;
    }

    private void checkBluetoothReady() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter == null)
            sendUpdateStatusBroadcast(ERROR, "no bluetooth adapter");
        else if (!mBtAdapter.isEnabled())
            mBtAdapter.enable();

        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(getPrefs().getString(SoundDetailActivity.VOLUME_LOCAL, "2")), 0);
        // Establish connection to the proxy.
        mBtAdapter.getProfileProxy(this, this, BluetoothProfile.A2DP);

        //mBtAdapter.getProfileProxy(this, this, BluetoothProfile.A2DP);
    }

    private void startMediaPlayer() {
        try {
            mMediaPlayer = new MediaPlayer();
            mPlayerState = PLAYER_CONNECTING;
            sendUpdateStatusBroadcast(PREPARING);
            String stream = getPrefs().getString(STREAM_DATA_SOURCE, DEFAULT_STATION_DATA_SOURCE);
            Log.i(TAG, "play stream: " + stream);
            mMediaPlayer.setDataSource(stream); // title: 1.FM - Chillout Lounge (www.1.fm)
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepare();
        } catch (Exception ex) {
            mPlayerState = PLAYER_STOPPED;
            String msg = ex.getMessage() == null ? ex.toString() : ex.getMessage();
            Log.w(TAG, msg);
            sendUpdateStatusBroadcast(ERROR, msg);
        }
    }

    private void stopMediaPlayer() {
        mPlayerState = PLAYER_STOPPED;
        try {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
        } catch (Exception ex) {
            sendUpdateStatusBroadcast(ERROR, ex.getMessage());
        }
        sendUpdateStatusBroadcast(STOPPED);
    }

    public void onDestroy() {
        stopMediaPlayer();
        unregisterReceiver(mBCReceiver);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mPlayerState = PLAYER_PLAYING;
        mMediaPlayer.start();
        sendUpdateStatusBroadcast(PLAYING);
    }

    protected SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void sendUpdateStatusBroadcast(int status) {
        sendUpdateStatusBroadcast(status, null);
    }

    private void sendUpdateStatusBroadcast(final int status, final String txt) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent bc = new Intent();
                bc.setAction(ACTION_MUSICPLAYER_SERVICE);
                bc.putExtra(STATUS, status);
                if (txt != null)
                    bc.putExtra(TEXT, txt);
                sendBroadcast(bc);
            }
        }, 500);
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        if (profile == BluetoothProfile.A2DP) {
            mA2dpService = (BluetoothA2dp) proxy;
            String btSoundAdapterName = getSoundAdapterName();
            if (btSoundAdapterName.length() == 0) {
                String msg = "no Bluetooth adapter defined in settings";
                sendUpdateStatusBroadcast(ERROR, msg);
                Log.w(TAG, msg);
            } else {
                BluetoothDevice btDev = findBondedDeviceByName(btSoundAdapterName);
                if (btDev == null) {
                    String msg = "Kopplung mit [" + btSoundAdapterName + "] nicht gefunden";
                    sendUpdateStatusBroadcast(ERROR, msg);
                    Log.w(TAG, msg);
                } else {
                    //mAudioManager.isBluetoothScoOn()
                    // mAudioManager.setBluetoothScoOn( true);
                    // mAudioManager.startBluetoothSco();
                    // mAudioManager.setParameters(BLUETOOTH_ENABLED+"=true");
                    //mAudioManager.setParameters("A2dpSuspended=false");
                    //mAudioManager.setBluetoothA2dpOn(true);
                    //mAudioManager.setBluetoothScoOn( true);
                    // mAudioManager.startBluetoothSco();

                    Method connect = getConnectMethod();

                    try {
                        connect.setAccessible(true);
                        connect.invoke(proxy, btDev);
                        sendUpdateStatusBroadcast(INFO, btSoundAdapterName + " verbinden...");
                        // mAudioManager.setBluetoothA2dpOn(true);

                        //  boolean isA2DBPlaying = ((BluetoothA2dp) proxy).isA2dpPlaying(btDev);

                        //mAudioManager.setRouting();mBtAdapter.getProfileProxy()

                        //if (mAudioManager.isBluetoothA2dpOn())
                        //     startMediaPlayer();
                        // startPlayerWithDelay( 5);
                    } catch (InvocationTargetException ex) {
                        biz.kindler.rigi.modul.system.Log.e(TAG, "Unable to invoke connect(BluetoothDevice) method on proxy. " + ex.toString());
                        sendUpdateStatusBroadcast(ERROR, btSoundAdapterName + " Fehler");
                    } catch (IllegalAccessException ex) {
                        biz.kindler.rigi.modul.system.Log.e(TAG, "Illegal Access! " + ex.toString());
                        sendUpdateStatusBroadcast(ERROR, btSoundAdapterName + " Fehler");
                    }

                    // if( mAudioManager.isBluetoothA2dpOn())
                    // if( mAudioManager.isBluetoothScoOn())
                    //    startMediaPlayer();
                }
            }
        }
    }

    private void startPlayerWithDelay(int delaySec) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int state = mBtAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
                if (state == BluetoothProfile.STATE_CONNECTED)
                    startMediaPlayer();
                else {
                    Intent intentOpenBluetoothSettings = new Intent();
                    intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivity(intentOpenBluetoothSettings);
                }
            }
        }, delaySec * 1000);
    }

    private void showBTSettingsWithDelay(int delaySec) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intentOpenBluetoothSettings = new Intent();
                intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intentOpenBluetoothSettings);
            }
        }, delaySec * 1000);
    }

    @Override
    public void onServiceDisconnected(int profile) {
        if (profile == BluetoothProfile.A2DP) {
            mA2dpService = null;
            sendUpdateStatusBroadcast(STOPPED, getSoundAdapterName() + " gestoppt");
        }
    }

    private BluetoothDevice findBondedDeviceByName(String name) {
        for (BluetoothDevice device : mBtAdapter.getBondedDevices()) {
            if (name.matches(device.getName())) {
                Log.d(TAG, String.format("Found device with name %s and address %s.", device.getName(), device.getAddress()));
                return device;
            }
        }
        Log.w(TAG, String.format("Unable to find device with name %s.", name));
        return null;
    }

    private String getSoundAdapterName() {
        return getPrefs().getString(SoundPreferenceFragment.MYBTSOUNDADAPTER, "");
    }

    /**
     * Wrapper around some reflection code to get the hidden 'connect()' method
     *
     * @return the connect(BluetoothDevice) method, or null if it could not be found
     */
    private Method getConnectMethod() {
        try {
            return BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
        } catch (NoSuchMethodException ex) {
            biz.kindler.rigi.modul.system.Log.e(TAG, "Unable to find connect(BluetoothDevice) method in BluetoothA2dp proxy.");
            return null;
        }
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                sendUpdateStatusBroadcast(INFO, getSoundAdapterName() + " nicht verbunden");
                checkBTProfileState();
                stopMediaPlayer();
            } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                sendUpdateStatusBroadcast(INFO, getSoundAdapterName() + " verbunden");
                checkBTProfileState();
                //startPlayerWithDelay( 1);
                // startMediaPlayer();
            } else if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
                checkBTProfileState();
                //if( mBtAdapter.getProfileConnectionState (BluetoothProfile.A2DP) == BluetoothProfile.STATE_DISCONNECTED) {
            } else if (action.equals(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)) {
                checkBTProfileState();
                //if( mBtAdapter.getProfileConnectionState (BluetoothProfile.A2DP) == BluetoothProfile.STATE_DISCONNECTED) {
            }
            //  else if( action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED) ||  action.equals( BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED))
            //      checkConnectionWithDelay( 5);

            else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                checkBTEnableState();
                //   if( mBTState ==  BT_UNKNOWN || mBTState ==  BT_DISABLED ||  mBTState == BT_ENABLING)
                //      mBTState = getBTEnable() ? BT_ENABLED : BT_DISABLED;
            } else if (action.equals(Intent.ACTION_TIME_TICK))
                handleTimeTick();

            else if (action.equals(SoundModel2.ACTION_PLAYER_CMD)) {
                if (intent.hasExtra(SoundModel2.CMD_BT_ENABLE)) {
                    boolean doBTEnable = intent.getBooleanExtra(SoundModel2.CMD_BT_ENABLE, false);
                    if (mBTEnableState == BT_ENABLED && !doBTEnable) {
                        setBTEnable(false);
                        System.out.println("setBTEnable: false");
                    } else if (mBTEnableState == BT_DISABLED && doBTEnable) {
                        setBTEnable(true);
                        System.out.println("setBTEnable: true");
                    }
                }
                if (intent.hasExtra(SoundModel2.CMD)) {
                    String cmd = intent.getStringExtra(SoundModel2.CMD);
                    if (cmd.equals(SoundModel2.CMD_PLAY)) {
                        System.out.println("TODO CMD_PLAY");
                        mCmdState = CMD_PLAY;
                    } else if (cmd.equals(SoundModel2.CMD_STOP)) {
                        System.out.println("TODO CMD_STOP");
                        mCmdState = CMD_STOP;
                        stopMediaPlayer();
                    } else if( cmd.equals( SoundModel2.CMD_CHANGE_STATION)) {
                        if( mCmdState == CMD_PLAY) {
                            mCmdState = CMD_STOP;
                            stopMediaPlayer();
                            mCmdState = CMD_PLAY;
                        }
                    }
                }

            } else
                System.out.println("ACTION:" + intent.getAction());
        }
    }

    private void handleTimeTick() {
        handleQuarterWatchdog();
        startHandlerWatchdogWithDelay(15);
        startHandlerWatchdogWithDelay(30);
        startHandlerWatchdogWithDelay(45);
    }

    private void startHandlerWatchdogWithDelay(int delaySec) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handleQuarterWatchdog();
            }
        }, delaySec * 1000);
    }

    private void checkConnectionWithDelay(int delaySec) {
        if (mBtAdapter.getProfileConnectionState(BluetoothProfile.A2DP) == BluetoothProfile.STATE_DISCONNECTED) {
            sendUpdateStatusBroadcast(ERROR, getSoundAdapterName() + " verbinden nicht möglich");
        }
    }

}
