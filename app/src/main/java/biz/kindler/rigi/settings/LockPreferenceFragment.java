package biz.kindler.rigi.settings;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.WindowManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.lock.LockModel;

/**
 * Created by P.Kindler
 * patrick.kindler@schindler.com (kindlepa)
 * TMG PORT Technology
 * 12.01.21
 */
public class LockPreferenceFragment extends BasePreferenceFragment {

    private final static String TAG = LockPreferenceFragment.class.getSimpleName();

    public static final String NUKI_SWITCH = "nuki_switch";
    public static final String NUKI_BRIDGE_IP = "nuki_bridge_ip";
    public static final String NUKI_ID = "nuki_id";
    public static final String NUKI_BRIDGE_TOKEN = "nuki_bridge_token";
    public static final String NUKI_BRIDGE_STATUS = "nuki_bridge_status";
    public static final String LOCK_AND_GO_VOICE_SWITCH = "lockngo_voice_switch";

    private static final String NUKI_BRIDGE_IP_DEFAULT = "192.X.X.X";
    private static final String NUKI_BRIDGE_TOKEN_DEFAULT = "XXXXXX";
    private static final String NUKI_ID_DEFAULT = "000000000";

    private Preference          mNukiBridgeStatusPref;
    private EditTextPreference  mNukiBridgeIpPref;
    private EditTextPreference  mNukiBridgeTokenPref;
    private EditTextPreference  mNukiIdPref;
    private SwitchPreference    mNukiSwitchPref;
    private SwitchPreference    mNukiLGVoicePref;
    private RequestQueue        mVolleyRequestQueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_lock);
        setHasOptionsMenu(true);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mNukiSwitchPref = (SwitchPreference)findPreference( NUKI_SWITCH);
        mNukiSwitchPref.setOnPreferenceClickListener(this);
        mNukiSwitchPref.setOnPreferenceChangeListener(this);
        mNukiSwitchPref.setSummary( getNukiSwitch() ? "ein" : "aus");

        mNukiBridgeIpPref = (EditTextPreference)findPreference( NUKI_BRIDGE_IP);
        mNukiBridgeIpPref.setOnPreferenceClickListener(this);
        mNukiBridgeIpPref.setOnPreferenceChangeListener(this);
        mNukiBridgeIpPref.setSummary( getNukiBridgeIp());

        mNukiBridgeTokenPref = (EditTextPreference)findPreference( NUKI_BRIDGE_TOKEN);
        mNukiBridgeTokenPref.setOnPreferenceClickListener(this);
        mNukiBridgeTokenPref.setOnPreferenceChangeListener(this);
        mNukiBridgeTokenPref.setSummary( getNukiBridgeToken());

        mNukiIdPref = (EditTextPreference)findPreference( NUKI_ID);
        mNukiIdPref.setOnPreferenceClickListener(this);
        mNukiIdPref.setOnPreferenceChangeListener(this);
        mNukiIdPref.setSummary( getNukiId());

        mNukiLGVoicePref = (SwitchPreference)findPreference( LOCK_AND_GO_VOICE_SWITCH);
        mNukiLGVoicePref.setOnPreferenceClickListener(this);
        mNukiLGVoicePref.setOnPreferenceChangeListener(this);
        mNukiLGVoicePref.setSummary( getLGVoiceSwitch() ? "ein" : "aus");

        mNukiBridgeStatusPref = findPreference(NUKI_BRIDGE_STATUS);
        mNukiBridgeStatusPref.setOnPreferenceClickListener(this);



        mVolleyRequestQueue = Volley.newRequestQueue(getContext());
        requestInfoFromBridgeWithDelay();
    }

    private void requestInfoFromBridgeWithDelay() {
        mNukiBridgeStatusPref.setSummary("checking...");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestInfoFromBridge();
            }
        }, 1000);
    }

    private void requestInfoFromBridge() {
        InfoFromBridgeCallback infoFromBridgeCallback = new InfoFromBridgeCallback();
        String cmd = String.format("http://%1$s:8080/info?token=%2$s", getNukiBridgeIp(), getNukiBridgeToken());
        mVolleyRequestQueue.add(new JsonObjectRequest(Request.Method.GET, cmd, null, infoFromBridgeCallback, infoFromBridgeCallback));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(NUKI_BRIDGE_STATUS))
            requestInfoFromBridgeWithDelay();
        else if (preference.getKey().equals(NUKI_SWITCH))
            mNukiSwitchPref.setSummary(getNukiSwitch() ? "ein" : "aus");
        else if (preference.getKey().equals(NUKI_BRIDGE_IP))
            mNukiBridgeIpPref.setSummary(getNukiBridgeIp());
        else if (preference.getKey().equals(NUKI_BRIDGE_TOKEN))
            mNukiBridgeTokenPref.setSummary(getNukiBridgeToken());
        else if (preference.getKey().equals(NUKI_ID))
            mNukiIdPref.setSummary(getNukiId());
        else if (preference.getKey().equals(LOCK_AND_GO_VOICE_SWITCH))
            mNukiLGVoicePref.setSummary(getLGVoiceSwitch() ? "ein" : "aus");
        else
            return false;

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        super.onPreferenceChange(preference, newValue);

        if (preference.getKey().equals(NUKI_SWITCH)) {
            mNukiSwitchPref.getSharedPreferences().edit().putBoolean(NUKI_SWITCH, (Boolean)newValue).apply();
            requestInfoFromBridgeWithDelay();
        }
        else if (preference.getKey().equals(NUKI_BRIDGE_IP)) {
            mNukiBridgeIpPref.getSharedPreferences().edit().putString(NUKI_BRIDGE_IP, (String) newValue).apply();
            requestInfoFromBridgeWithDelay();
        }
        else if (preference.getKey().equals(NUKI_BRIDGE_TOKEN)) {
            mNukiBridgeTokenPref.getSharedPreferences().edit().putString(NUKI_BRIDGE_TOKEN, (String) newValue).apply();
            requestInfoFromBridgeWithDelay();
        }
        else if (preference.getKey().equals(NUKI_ID)) {
            mNukiIdPref.getSharedPreferences().edit().putString(NUKI_ID, (String) newValue).apply();
        }
        else if (preference.getKey().equals(LOCK_AND_GO_VOICE_SWITCH)) {
            mNukiLGVoicePref.getSharedPreferences().edit().putBoolean(LOCK_AND_GO_VOICE_SWITCH, (Boolean)newValue).apply();
        }

        return onPreferenceClick(preference);
    }

    private boolean getNukiSwitch() {
        return mNukiSwitchPref.getSharedPreferences().getBoolean(NUKI_SWITCH, true);
    }

    private boolean getLGVoiceSwitch() {
        return mNukiLGVoicePref.getSharedPreferences().getBoolean(LOCK_AND_GO_VOICE_SWITCH, true);
    }

    private String getNukiBridgeIp() {
        String val =  mNukiBridgeIpPref.getSharedPreferences().getString(NUKI_BRIDGE_IP, null);
        return val == null ? NUKI_BRIDGE_IP_DEFAULT : val;
    }

    private String getNukiBridgeToken() {
        String val =  mNukiBridgeTokenPref.getSharedPreferences().getString(NUKI_BRIDGE_TOKEN, null);
        return val == null ? NUKI_BRIDGE_TOKEN_DEFAULT : val;
    }

    private String getNukiId() {
        String val =  mNukiIdPref.getSharedPreferences().getString(NUKI_ID, null);
        return val == null ? NUKI_ID_DEFAULT : val;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class InfoFromBridgeCallback implements Response.Listener<JSONObject>, Response.ErrorListener {
        // {
        //	"bridgeType": 2,
        //	"currentTime": "2021-01-06T14:21:00Z",
        //	"ids": {
        //		"serverId": XXXXXXXXXX
        //	},
        //	"scanResults": [
        //		{
        //			"name": "Nuki_XXXXXXX",
        //			"nukiId": XXXXXXXXX,
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
        public void onErrorResponse(VolleyError error) {
            String errMsg;
            if( error instanceof AuthFailureError)
                errMsg = "AuthFailureError";
            else
                errMsg = error == null ? "VolleyError" : (error.getMessage() == null ? "unknown failure" : error.getMessage());
            updateSummaryText(errMsg);
        }

        @Override
        public void onResponse(JSONObject response) {
            try {
                boolean bridgeConnected = response.getBoolean("serverConnected");
                long uptime = response.getLong("uptime");
                JSONObject versionObj = (JSONObject) response.get("versions");
                boolean bridgeHW = versionObj.has("firmwareVersion");
                String version = bridgeHW ? versionObj.getString("firmwareVersion") : versionObj.getString("appVersion");
                String infoString = "NUKI Bridge " + (bridgeHW ? " Hardware " : " Android ") + (bridgeConnected ? "connected" : "NOT connected") + ", version: " + version + ", uptime: " + toReadableString(uptime);
                updateSummaryText(infoString);
            } catch (Exception ex) {
                updateSummaryText(ex.getMessage());
            }
        }

        private void updateSummaryText(final String txt) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mNukiBridgeStatusPref.setSummary(txt);
                }
            });
        }

        private String toReadableString(long uptimeSeconds) {
            long uptimeMinutes = uptimeSeconds / 60;
            if (uptimeMinutes < 60)
                return uptimeMinutes + " Min";
            else if (uptimeMinutes < 1440)  // 24 x 60
                return uptimeMinutes / 60 + " Std";
            else
                return uptimeMinutes / 60 / 24 + " Tage";
        }
    }
}
