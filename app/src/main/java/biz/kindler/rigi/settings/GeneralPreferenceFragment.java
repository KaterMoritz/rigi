package biz.kindler.rigi.settings;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.view.MenuItem;
import android.view.WindowManager;

import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.background.BackgroundModel;
import biz.kindler.rigi.modul.entree.EntreeModel;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 06.06.17.
 */

public class GeneralPreferenceFragment extends BasePreferenceFragment {

    private final static String 	        TAG = GeneralPreferenceFragment.class.getSimpleName();

    public static final String ACTION_BACKGROUND_MODE_SETTINGS_CHANGED          = "background-mode-settings-changed";

    public static final String PREFS_DAWN_LEVEL         = "pref_dawn_level";
    public static final String PREFS_TWILIGHT_LEVEL     = "pref_twilight_level";

    public static final String OPENHAB_SERVER          = "openhab_server";
    public static final String DOOR_CAM                = "door_cam";
    private static final String SCREENSAVER_ON          = "screensaver-on";
    private static final String SCREENSAVER_OFF         = "screensaver-off";
    private static final String LOREMFLICKR_KEYWORD     = "loremflickr_keyword";

    private EditTextPreference  mOpenHabServerPref;
    private EditTextPreference  mDoorCamPref;
    private EditTextPreference  mDisplayOffPref;
    private EditTextPreference  mDawnLevelPref;
    private EditTextPreference  mTwilightLevelPref;
    private EditTextPreference  mScreensaverOnPref;
    private EditTextPreference  mScreensaverOffPref;
    private ListPreference      mBackgroundPref;
    private EditTextPreference  mLoremflickrKeywordPref;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mOpenHabServerPref = (EditTextPreference)findPreference(OPENHAB_SERVER);
        mOpenHabServerPref.setOnPreferenceChangeListener(this);
        String openHabServerIP = mOpenHabServerPref.getSharedPreferences().getString( OPENHAB_SERVER, "http://192.168.1.131:8080");
        updateSummary( mOpenHabServerPref, openHabServerIP);

        mDoorCamPref = (EditTextPreference)findPreference(DOOR_CAM);
        mDoorCamPref.setOnPreferenceChangeListener(this);
        String doorCamServerIP = mDoorCamPref.getSharedPreferences().getString( DOOR_CAM, "http://192.168.1.112/html/cam_pic.php");
        updateSummary( mDoorCamPref, doorCamServerIP);

        mDisplayOffPref = (EditTextPreference)findPreference(EntreeModel.PREFS_DISPLAY_OFF_AFTER_MINUTES);
        mDisplayOffPref.setOnPreferenceChangeListener(this);
        String displayOffState = mDisplayOffPref.getSharedPreferences().getString(EntreeModel.PREFS_DISPLAY_OFF_AFTER_MINUTES, "5");
        updateSummary(mDisplayOffPref, displayOffState, R.string.pref_display_off);

        mDawnLevelPref = (EditTextPreference)findPreference(PREFS_DAWN_LEVEL);
        mDawnLevelPref.setOnPreferenceChangeListener(this);
        String dawnLevel = mDawnLevelPref.getSharedPreferences().getString(PREFS_DAWN_LEVEL, "2000");
        updateSummary(mDawnLevelPref, dawnLevel, R.string.pref_dawn_level_summary);

        mTwilightLevelPref = (EditTextPreference)findPreference(PREFS_TWILIGHT_LEVEL);
        mTwilightLevelPref.setOnPreferenceChangeListener(this);
        String twilightLevel = mTwilightLevelPref.getSharedPreferences().getString(PREFS_TWILIGHT_LEVEL, "2000");
        updateSummary(mTwilightLevelPref, twilightLevel, R.string.pref_twilight_level_summary);

        mScreensaverOnPref = (EditTextPreference)findPreference(SCREENSAVER_ON);
        mScreensaverOnPref.setOnPreferenceChangeListener(this);
        String screensaverOnState = mScreensaverOnPref.getSharedPreferences().getString( SCREENSAVER_ON, "00:00");
        updateSummary(mScreensaverOnPref, screensaverOnState, R.string.pref_screensaver_on);

        mScreensaverOffPref = (EditTextPreference)findPreference(SCREENSAVER_OFF);
        mScreensaverOffPref.setOnPreferenceChangeListener(this);
        String screensaverOffState = mScreensaverOffPref.getSharedPreferences().getString( SCREENSAVER_OFF, "05:00");
        updateSummary(mScreensaverOffPref, screensaverOffState, R.string.pref_screensaver_off);

        mBackgroundPref = (ListPreference)findPreference(BackgroundModel.BACKGROUND_MODE);
        mBackgroundPref.setOnPreferenceChangeListener(this);
        String bgMode = mBackgroundPref.getSharedPreferences().getString(BackgroundModel.BACKGROUND_MODE, "standard");
        updateSummaryForList(mBackgroundPref, bgMode, R.array.pref_background_keys, R.array.pref_background_values);

        mLoremflickrKeywordPref = (EditTextPreference)findPreference( LOREMFLICKR_KEYWORD);
        mLoremflickrKeywordPref.setOnPreferenceChangeListener(this);
        mLoremflickrKeywordPref.setOnPreferenceClickListener(this);
        mLoremflickrKeywordPref.setEnabled( bgMode.equals( BackgroundModel.WEBRANDOM));
        String storedKeyword = mLoremflickrKeywordPref.getSharedPreferences().getString(LOREMFLICKR_KEYWORD, "cat");
        updateSummary(mLoremflickrKeywordPref, storedKeyword);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            getActivity().onBackPressed();
            //startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ( preference.getKey().equals( OPENHAB_SERVER))
            updateSummary(mOpenHabServerPref, (String)newValue);
        else if ( preference.getKey().equals( DOOR_CAM))
            updateSummary(mDoorCamPref, (String)newValue);
        else if ( preference.getKey().equals( EntreeModel.PREFS_DISPLAY_OFF_AFTER_MINUTES))
            updateSummary(mDisplayOffPref, (String)newValue, R.string.pref_display_off);
        else if ( preference.getKey().equals( PREFS_DAWN_LEVEL))
            updateSummary(mDawnLevelPref, (String)newValue, R.string.pref_dawn_level_summary);
        else if ( preference.getKey().equals( PREFS_TWILIGHT_LEVEL))
            updateSummary(mTwilightLevelPref, (String)newValue, R.string.pref_twilight_level_summary);
        else if ( preference.getKey().equals( SCREENSAVER_ON))
            updateSummary(mScreensaverOnPref, (String)newValue, R.string.pref_screensaver_on);
        else if ( preference.getKey().equals( SCREENSAVER_OFF))
            updateSummary(mScreensaverOffPref, (String)newValue, R.string.pref_screensaver_off);
        else if ( preference.getKey().equals( BackgroundModel.BACKGROUND_MODE)) {
            updateSummaryForList(mBackgroundPref, (String)newValue, R.array.pref_background_keys, R.array.pref_background_values);
            sendSettingsChangedBroadcast( preference.getContext(), ACTION_BACKGROUND_MODE_SETTINGS_CHANGED, preference.getKey(), newValue.toString());
            mLoremflickrKeywordPref.setEnabled( newValue.equals( BackgroundModel.WEBRANDOM));
        }
        else if ( preference.getKey().equals( LOREMFLICKR_KEYWORD)) {
            updateSummary(mLoremflickrKeywordPref, (String)newValue);
            sendSettingsChangedBroadcast( preference.getContext(), ACTION_BACKGROUND_MODE_SETTINGS_CHANGED, preference.getKey(), newValue.toString());
        }

        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if ( preference.getKey().equals( LOREMFLICKR_KEYWORD)) {
            mLoremflickrKeywordPref.getEditText().setSelection( mLoremflickrKeywordPref.getText().length());
            return false;
        }
        return true;
    }
}
