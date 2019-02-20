package biz.kindler.rigi.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.view.WindowManager;

import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 23.06.17.
 */

public class WateringPreferenceFragment extends BasePreferenceFragment {

    private final static String TAG         = WateringPreferenceFragment.class.getSimpleName();

    public static final String              ACTION_WATERING_SETTINGS_CHANGED = "watering-settings-changed";
    public final static String              MANUELL_DURATION = "pref_manuell_watering_duration";

    private ListPreference                  mManDurationPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_watering);
        setHasOptionsMenu(true);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);


        mManDurationPref = (ListPreference)findPreference( MANUELL_DURATION);
        mManDurationPref.setOnPreferenceChangeListener(this);
        updateSummaryForManDuration(null);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        super.onPreferenceChange(preference, newValue);
        String key = preference.getKey();
        if (key.equals(MANUELL_DURATION)) {
            updateSummaryForManDuration(newValue.toString());
            sendSettingsChangedBroadcast( preference.getContext(), ACTION_WATERING_SETTINGS_CHANGED,  preference.getKey(), newValue.toString());
            return true;
        }
        return false;
    }

    private void updateSummaryForManDuration( Object newValue) {
        String manDuration = "";
        if( newValue == null)
            manDuration = mManDurationPref.getSharedPreferences().getString(MANUELL_DURATION, "");
        else if( newValue instanceof String)
            manDuration = newValue.toString();

        mManDurationPref.setValue(manDuration);

        int idx = mManDurationPref.findIndexOfValue(manDuration);
        if( idx >= 0)
            mManDurationPref.setSummary( String.valueOf(mManDurationPref.getEntries()[idx]));

    }


}
