package biz.kindler.rigi.settings;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.system.Log;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 13.08.17.
 */

public class SoundPreferenceFragment2 extends BasePreferenceFragment {

    private final static String TAG = SoundPreferenceFragment2.class.getSimpleName();

    public static final String SOUNDSWITCH          = "remote_sound_switch";
    public static final String SHOUTCAST_API_KEY    = "shoutcast_api_key";
    public static final String SOCKETSWITCH         = "socket_switch";

    private EditTextPreference  mSoundApiKeyPref;
    private SwitchPreference    mSoundDeviceSwitchPref;
    private SwitchPreference    mSocketSwitchPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_sound2);
        setHasOptionsMenu(true);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mSoundApiKeyPref = (EditTextPreference)findPreference( SHOUTCAST_API_KEY);
        mSoundApiKeyPref.setSummary( getApiKeySummary());

        mSoundDeviceSwitchPref = (SwitchPreference)findPreference(SOUNDSWITCH);
        mSoundDeviceSwitchPref.setOnPreferenceClickListener(this);
        mSoundDeviceSwitchPref.setSummary( getSoundDeviceSwitchSummary());

        mSocketSwitchPref = (SwitchPreference)findPreference(SOCKETSWITCH);
        mSocketSwitchPref.setOnPreferenceClickListener(this);
        mSocketSwitchPref.setSummary( getSocketSwitchSummary());
    }

    private String getApiKeySummary() {
        String apiKey = mSoundApiKeyPref.getText();
        return apiKey != null && apiKey.length() > 0 ? "[defined]" : "no api key";
    }

    private String getSoundDeviceSwitchSummary() {
        return mSoundDeviceSwitchPref.isChecked() ? "Google Cast" : "Geräte Lautsprecher / Kopfhörer";
    }

    private String getSocketSwitchSummary() {
        return mSocketSwitchPref.isChecked() ? "Ein und Ausschalten" : "nicht schalten";
    }

    private void setBTSoundDeviceSwitch( boolean status) {
        mSoundDeviceSwitchPref.getSharedPreferences().edit().putBoolean(SOUNDSWITCH, status).apply();
    }

    private void setSocketSwitch( boolean status) {
        mSocketSwitchPref.getSharedPreferences().edit().putBoolean(SOCKETSWITCH, status).apply();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(SOUNDSWITCH)) {
            mSoundDeviceSwitchPref.setSummary( getSoundDeviceSwitchSummary());
            setBTSoundDeviceSwitch( mSoundDeviceSwitchPref.isChecked());
        }
        else if (preference.getKey().equals(SOCKETSWITCH)) {
            mSocketSwitchPref.setSummary( getSocketSwitchSummary());
            setSocketSwitch( mSocketSwitchPref.isChecked());
        }
        else if (preference.getKey().equals(SHOUTCAST_API_KEY)) {
            mSoundApiKeyPref.setSummary( getApiKeySummary());
        }

        return true;
    }

}
