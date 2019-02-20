package biz.kindler.rigi.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import java.util.Set;

import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.misc.MiscModel;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 06.06.17.
 */

public class LightPreferenceFragment extends BasePreferenceFragment {

    private final static String 	        TAG = LightPreferenceFragment.class.getSimpleName();

   // public static final String ACTION_DAYLIGHT_FOR_SENSOR_SETTINGS_CHANGED      = "daylight-for-sensor-settings-changed";

    public final static String 	        PREF_NIGHTMODE_OG_SWITCH    = "pref_nightmode_og_switch";
    public final static String 	        PREF_NIGHTMODEACTION_LIST   = "pref_nightmodeaction_list";
    public final static String 	        PREF_NIGHTMODE_ON_TIME      = "pref_nightmode_on_time";
    public final static String 	        PREF_NIGHTMODE_OFF_TIME     = "pref_nightmode_off_time";
    public final static String 	        PREF_MITTELWAND_AUTO_SWITCH = "pref_mittelwandplug_auto_switch";
    public final static String 	        PREF_MITTELWAND_ON_DURATION = "pref_mittelwandplug_on_duration";

    private SwitchPreference                mDaylightSwitchPref;
    private SwitchPreference                mNightmodeOGSwitchPref;
    private MultiSelectListPreference       mNightmodeActionListPref;
    private EditTextPreference              mNightmodeOnPref;
    private EditTextPreference              mNightmodeOffPref;
    private SwitchPreference                mMittelwandAutoSwitchPref;
    private EditTextPreference              mMittelwandOnDurationPref;

    // private EditTextPreference mDaylightLevelPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_light);
        setHasOptionsMenu(true);

        mDaylightSwitchPref = (SwitchPreference)findPreference( MiscModel.PREF_DAYLIGHT_FOR_KITCHEN_SENSOR_SWITCH);
        mDaylightSwitchPref.setOnPreferenceChangeListener(this);
        boolean daylightSwitchState = mDaylightSwitchPref.getSharedPreferences().getBoolean( MiscModel.PREF_DAYLIGHT_FOR_KITCHEN_SENSOR_SWITCH, false);
        updateSummaryForSensorDaylightSwitch( daylightSwitchState);
/*
        mDaylightLevelPref = (EditTextPreference)findPreference( WeatherstationModel.PREF_LEVEL_DAYLIGHT_FOR_SENSOR);
        mDaylightLevelPref.setOnPreferenceChangeListener(this);
        String level = mDaylightLevelPref.getSharedPreferences().getString( WeatherstationModel.PREF_LEVEL_DAYLIGHT_FOR_SENSOR, "1000");
        mDaylightLevelPref.setEnabled( daylightSwitchState);
        updateSummaryForLevelDaylight( level); */

        mNightmodeOGSwitchPref = (SwitchPreference)findPreference( PREF_NIGHTMODE_OG_SWITCH);
        mNightmodeOGSwitchPref.setOnPreferenceChangeListener(this);
        boolean nightmodeSwitchState = mNightmodeOGSwitchPref.getSharedPreferences().getBoolean( PREF_NIGHTMODE_OG_SWITCH, false);
        updateSummaryForNightmodeSwitch( nightmodeSwitchState);

        mNightmodeActionListPref = (MultiSelectListPreference)findPreference( PREF_NIGHTMODEACTION_LIST);
        mNightmodeActionListPref.setOnPreferenceChangeListener(this);
        mNightmodeActionListPref.setEnabled( nightmodeSwitchState);
        updateSummaryForNightmodeAction( null);

        mNightmodeOnPref = (EditTextPreference)findPreference( PREF_NIGHTMODE_ON_TIME);
        mNightmodeOnPref.setOnPreferenceChangeListener(this);
        mNightmodeOnPref.setEnabled( nightmodeSwitchState);
        String nightmodeOnTime = mNightmodeOnPref.getSharedPreferences().getString( PREF_NIGHTMODE_ON_TIME, "21:00");
        updateSummaryForNightmodeOnTime( nightmodeOnTime);

        mNightmodeOffPref = (EditTextPreference)findPreference( PREF_NIGHTMODE_OFF_TIME);
        mNightmodeOffPref.setOnPreferenceChangeListener(this);
        mNightmodeOffPref.setEnabled( nightmodeSwitchState);
        String nightmodeOffTime = mNightmodeOnPref.getSharedPreferences().getString( PREF_NIGHTMODE_OFF_TIME, "08:00");
        updateSummaryForNightmodeOffTime( nightmodeOffTime);

        mMittelwandAutoSwitchPref = (SwitchPreference)findPreference( PREF_MITTELWAND_AUTO_SWITCH);
        mMittelwandAutoSwitchPref.setOnPreferenceChangeListener(this);
        boolean mittelwandAutoSwitchState = mMittelwandAutoSwitchPref.getSharedPreferences().getBoolean( PREF_MITTELWAND_AUTO_SWITCH, false);

        mMittelwandOnDurationPref = (EditTextPreference)findPreference( PREF_MITTELWAND_ON_DURATION);
        mMittelwandOnDurationPref.setOnPreferenceChangeListener(this);
        mMittelwandOnDurationPref.setEnabled( mittelwandAutoSwitchState);
        String duration = mMittelwandOnDurationPref.getSharedPreferences().getString( PREF_MITTELWAND_ON_DURATION, "15");
        updateSummaryForMittelwandOnDuration( duration);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if( preference.getKey().equals( MiscModel.PREF_DAYLIGHT_FOR_KITCHEN_SENSOR_SWITCH))
            return updateSummaryForSensorDaylightSwitch(((Boolean) newValue).booleanValue());
        else if( preference.getKey().equals( PREF_NIGHTMODE_OG_SWITCH))
            return updateSummaryForNightmodeSwitch(((Boolean) newValue).booleanValue());
        else if( preference.getKey().equals( PREF_NIGHTMODEACTION_LIST))
            return updateSummaryForNightmodeAction( newValue);
        else if( preference.getKey().equals( PREF_NIGHTMODE_ON_TIME))
            return updateSummaryForNightmodeOnTime((String) newValue);
        else if( preference.getKey().equals( PREF_NIGHTMODE_OFF_TIME))
            return updateSummaryForNightmodeOffTime((String) newValue);
        else if( preference.getKey().equals( PREF_MITTELWAND_AUTO_SWITCH))
            return updateSummaryForMittelwandSwitch(((Boolean) newValue).booleanValue());
        else if( preference.getKey().equals( PREF_MITTELWAND_ON_DURATION))
            return updateSummaryForMittelwandOnDuration((String)newValue);

        /*
        else if( preference.getKey().equals( WeatherstationModel.PREF_LEVEL_DAYLIGHT_FOR_SENSOR)) {
            String daylightLevel = (String) newValue;
            updateSummaryForLevelDaylight(daylightLevel);
            return true;
        } */

       // sendSettingsChangedBroadcast( preference.getContext(), ACTION_DAYLIGHT_FOR_SENSOR_SETTINGS_CHANGED,  preference.getKey(), newValue.toString());

        return false;
    }

    private boolean updateSummaryForSensorDaylightSwitch( boolean switchState) {
        mDaylightSwitchPref.setSummary( switchState ? getResources().getString( R.string.pref_sensor_kitchen_switch_daylight_summary_switchon) :
                getResources().getString(R.string.pref_sensor_kitchen_switch_daylight_summary_switchoff));
        return true;
    }

    private boolean updateSummaryForNightmodeSwitch( boolean switchState) {
        mNightmodeOGSwitchPref.setSummary( switchState ? getResources().getString( R.string.pref_nightmode_og_switch_summary_switchon) :
                getResources().getString(R.string.pref_nightmode_og_switch_summary_switchoff));

        if( mNightmodeActionListPref != null && mNightmodeOnPref != null && mNightmodeOffPref != null) {
            mNightmodeActionListPref.setEnabled(switchState);
            mNightmodeOnPref.setEnabled(switchState);
            mNightmodeOffPref.setEnabled(switchState);
        }
        return true;
    }

    private boolean updateSummaryForNightmodeAction(Object newValue) {
        String summary = "";
        Set<String> selections = null;
        if( newValue != null)
            selections = (Set<String>)newValue;
        else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            selections = preferences.getStringSet(PREF_NIGHTMODEACTION_LIST, null);
        }

        String[] selected = selections.toArray(new String[]{});
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < selected.length; i++)
            buff.append(selected[i] + ",");

        if( buff.length() > 0)
            summary = buff.substring(0, buff.length() -1);
        else
            summary = "keine";

        mNightmodeActionListPref.setSummary(summary);
        return true;
    }

    private boolean updateSummaryForNightmodeOnTime( String nightmodeOnTime) {
        mNightmodeOnPref.setText( nightmodeOnTime);
        mNightmodeOnPref.setSummary( nightmodeOnTime);
        return true;
    }

    private boolean updateSummaryForNightmodeOffTime( String nightmodeOffTime) {
        mNightmodeOffPref.setText( nightmodeOffTime);
        mNightmodeOffPref.setSummary( nightmodeOffTime);
        return true;
    }

    private boolean updateSummaryForMittelwandSwitch( boolean mittelwandAutoSwitchState) {
        mMittelwandAutoSwitchPref.setSummary( mittelwandAutoSwitchState ? getResources().getString( R.string.pref_mittelwandplug_auto_switch_on) :
                getResources().getString(R.string.pref_mittelwandplug_auto_switch_off));

        if( mMittelwandOnDurationPref != null)
            mMittelwandOnDurationPref.setEnabled(mittelwandAutoSwitchState);

        return true;
    }

    private boolean updateSummaryForMittelwandOnDuration( String newValue) {
        mMittelwandOnDurationPref.setSummary( newValue + " Minuten");
        return true;
    }


/*
    private void updateSummaryForLevelDaylight( String lux) {
        String defaultTxt = getResources().getString( R.string.pref_daylight_level_summary).replace( "#", lux);
        mDaylightLevelPref.setSummary( defaultTxt);
    } */
}
