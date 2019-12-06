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
import androidx.core.app.ActivityCompat;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.calendar.CalendarModel;
import biz.kindler.rigi.modul.system.Log;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 11.01.19.
 */

public class CalendarPreferenceFragment extends BasePreferenceFragment {

    private final static String TAG = CalendarPreferenceFragment.class.getSimpleName();

    public static final String EMAIL_ACCOUNT          = "calendar_email";

    private EditTextPreference mAccountEMailPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_calendar);
        setHasOptionsMenu(true);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mAccountEMailPref = (EditTextPreference)findPreference(EMAIL_ACCOUNT);
        mAccountEMailPref.setSummary( getEmailAccountSummary());
        mAccountEMailPref.setOnPreferenceClickListener(this);
        mAccountEMailPref.setOnPreferenceChangeListener(this);
    }

    private String getEmailAccountSummary() {
        return getEmailAccount();
    }

    private String getEmailAccount() {
        String accountName = getContext().getSharedPreferences( MainActivity.PREFS_ID, Context.MODE_PRIVATE).getString(CalendarModel.PREF_ACCOUNT_NAME, "?");
        return accountName;
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(EMAIL_ACCOUNT)) {
            mAccountEMailPref.setText( getEmailAccount());
        }

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        super.onPreferenceChange(preference, newValue);
        String key = preference.getKey();
        if (key.equals(EMAIL_ACCOUNT)) {
            mAccountEMailPref.setSummary( newValue.toString());
        }
        return true;
    }

}
