package biz.kindler.rigi.modul.lock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.settings.LockPreferenceFragment;
import biz.kindler.rigi.settings.SoundPreferenceFragment2;


/**
 * Created by Patrick Kindler, Switzerland
 * 05.01.21
 */
public class LockModel extends BroadcastReceiver {
    public static final String  ACTION_LOCK         = "action_lock";

    // Lock states (according: Nuki API)
    public static final int         LOCK_UNCALIBRATED       = 0;
    public static final int         LOCK_LOCKED             = 1;
    public static final int         LOCK_UNLOCKING          = 2;
    public static final int         LOCK_UNLOCKED           = 3;
    public static final int         LOCK_LOCKING            = 4;
    public static final int         LOCK_UNLATCHED          = 5;
    public static final int         LOCK_UNLOCKED_LOCKNGO   = 6;
    public static final int         LOCK_UNLATCHING         = 7;
    public static final int         LOCK_MOTOR_BLOCKED      = 254;
    public static final int         LOCK_MOTOR_UNDEFINED    = 255;

    // Doorsensor states (according: Nuki API)
    public static final int         DOOR_DEACTIVATED        = 1;
    public static final int         DOOR_CLOSED             = 2;
    public static final int         DOOR_OPENED             = 3;
    public static final int         DOOR_UNKNOWN            = 4;
    public static final int         DOOR_CALIBRATING        = 5;

    // Lock Actions
    public static final int         CMD_UNLOCK              = 1;
    public static final int         CMD_LOCK                = 2;
    public static final int         CMD_UNLATCH             = 3;
    public static final int         CMD_LOCK_N_GO           = 4;
    public static final int         CMD_LOCK_N_GO_WITH_UNLATCH = 5;

    private static final String     VOICE_INTRO_TEXT        = "Lock and go aktiviert. 15 Sekunden verbleiben";
    private static final String     VOICE_END_TEXT          = "Auf Wiedersehen";
    private static final String     VOICE_REMAINING_UNIT    = "Sekunden";

    private final static String TAG = LockModel.class.getSimpleName();
    private static final String SOMEONE_MOVING  = "Bewegung_Ankleide";

    private Context             mCtx;
    private SharedPreferences   mPrefs;
    private RequestQueue        mVolleyRequestQueue;
    private boolean             mSomeoneMoving;
    private int                 mTickCnt;
    private int                 mLockStateCnt;
    private boolean             mBridgeConnected;
    private boolean             mTtsReady;
    private TextToSpeech        mTts;

    public LockModel(final Context ctx) {
        mCtx = ctx;

        mVolleyRequestQueue = Volley.newRequestQueue(ctx);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(SOMEONE_MOVING);
        intentFilter.addAction(MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.LOCK);
        intentFilter.addAction(ACTION_LOCK);
        mCtx.registerReceiver(this, intentFilter);

        if ( getNukiSwitch()) {
            mSomeoneMoving = true;
            requestLockStateWithDelay(15000);
        }

        mTts = new TextToSpeech(mCtx, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                mTtsReady = status != TextToSpeech.ERROR;
            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String value = intent.getStringExtra(ItemManager2.VALUE);

        if ( getNukiSwitch()) {
            if (action.equals(Intent.ACTION_TIME_TICK))
                handleTimeTick();
            if (action.equals(SOMEONE_MOVING))
                handleSomeoneMoving(value);
            else if (action.equals(ACTION_LOCK))
                doLockAction(intent.getIntExtra("lockActionCmd", -1));
        }
    }

    private void handleTimeTick() {
        if(mTickCnt == 0) {
            requestInfoFromBridge();
        } else if( mTickCnt >= 15 || ! mBridgeConnected) {
            mTickCnt = -1;
        }
        mTickCnt++;
    }

    private void handleSomeoneMoving(String itemState) {
        Intent bc = new Intent();
        bc.setAction( ACTION_LOCK);

        if (itemState.equals("ON")) {
            mSomeoneMoving = true;
            mLockStateCnt = 0;
            requestLockStateWithDelay(0);
        } else if (itemState.equals("OFF")) {
            mSomeoneMoving = false;
        }

        bc.putExtra( "someoneMoving", mSomeoneMoving);
        mCtx.sendBroadcast(bc);
    }

    private void requestLockStateWithDelay(int delayMs) {
        mLockStateCnt++;
        if( mLockStateCnt > 400) {
            System.out.println( "requestLockState more than 400 times without a moving off event: -> set mSomeoneMoving = false ");
            mSomeoneMoving = false;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               // requestLockStateFromLock();
                requestLockStateCachedFromBridge();
            }
        }, delayMs);
    }

    private void requestLockStateCachedFromBridge() {
        LockStateCallback lockStateCallback = new LockStateCallback();
        String cmd = String.format("http://%1$s:8080/list?token=%2$s", getBridgeIp(), getBridgeToken());
        mVolleyRequestQueue.add(new JsonArrayRequest(Request.Method.GET, cmd, null, lockStateCallback, lockStateCallback));
    }

    private void requestLockStateFromLock() {
        LockStateCallback lockStateCallback = new LockStateCallback();
        String cmd = String.format("http://%1$s:8080/lockState?nukiId=%2$s&token=%3$s", getBridgeIp(), getNukiId(), getBridgeToken());
        mVolleyRequestQueue.add(new JsonArrayRequest(Request.Method.GET, cmd, null, lockStateCallback, lockStateCallback));
    }

    private void requestInfoFromBridge() {
        InfoFromBridgeCallback infoFromBridgeCallback = new InfoFromBridgeCallback();
        String cmd = String.format("http://%1$s:8080/info?token=%2$s", getBridgeIp(), getBridgeToken());
        mVolleyRequestQueue.add(new JsonObjectRequest(Request.Method.GET, cmd, null, infoFromBridgeCallback, infoFromBridgeCallback));
    }

    private String getBridgeIp() {
        return getPrefs().getString(LockPreferenceFragment.NUKI_BRIDGE_IP, "0.0.0.0");
    }

    private String getBridgeToken() {
        return getPrefs().getString(LockPreferenceFragment.NUKI_BRIDGE_TOKEN, "");
    }

    private String getNukiId() {
        return getPrefs().getString(LockPreferenceFragment.NUKI_ID, "");
    }

    private boolean getNukiSwitch() {
        return getPrefs().getBoolean(LockPreferenceFragment.NUKI_SWITCH, false);
    }

    private boolean getLockAndGoVoiceSwitch() {
        return getPrefs().getBoolean(LockPreferenceFragment.LOCK_AND_GO_VOICE_SWITCH, false);
    }

    private void doLockAction( int lockActionId) {
        if( lockActionId > 0) {
            LockActionCallback lockActionCallback = new LockActionCallback(lockActionId);
            String cmd = String.format("http://%1$s:8080/lockAction?action=%2$s&nowait=1&nukiId=%3$s&token=%4$s", getBridgeIp(), lockActionId, getNukiId(), getBridgeToken());
            mVolleyRequestQueue.add(new JsonObjectRequest(Request.Method.GET, cmd, null, lockActionCallback, lockActionCallback));
        }
    }

    private void handleLockAndGoActivated() {
        if( mTtsReady && getLockAndGoVoiceSwitch()) {

            String volume = PreferenceManager.getDefaultSharedPreferences(mCtx).getString(LockPreferenceFragment.TTS_VOLUME, "0.5");
            final Bundle param = new Bundle();
            param.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, Float.parseFloat(volume));

            mTts.speak(VOICE_INTRO_TEXT, TextToSpeech.QUEUE_FLUSH, param, null);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new CountDownTimer(11000, 5000) {
                        public void onTick(long millisUntilFinished) {
                            int remainingTime = new Long(millisUntilFinished / 1000).intValue();
                            if (remainingTime != 0 && remainingTime != 1)
                                mTts.speak(remainingTime + " " + VOICE_REMAINING_UNIT, TextToSpeech.QUEUE_FLUSH, param, null);
                        }

                        public void onFinish() {
                            mTts.speak(VOICE_END_TEXT, TextToSpeech.QUEUE_FLUSH, param, null);
                        }
                    }.start();
                }
            }, 8000);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class InfoFromBridgeCallback implements Response.Listener<JSONObject>, Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            String errMsg;
            if( error instanceof AuthFailureError)
                errMsg = "AuthFailureError";
            else
                errMsg = error == null ? "VolleyError" : (error.getMessage() == null ? "error" : error.getMessage());
            Log.w(TAG, errMsg);
            sendBridgeInfo( false, errMsg);
        }

        // {
        //	"bridgeType": 2,
        //	"currentTime": "2021-01-06T14:21:00Z",
        //	"ids": {
        //		"serverId": 1650965841
        //	},
        //	"scanResults": [
        //		{
        //			"name": "Nuki_234EFB2D",
        //			"nukiId": 592378669,
        //			"paired": true,
        //			"rssi": -62
        //		}
        //	],
        //	"serverConnected": true,
        //	"uptime": 86725,
        //	"versions": {
        //		"appVersion": "1.4.6"
        //	}
        //}
        @Override
        public void onResponse(JSONObject response) {
            try {
                mBridgeConnected = response.getBoolean("serverConnected");;
                Log.i(TAG, "BridgeConnected: " + mBridgeConnected);
                sendBridgeInfo( mBridgeConnected, null);
            } catch(Exception ex) {
                Log.w(TAG, ex);
                sendBridgeInfo( false, ex.getMessage());
            }
        }

        private void sendBridgeInfo( boolean connected, String infoTxt) {
            Intent bc = new Intent();
            bc.setAction( ACTION_LOCK);
            bc.putExtra( "bridgeConnected", connected);
            if( infoTxt != null)
                bc.putExtra( "info", infoTxt);
            mCtx.sendBroadcast(bc);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class LockStateCallback implements Response.Listener<JSONArray>, Response.ErrorListener {

        private void handleLockStateError(VolleyError error) {
            Intent bc = new Intent();
            bc.setAction( ACTION_LOCK);
            bc.putExtra( "success", false);

            String errMsg;
            if( error instanceof TimeoutError)
                errMsg = "timeout";
            else
                errMsg = error == null ? "VolleyError" : (error.getMessage() == null ? "error" : error.getMessage());

            Log.w(TAG, errMsg);
            bc.putExtra( "failure", errMsg);
            mCtx.sendBroadcast(bc);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            handleLockStateError(error);
            if( mSomeoneMoving == true)
                requestLockStateWithDelay(5000);
        }

        // { "batteryCritical": false, "state": 3,"stateName": "unlocked", "success": true }
        @Override
        public void onResponse(JSONArray response) {
            Intent bc = new Intent();
            bc.setAction( ACTION_LOCK);
            bc.putExtra( "success", false);
            try {
                if( response.length() == 0) {
                    bc.putExtra( "failure", "check connection from bridge to Nuki lock");
                    mCtx.sendBroadcast(bc);
                } else {
                    JSONObject firstNukiLockObj = (JSONObject) response.get(0);
                    JSONObject lockStateObj = firstNukiLockObj.getJSONObject("lastKnownState");

                    bc.putExtra( "success", true);
                    bc.putExtra( "lockState", lockStateObj.getInt("state"));
                    bc.putExtra( "lockStateText", lockStateObj.getString("stateName"));
                    bc.putExtra( "doorState", lockStateObj.getInt("doorsensorState"));
                    bc.putExtra( "doorStateText", lockStateObj.getString("doorsensorStateName"));
                    bc.putExtra( "someoneMoving", mSomeoneMoving);
                    bc.putExtra( "lockStateCnt", mLockStateCnt); // just as logging info
                    mCtx.sendBroadcast(bc);
                }
            } catch(Exception ex) {
                Log.w(TAG, ex);
                bc.putExtra( "failure", ex.getMessage());
                mCtx.sendBroadcast(bc);
            }
            if( mSomeoneMoving == true) {
                requestLockStateWithDelay(5000);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class LockActionCallback implements Response.Listener<JSONObject>, Response.ErrorListener {
        private int lockActionId;

        public LockActionCallback( int lockActionId) {
            this.lockActionId = lockActionId;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            handleLockActionError(error);
        }

        @Override
        public void onResponse(JSONObject response) {
            Intent bc = new Intent();
            bc.setAction( ACTION_LOCK);
            bc.putExtra( "lockActionId", lockActionId);
            bc.putExtra( "success", false);

            try {
                bc.putExtra( "success", response.getBoolean("success"));
                mCtx.sendBroadcast(bc);
                if( lockActionId == CMD_LOCK_N_GO || lockActionId == CMD_LOCK_N_GO_WITH_UNLATCH)
                    handleLockAndGoActivated();
            } catch(Exception ex) {
                Log.w(TAG, ex);
                bc.putExtra( "failure", ex.getMessage());
                mCtx.sendBroadcast(bc);
            }
        }

        private void handleLockActionError(VolleyError error) {
            String errMsg;
            if( error instanceof TimeoutError)
                errMsg = "timeout";
            else
                errMsg = error == null ? "VolleyError" : (error.getMessage() == null ? "error" : error.getMessage());

            Log.w(TAG, errMsg);

            Intent bc = new Intent();
            bc.setAction( ACTION_LOCK);
            bc.putExtra( "lockActionId", lockActionId);
            bc.putExtra( "failure", errMsg);
            mCtx.sendBroadcast(bc);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    protected SharedPreferences getPrefs() {
        if( mPrefs == null)
            mPrefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
        // mPrefs = mCtx.getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE);
        return mPrefs;
    }
}
