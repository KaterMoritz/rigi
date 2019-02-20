package biz.kindler.rigi.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.shutter.ShutterModel;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 06.06.17.
 */

public class ShutterPreferenceFragment extends BasePreferenceFragment {

    private final static String 	        TAG = ShutterPreferenceFragment.class.getSimpleName();

    public static final String WAKEUP_DAYS  = "wakeup_days";
    public static final String ACTION_SHUTTER_SETTINGS_CHANGED                  = "shutter-settings-changed";

    private SwitchPreference mWakeupSwitchPref;
    private MultiSelectListPreference mWeekdaysPref;
    private EditTextPreference mWakeupTimePref;
    private EditTextPreference  mWakeupTimeStepPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_shutter);
        setHasOptionsMenu(true);

        mWakeupSwitchPref = (SwitchPreference)findPreference( ShutterModel.PREF_SHUTTER_WAKEUP_SWITCH);
        mWakeupSwitchPref.setOnPreferenceChangeListener(this);
        boolean wakeupSwitchState = mWakeupSwitchPref.getSharedPreferences().getBoolean( ShutterModel.PREF_SHUTTER_WAKEUP_SWITCH, false);
        updateSummaryForShutterWakeupSwitch( wakeupSwitchState);

        mWeekdaysPref = (MultiSelectListPreference)findPreference( WAKEUP_DAYS);
        mWeekdaysPref.setOnPreferenceChangeListener(this);
        mWeekdaysPref.setEnabled( wakeupSwitchState);
        updateSummaryForWeekdays(null);

        mWakeupTimePref = (EditTextPreference)findPreference( ShutterModel.PREF_SHUTTER_WAKEUP_TIME);
        mWakeupTimePref.setOnPreferenceChangeListener(this);
        mWakeupTimePref.setEnabled( wakeupSwitchState);
        String currTime = mWakeupTimePref.getSharedPreferences().getString( ShutterModel.PREF_SHUTTER_WAKEUP_TIME, "08:00");
        updateSummaryForShutterWakeupTime( currTime);

        mWakeupTimeStepPref = (EditTextPreference)findPreference( ShutterModel.PREF_SHUTTER_WAKEUP_TIME_STEP);
        mWakeupTimeStepPref.setOnPreferenceChangeListener(this);
        mWakeupTimeStepPref.setEnabled( wakeupSwitchState);
        String currStep = mWakeupTimeStepPref.getSharedPreferences().getString( ShutterModel.PREF_SHUTTER_WAKEUP_TIME_STEP, "10");
        updateSummaryForShutterWakeupStep( currStep);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if( preference.getKey().equals( ShutterModel.PREF_SHUTTER_WAKEUP_SWITCH)) {
            boolean wakeupSwitchState = ((Boolean)newValue).booleanValue();
            updateSummaryForShutterWakeupSwitch( wakeupSwitchState);
            mWeekdaysPref.setEnabled( wakeupSwitchState);
            mWakeupTimePref.setEnabled( wakeupSwitchState);
            mWakeupTimeStepPref.setEnabled( wakeupSwitchState);
        } else if ( preference.getKey().equals( ShutterModel.PREF_SHUTTER_WAKEUP_TIME)) {
            String newTime = (String)newValue;
            updateSummaryForShutterWakeupTime(newTime);
        } else if ( preference.getKey().equals( ShutterModel.PREF_SHUTTER_WAKEUP_TIME_STEP)) {
            String newTime = (String)newValue;
            updateSummaryForShutterWakeupStep(newTime);
        } else if ( preference.getKey().equals( WAKEUP_DAYS)) {
            updateSummaryForWeekdays(newValue);
        }

        sendSettingsChangedBroadcast( preference.getContext(), ACTION_SHUTTER_SETTINGS_CHANGED,  preference.getKey(), newValue.toString());

        return true;
    }

    private void updateSummaryForShutterWakeupSwitch( boolean switchState) {
        mWakeupSwitchPref.setSummary( getResources().getString( R.string.pref_shutter_wakeup_switch_summary) + " " + (switchState ? getResources().getString(R.string.pref_shutter_on) : getResources().getString(R.string.pref_shutter_off)));
    }

    private void updateSummaryForShutterWakeupTime( String time) {
        mWakeupTimePref.setSummary( getResources().getString( R.string.pref_shutter_wakeup_time_summary) + " um " + time + " Uhr");
    }

    private void updateSummaryForShutterWakeupStep( String time) {
        mWakeupTimeStepPref.setSummary( getResources().getString( R.string.pref_shutter_wakeup_step_time_summary) + " ist " + time + " Minuten");
    }

    private void updateSummaryForWeekdays(Object newValue) {
        List<String> selections;

        if( newValue == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            selections = new ArrayList<>(preferences.getStringSet(WAKEUP_DAYS, null));
        }
        else
            selections = new ArrayList<>((Set<String>)newValue);

        mWeekdaysPref.setSummary( getSelectionAsString( selections));
    }

    private String getSelectionAsString( List<String> selections) {
        Collections.sort(selections);
        StringBuffer sb = new StringBuffer();
        if( selections.contains( "2"))
            sb.append( "Mo, ");
        if( selections.contains( "3"))
            sb.append( "Di, ");
        if( selections.contains( "4"))
            sb.append( "Mi, ");
        if( selections.contains( "5"))
            sb.append( "Do, ");
        if( selections.contains( "6"))
            sb.append( "Fr, ");
        if( selections.contains( "7"))
            sb.append( "Sa, ");
        if( selections.contains( "1"))
            sb.append( "So, ");
        String data = sb.toString();
        if( data.length() > 0)
            return data.substring( 0, data.length() -2);
        else
            return "Keine Tage ausgewählt";
    }
}
