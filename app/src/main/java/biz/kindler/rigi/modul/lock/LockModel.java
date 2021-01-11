package biz.kindler.rigi.modul.lock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import biz.kindler.rigi.ItemManager;
import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.entree.EntreeView;
import biz.kindler.rigi.modul.vbl.VBLModel;

/**
 * Created by P.Kindler
 * patrick.kindler@schindler.com (kindlepa)
 * TMG PORT Technology
 * 05.01.21
 */
public class LockModel extends BroadcastReceiver {
    public static final String  ACTION_LOCK         = "action_lock";
  //  public static final String  ACTION_LOCK_N_GO    = "action_lock_n_go";
    public static final String  ACTION_BRIDGE       = "action_bridge";

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

    private final static String TAG = LockModel.class.getSimpleName();
    private static final String SOMEONE_MOVING  = "Bewegung_Ankleide";
    private static final String BRIDGE_IP       = "192.168.1.147";
    private static final String BRIDGE_TOKEN    = "";//
    private static final String NUKI_ID         = "";

    private Context             mCtx;
    private RequestQueue        mVolleyRequestQueue;
    private boolean             mSomeoneMoving;
    private int                 mTickCnt;
    private int                 mLockStateCnt;
    private boolean             mBridgeConnected;

    public LockModel(final Context ctx) {
        mCtx = ctx;

        mVolleyRequestQueue = Volley.newRequestQueue(ctx);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(SOMEONE_MOVING);
        intentFilter.addAction(MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.LOCK);
       // intentFilter.addAction(MainActivity.LOCK_AND_GO);

        intentFilter.addAction(ACTION_LOCK);
       // intentFilter.addAction(ACTION_LOCK_N_GO);

        mCtx.registerReceiver(this, intentFilter);

        mSomeoneMoving = true;
        requestLockStateWithDelay(15000);
    }


    private void sendStateBC(final int lockState, final String lockStateText, final int doorState, final String doorStateText, boolean someoneMoving) {
        Intent bc = new Intent();
        bc.setAction( ACTION_LOCK);
       // bc.putExtra( ItemManager2.VALUE, text);
        bc.putExtra( "lockState", lockState);
        bc.putExtra( "lockStateText", lockStateText);
        bc.putExtra( "doorState", doorState);
        bc.putExtra( "doorStateText", doorStateText);
        bc.putExtra( "someoneMoving", someoneMoving);
        mCtx.sendBroadcast(bc);
    }

    private void sendCmdResultBC(final String action, boolean success) {
        Intent bc = new Intent();
        bc.setAction( action);
        bc.putExtra( "success", success);
        mCtx.sendBroadcast(bc);
    }
/*
    private void sendCmdResultBC(final String action, boolean success, final String shortText, String longText, boolean showFailure, boolean hideIcon) {
        Intent bc = new Intent();
        bc.setAction( action);
        bc.putExtra( "success", success);
        bc.putExtra( "shortText", shortText);
        bc.putExtra( "longText", longText);
        bc.putExtra( "showFailure", showFailure);
        bc.putExtra( "hideIcon", hideIcon);
        mCtx.sendBroadcast(bc);
    } */

    private void sendLockNGoBC(final int state, final String text) {
        Intent bc = new Intent();
        bc.setAction( MainActivity.LOCK_AND_GO);
        bc.putExtra( ItemManager2.VALUE, text);
        bc.putExtra( "state", state);
        mCtx.sendBroadcast(bc);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String value = intent.getStringExtra(ItemManager2.VALUE);

        if( action.equals( Intent.ACTION_TIME_TICK))
            handleTimeTick();
        if( action.equals(SOMEONE_MOVING))
            handleSomeoneMoving(value);
        /*else if( action.equals(MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.LOCK)) {
            if( intent.getIntExtra(MainListAdapter.KEY_BUTTON_NR, -1) == 3) // 3 == Lock N Go
                handleLockNgoCmd();
        } */
        else if (action.equals(ACTION_LOCK)) {

            // TEST !!!!!!
            /*
            final int lockActionCmd = intent.getIntExtra("lockActionCmd", -1);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if( lockActionCmd == CMD_LOCK_N_GO) {
                        Intent bc = new Intent();
                        bc.setAction( ACTION_LOCK);
                        bc.putExtra("lockActionId", lockActionCmd);
                        bc.putExtra("success", false);
                        bc.putExtra( "failure", "any demo failure message");
                        mCtx.sendBroadcast(bc);
                    }
                }
            }, 5000); */

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
          //  Toast.makeText(mCtx, "MOVING ON", Toast.LENGTH_LONG).show();
        } else if (itemState.equals("OFF")) {
            mSomeoneMoving = false;
          //  sendStateBC(-1, "?", -1, "?", false);
         //   Toast.makeText(mCtx, "MOVING OFF", Toast.LENGTH_LONG).show();
        }

        bc.putExtra( "someoneMoving", mSomeoneMoving);
        mCtx.sendBroadcast(bc);
    }

    private void handleLockNgoAction() {
        doLockAction( CMD_LOCK_N_GO_WITH_UNLATCH);
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
        String cmd = String.format("http://%1$s:8080/list?token=%2$s", BRIDGE_IP, BRIDGE_TOKEN);
        mVolleyRequestQueue.add(new JsonArrayRequest(Request.Method.GET, cmd, null, lockStateCallback, lockStateCallback));
    }

    private void requestLockStateFromLock() {
        LockStateCallback lockStateCallback = new LockStateCallback();
        String cmd = String.format("http://%1$s:8080/lockState?nukiId=%2$s&token=%3$s", BRIDGE_IP, NUKI_ID, BRIDGE_TOKEN);
        mVolleyRequestQueue.add(new JsonArrayRequest(Request.Method.GET, cmd, null, lockStateCallback, lockStateCallback));
    }

    private void requestInfoFromBridge() {
        InfoFromBridgeCallback infoFromBridgeCallback = new InfoFromBridgeCallback();
        String cmd = String.format("http://%1$s:8080/info?token=%2$s", BRIDGE_IP, BRIDGE_TOKEN);
        mVolleyRequestQueue.add(new JsonObjectRequest(Request.Method.GET, cmd, null, infoFromBridgeCallback, infoFromBridgeCallback));
    }

    private void doLockAction( int lockActionId) {
        if( lockActionId > 0) {
            LockActionCallback lockActionCallback = new LockActionCallback(lockActionId);
            String cmd = String.format("http://%1$s:8080/lockAction?action=%2$s&nowait=1&nukiId=%3$s&token=%4$s", BRIDGE_IP, lockActionId, NUKI_ID, BRIDGE_TOKEN);
            mVolleyRequestQueue.add(new JsonObjectRequest(Request.Method.GET, cmd, null, lockActionCallback, lockActionCallback));
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class InfoFromBridgeCallback implements Response.Listener<JSONObject>, Response.ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            String errMsg = error == null ? "VolleyError" : (error.getMessage() == null ? "error" : error.getMessage());
            Log.w(TAG, errMsg);
          //  sendCmdResultBC( ACTION_BRIDGE, false, "BridgeInfo Error", errMsg, true, true);
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
                mBridgeConnected = response.getBoolean("serverConnected");
                long uptime = response.getLong("uptime");
                JSONObject versionObj = (JSONObject) response.get("versions");
                boolean bridgeHW = versionObj.has("firmwareVersion");
                String version = bridgeHW ? versionObj.getString( "firmwareVersion") : versionObj.getString( "appVersion");
                String infoString = "NUKI Bridge " + (bridgeHW ? " Hardware " : " Android ") + (mBridgeConnected ? "connected" : "NOT connected") + ", version: " + version + ", uptime: " + toReadableString(uptime);

                Log.i(TAG, infoString);
                sendBridgeInfo( mBridgeConnected, infoString);
                //Toast.makeText(mCtx, infoString, Toast.LENGTH_LONG).show();
              //  sendLockNGoBC(mBridgeConnected ? 1 : 0, infoString);
              //  sendCmdResultBC( ACTION_BRIDGE, true);
            } catch(Exception ex) {
                Log.w(TAG, ex);
               // sendCmdResultBC( ACTION_BRIDGE, false, "BridgeInfo Error", ex.getMessage(), true, true);
                sendBridgeInfo( false, ex.getMessage());
            }
        }

        private String toReadableString(long uptimeSeconds) {
            long uptimeMinutes = uptimeSeconds / 60;
            if(uptimeMinutes < 60)
                return uptimeMinutes + " Min";
            else if(uptimeMinutes < 1440)  // 24 x 60
                return uptimeMinutes / 60 + " Std";
            else
                return uptimeMinutes / 60 / 24 + " Tage";
        }

        private void sendBridgeInfo( boolean connected, String infoTxt) {
            Intent bc = new Intent();
            bc.setAction( ACTION_LOCK);
            bc.putExtra( "bridgeConnected", connected);
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
           // sendCmdResultBC( ACTION_LOCK, false, "LockStateError", errMsg, true, false);
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
                //Log.i(TAG, "LockActionResponse: " + success);
            } catch(Exception ex) {
                Log.w(TAG, ex);
                bc.putExtra( "failure", ex.getMessage());
                mCtx.sendBroadcast(bc);
              //  sendCmdResultBC( ACTION_LOCK, false, "LockActionResponse", ex.getMessage(), true, false);
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

          //  sendCmdResultBC( ACTION_LOCK, false, "LockActionError", errMsg, true, false);
        }
    }
}
