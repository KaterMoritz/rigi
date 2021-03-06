package biz.kindler.rigi.settings;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 06.06.17.
 */

public class SitzplatzPreferenceFragment extends BasePreferenceFragment {

    private final static String TAG         = SitzplatzPreferenceFragment.class.getSimpleName();

    public static final String ACTION_SITZPLATZ_PLUG_SETTINGS_CHANGED           = "sitzplatz-plug-settings-changed";

    public static final String PLUG_TIMER_SWITCH   = "plug-timer";
    public static final String PLUG_TIMER_ON       = "plug-on";
    public static final String PLUG_TIMER_OFF      = "plug-off";
    private static final String DEFAULT_ON_TIME    = "08:00";
    private static final String DEFAULT_OFF_TIME   = "22:00";

    private SwitchPreference mSitzplatzSwitchPref;
    private Preference mOnTimePref;
    private Preference mOffTimePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_sitzplatz);
        setHasOptionsMenu(true);

        mSitzplatzSwitchPref = (SwitchPreference)findPreference( PLUG_TIMER_SWITCH);
        mSitzplatzSwitchPref.setOnPreferenceChangeListener(this);
        boolean sitzplatzSwitchState = mSitzplatzSwitchPref.getSharedPreferences().getBoolean( PLUG_TIMER_SWITCH, false);
        updateSummary(mSitzplatzSwitchPref, sitzplatzSwitchState, R.string.pref_plug_summary_switch_on, R.string.pref_plug_summary_switch_off);

        mOnTimePref = findPreference( PLUG_TIMER_ON);
        mOnTimePref.setOnPreferenceClickListener(this);
        mOnTimePref.setEnabled( sitzplatzSwitchState);
        String onTime = getStringPreference( PLUG_TIMER_ON, DEFAULT_ON_TIME);
        updateSummary(mOnTimePref, onTime, R.string.pref_plug_on_summary);

        mOffTimePref = findPreference( PLUG_TIMER_OFF);
        mOffTimePref.setOnPreferenceClickListener(this);
        mOffTimePref.setEnabled( sitzplatzSwitchState);
        String offTime = getStringPreference( PLUG_TIMER_OFF, DEFAULT_OFF_TIME);
        updateSummary(mOffTimePref, offTime, R.string.pref_plug_off_summary);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if( preference.getKey().equals( PLUG_TIMER_ON)) {
            String[] onTime = getStringPreference( PLUG_TIMER_ON, DEFAULT_ON_TIME).split(":");
            TimePickerDialog picker = new TimePickerDialog(getContext(), timeSetFromCallback, Integer.valueOf(onTime[0]), Integer.valueOf(onTime[1]),true);
            picker.show();
            return true;
        } else if( preference.getKey().equals( PLUG_TIMER_OFF)) {
            String[] offTime = getStringPreference( PLUG_TIMER_OFF, DEFAULT_OFF_TIME).split(":");
            TimePickerDialog picker = new TimePickerDialog(getContext(), timeSetToCallback, Integer.valueOf(offTime[0]), Integer.valueOf(offTime[1]),true);
            picker.show();
            return true;
        }
        return false;
    }

    private TimePickerDialog.OnTimeSetListener timeSetFromCallback = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String newTimeFormated = String.format("%02d:%02d", hourOfDay, minute);
            updateStringPreference( PLUG_TIMER_ON, newTimeFormated);
            updateSummary(mOnTimePref, newTimeFormated, R.string.pref_plug_on_summary);
            sendSettingsChangedBroadcast( getContext(), ACTION_SITZPLATZ_PLUG_SETTINGS_CHANGED,  PLUG_TIMER_ON, newTimeFormated);
        }
    };

    private TimePickerDialog.OnTimeSetListener timeSetToCallback = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String newTimeFormated = String.format("%02d:%02d", hourOfDay, minute);
            updateStringPreference( PLUG_TIMER_OFF, newTimeFormated);
            updateSummary(mOffTimePref, newTimeFormated, R.string.pref_plug_off_summary);
            sendSettingsChangedBroadcast( getContext(), ACTION_SITZPLATZ_PLUG_SETTINGS_CHANGED,  PLUG_TIMER_OFF, newTimeFormated);
        }
    };

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if( preference.getKey().equals( PLUG_TIMER_SWITCH)) {
            boolean sitzplatzSwitchState = ((Boolean) newValue).booleanValue();
            updateSummary(mSitzplatzSwitchPref, sitzplatzSwitchState, R.string.pref_plug_summary_switch_on, R.string.pref_plug_summary_switch_off);
            mOnTimePref.setEnabled(sitzplatzSwitchState);
            mOffTimePref.setEnabled(sitzplatzSwitchState);
        }

        sendSettingsChangedBroadcast( preference.getContext(), ACTION_SITZPLATZ_PLUG_SETTINGS_CHANGED,  preference.getKey(), newValue.toString());
        return true;
    }
}
