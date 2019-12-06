package biz.kindler.rigi.settings;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import androidx.appcompat.app.ActionBar;

import java.util.List;

import biz.kindler.rigi.R;

/**
 *
 * Created by patrick kindler (katermoritz100@gmail.com)
 *
 *
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private final static String 	        TAG = SettingsActivity.class.getSimpleName();


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    //private GoogleApiClient client;

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client = new GoogleApiClient.Builder(this).build();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || CamPreferenceFragment.class.getName().equals(fragmentName)
                || GaragePreferenceFragment.class.getName().equals(fragmentName)
                || NotificationPreferenceFragment.class.getName().equals(fragmentName)
                || ShutterPreferenceFragment.class.getName().equals(fragmentName)
                || LightPreferenceFragment.class.getName().equals(fragmentName)
                || KioskPreferenceFragment.class.getName().equals(fragmentName)
                || SitzplatzPreferenceFragment.class.getName().equals(fragmentName)
                || LogPreferenceFragment.class.getName().equals(fragmentName)
                || WateringPreferenceFragment.class.getName().equals(fragmentName)
                || CalendarPreferenceFragment.class.getName().equals(fragmentName)
                || PublicTransportPreferenceFragment.class.getName().equals(fragmentName)
                || SoundPreferenceFragment2.class.getName().equals(fragmentName)
                || UpdatePreferenceFragment.class.getName().equals(fragmentName);
    }



    @Override
    public void onStart() {
        super.onStart(); // ATTENTION: This was auto-generated to implement the App Indexing API. // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client.connect();
    }

    @Override
    public void onStop() {
        super.onStop(); // ATTENTION: This was auto-generated to implement the App Indexing API. // See https://g.co/AppIndexing/AndroidStudio for more information.
       // client.disconnect();
    }
}
