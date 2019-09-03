package biz.kindler.rigi.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import biz.kindler.rigi.R;
import biz.kindler.rigi.Util;
import biz.kindler.rigi.modul.background.BackgroundModel;
import biz.kindler.rigi.modul.entree.EntreeModel;
import biz.kindler.rigi.modul.system.Log;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 06.06.17.
 */

public class GeneralPreferenceFragment extends BasePreferenceFragment {

    private final static String 	        TAG = GeneralPreferenceFragment.class.getSimpleName();

    public static final String ACTION_BACKGROUND_MODE_SETTINGS_CHANGED          = "background-mode-settings-changed";

    private static final String SERVER_BASE_CONFIG_URL_JSON                     = "http://www.kindler.biz/rigi/";

    public static final String PREFS_DAWN_LEVEL         = "pref_dawn_level";
    public static final String PREFS_TWILIGHT_LEVEL     = "pref_twilight_level";

    public static final String CONFIG_FROM_SERVER       = "config_from_server";
    public static final String OPENHAB_SERVER           = "openhab_server";
    private static final String SCREENSAVER_ON          = "screensaver-on";
    private static final String SCREENSAVER_OFF         = "screensaver-off";

    private EditTextPreference  mConfigFromServerPref;
    private EditTextPreference  mOpenHabServerPref;
    private EditTextPreference  mDisplayOffPref;
    private EditTextPreference  mDawnLevelPref;
    private EditTextPreference  mTwilightLevelPref;
    private EditTextPreference  mScreensaverOnPref;
    private EditTextPreference  mScreensaverOffPref;
    private EditTextPreference  mBackgroundPref;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(false);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
      //  getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getActivity().setTitle("");

        mConfigFromServerPref = (EditTextPreference)findPreference(CONFIG_FROM_SERVER);
        mConfigFromServerPref.setOnPreferenceChangeListener(this);
        String configFromServerPath = mConfigFromServerPref.getSharedPreferences().getString( CONFIG_FROM_SERVER, "");
        updateSummary( mConfigFromServerPref, configFromServerPath);

        mOpenHabServerPref = (EditTextPreference)findPreference(OPENHAB_SERVER);
        mOpenHabServerPref.setOnPreferenceChangeListener(this);
        String openHabServerIP = mOpenHabServerPref.getSharedPreferences().getString( OPENHAB_SERVER, "http://192.168.1.131:8080");
        updateSummary( mOpenHabServerPref, openHabServerIP);

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

        mBackgroundPref = (EditTextPreference)findPreference(BackgroundModel.BACKGROUND_CAMURL);
        mBackgroundPref.setOnPreferenceChangeListener(this);
        String backgroundCamUrl = mBackgroundPref.getSharedPreferences().getString( BackgroundModel.BACKGROUND_CAMURL, "https://rigipic.ch/rigikapellekulm.jpg");
        updateSummary(mBackgroundPref, backgroundCamUrl);
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
        if ( preference.getKey().equals( CONFIG_FROM_SERVER)) {
            updateSummary(mConfigFromServerPref, (String) newValue);
            loadConfigFromServer( mConfigFromServerPref.getSharedPreferences().getString( CONFIG_FROM_SERVER, ""));
        }
        else if ( preference.getKey().equals( OPENHAB_SERVER))
            updateSummary(mOpenHabServerPref, (String)newValue);
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
        else if ( preference.getKey().equals( BackgroundModel.BACKGROUND_CAMURL)) {
            updateSummary(mBackgroundPref, (String)newValue);
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return true;
    }

    private void loadConfigFromServer(final String path) {
        Util.showToastInUiThread( getContext(), "load from: " + path, Toast.LENGTH_LONG);
        new Thread(new Runnable(){
            public void run(){
                try {
                    URL url = new URL(SERVER_BASE_CONFIG_URL_JSON + path + ".json"); // the config file
                    HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(60000); // timing out in a minute
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String str;
                    StringBuffer buff= new StringBuffer();
                    while ((str = in.readLine()) != null) {
                        buff.append(str);
                    }
                    in.close();

                    JSONObject dataArr = new JSONObject(buff.toString());

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor prefsEditor = prefs.edit();

                    Iterator iterator = dataArr.keys();
                    while (iterator.hasNext()) {
                        String key = (String) iterator.next();
                        String value = dataArr.getString( key);
                        Log.d(TAG, "key:" + key + ",val:" + value);
                        prefsEditor.putString( key, value);
                    }
                    prefsEditor.apply();
                    Util.showToastInUiThread(getContext(), dataArr.length() + " Einstellungen geladen", Toast.LENGTH_LONG);
                } catch (FileNotFoundException e) {
                    Util.showToastInUiThread(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                } catch (Exception e) {
                    Util.showToastInUiThread(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                }
            }
        }).start();
    }
}
