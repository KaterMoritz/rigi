package biz.kindler.rigi.settings;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;

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

    private SwitchPreference mSitzplatzSwitchPref;
    private EditTextPreference mOnTimePref;
    private EditTextPreference mOffTimePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_sitzplatz);
        setHasOptionsMenu(true);

        mSitzplatzSwitchPref = (SwitchPreference)findPreference( PLUG_TIMER_SWITCH);
        mSitzplatzSwitchPref.setOnPreferenceChangeListener(this);
        boolean sitzplatzSwitchState = mSitzplatzSwitchPref.getSharedPreferences().getBoolean( PLUG_TIMER_SWITCH, false);
        updateSummary(mSitzplatzSwitchPref, sitzplatzSwitchState, R.string.pref_plug_summary_switch_on, R.string.pref_plug_summary_switch_off);

        mOnTimePref = (EditTextPreference)findPreference( PLUG_TIMER_ON);
        mOnTimePref.setOnPreferenceChangeListener(this);
        mOnTimePref.setEnabled( sitzplatzSwitchState);
        String onTime = mOnTimePref.getSharedPreferences().getString( PLUG_TIMER_ON, "08:00");
        updateSummary(mOnTimePref, onTime, R.string.pref_plug_on_summary);

        mOffTimePref = (EditTextPreference)findPreference( PLUG_TIMER_OFF);
        mOffTimePref.setOnPreferenceChangeListener(this);
        mOffTimePref.setEnabled( sitzplatzSwitchState);
        String offTime = mOffTimePref.getSharedPreferences().getString( PLUG_TIMER_OFF, "22:00");
        updateSummary(mOffTimePref, offTime, R.string.pref_plug_off_summary);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if( preference.getKey().equals( PLUG_TIMER_SWITCH)) {
            boolean sitzplatzSwitchState = ((Boolean) newValue).booleanValue();
            updateSummary(mSitzplatzSwitchPref, sitzplatzSwitchState, R.string.pref_plug_summary_switch_on, R.string.pref_plug_summary_switch_off);
            mOnTimePref.setEnabled(sitzplatzSwitchState);
            mOffTimePref.setEnabled(sitzplatzSwitchState);
        }
        else if( preference.getKey().equals( PLUG_TIMER_ON)) {
            String onTime = (String) newValue;
            updateSummary(mOnTimePref, onTime, R.string.pref_plug_on_summary);
        }
        else if( preference.getKey().equals( PLUG_TIMER_OFF)) {
            String offTime = (String) newValue;
            updateSummary(mOffTimePref, offTime, R.string.pref_plug_off_summary);
        }

        sendSettingsChangedBroadcast( preference.getContext(), ACTION_SITZPLATZ_PLUG_SETTINGS_CHANGED,  preference.getKey(), newValue.toString());
        return true;
    }
}
