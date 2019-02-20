package biz.kindler.rigi.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.view.WindowManager;

import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 07.07.17.
 */

public class GaragePreferenceFragment extends BasePreferenceFragment {

    private final static String TAG = GaragePreferenceFragment.class.getSimpleName();

    public static final String LIGHT_MODE                       = "lightmode";
    public static final String ACTION_GARAGE_SETTINGS_CHANGED   = "garage-settings-changed";
    public static final String PLAY_SOUND                       = "playSound";

    private ListPreference  mLightModePref;
    private SwitchPreference mPlaySoundSwitchPref;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_garage);
        setHasOptionsMenu(true);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mLightModePref = (ListPreference)findPreference( LIGHT_MODE);
        mLightModePref.setOnPreferenceChangeListener(this);
        updateSummaryForLightMode(null);

        mPlaySoundSwitchPref = (SwitchPreference)findPreference( PLAY_SOUND);
        mPlaySoundSwitchPref.setOnPreferenceChangeListener(this);
        updateSummaryForPlaySoundSwitchState(null);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(LIGHT_MODE)) {
            updateSummaryForLightMode(newValue);
            return true;
        }
        else if (preference.getKey().equals(PLAY_SOUND)) {
            updateSummaryForPlaySoundSwitchState(newValue);
            return true;
        }

        sendSettingsChangedBroadcast( preference.getContext(), ACTION_GARAGE_SETTINGS_CHANGED,  preference.getKey(), newValue.toString());

        return false;
    }

    private void updateSummaryForLightMode( Object newValue) {
        String lightmode = "";
        if( newValue == null)
            lightmode = mLightModePref.getSharedPreferences().getString( LIGHT_MODE, "auto2");
        else if( newValue instanceof String)
            lightmode = ((String)newValue).toString();

        mLightModePref.setSummary( getLightModeSummaryText(lightmode));
    }

    private String getLightModeSummaryText( String lightmode) {
        String[] data = getContext().getResources().getStringArray( R.array.lightmode_values);
        if( lightmode.equals( data[0]))
            return "Manuell ein und ausschalten";
        else if( lightmode.equals( data[1]))
            return "Schaltet Licht ein wenn Garage offen ist";
        else if( lightmode.equals( data[2]))
            return "Schaltet Licht ein wenn Garage offen ist und Dämmerung oder Dunkelheit";
        else
            return "?";
    }

    private void updateSummaryForPlaySoundSwitchState( Object newValue) {
        boolean switchState = false;
        if( newValue == null)
            switchState = mPlaySoundSwitchPref.getSharedPreferences().getBoolean( PLAY_SOUND, true);
        else if( newValue instanceof Boolean)
            switchState = ((Boolean)newValue).booleanValue();

        updateBooleanPreference( PLAY_SOUND, switchState);
        mPlaySoundSwitchPref.setSummary( switchState ? R.string.pref_playsound_on__summary : R.string.pref_playsound_off_summary);
    }
}
