package biz.kindler.rigi.settings;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.BaseModel;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 19.02.19
 */
public class PublicTransportPreferenceFragment extends BasePreferenceFragment {

    private final static String TAG = PublicTransportPreferenceFragment.class.getSimpleName();

    public static final String      API_KEY                 = "transport_api_key";
    public static final String      FROM_LOCATION           = "from_location";
    public static final String      TO_LOCATION             = "to_location";
    public static final String      TRANSPORT_REQUEST_CNT   = "transport_req_cnt";
    public static final String      TRANSPORT_REQUEST_SINCE = "transport_req_since";

    private EditTextPreference  mApiKeyPref;
    private EditTextPreference  mFromPref;
    private EditTextPreference  mToPref;
    private Preference          mInfoPref;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_transport);
        setHasOptionsMenu(true);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mApiKeyPref = (EditTextPreference)findPreference( API_KEY);
        mApiKeyPref.setOnPreferenceClickListener(this);
        mApiKeyPref.setOnPreferenceChangeListener(this);
        mApiKeyPref.setSummary( getApiKeySummary());

        mFromPref = (EditTextPreference)findPreference( FROM_LOCATION);
        mFromPref.setOnPreferenceClickListener(this);
        mFromPref.setOnPreferenceChangeListener(this);
        mFromPref.setSummary( getFromSummary());

        mToPref = (EditTextPreference)findPreference( TO_LOCATION);
        mToPref.setOnPreferenceClickListener(this);
        mToPref.setOnPreferenceChangeListener(this);
        mToPref.setSummary( getToSummary());

        mInfoPref = findPreference( TRANSPORT_REQUEST_CNT);
        mInfoPref.setSummary( "Anfragen: " + PreferenceManager.getDefaultSharedPreferences(getContext()).getString(TRANSPORT_REQUEST_CNT, "unknown") + " (seit " +
                PreferenceManager.getDefaultSharedPreferences(getContext()).getString(TRANSPORT_REQUEST_SINCE, "unknown") + ")");
    }

    private String getApiKeySummary() {
        String apiKey = getAPIKey();
        return apiKey != null && apiKey.length() > 0 ? "[defined]" : "no api key";
    }

    private String getFromSummary() {
        return getFrom();
    }

    private String getToSummary() {
        return getTo();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(API_KEY))
            mApiKeyPref.setSummary( getApiKeySummary());
        else if (preference.getKey().equals(FROM_LOCATION))
            mFromPref.setSummary( getFromSummary());
        else if (preference.getKey().equals(TO_LOCATION))
            mToPref.setSummary( getToSummary());

        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        super.onPreferenceChange(preference, newValue);
        if (preference.getKey().equals(API_KEY))
            setAPIKey((String)newValue);
        else if (preference.getKey().equals(FROM_LOCATION))
            setFrom((String)newValue);
        else if (preference.getKey().equals(TO_LOCATION))
            setTo((String)newValue);
        return onPreferenceClick(preference);
    }

    private void setAPIKey( String value) {
        mApiKeyPref.getSharedPreferences().edit().putString(API_KEY, value).apply();
    }

    private String getAPIKey() {
        return mApiKeyPref.getSharedPreferences().getString(API_KEY, "");
    }

    private void setFrom( String value) {
        mFromPref.getSharedPreferences().edit().putString(FROM_LOCATION, value).apply();
    }

    private String getFrom() {
        return mFromPref.getSharedPreferences().getString(FROM_LOCATION, "");
    }

    private void setTo( String value) {
        mToPref.getSharedPreferences().edit().putString(TO_LOCATION, value).apply();
    }

    private String getTo() {
        return mToPref.getSharedPreferences().getString(TO_LOCATION,"");
    }


    /*

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
    */
}
