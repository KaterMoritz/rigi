package biz.kindler.rigi.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.modul.system.SystemModel;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 06.06.17.
 */

public class BasePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    protected static final String KEY_SETTINGS_KEY      = "settings-key";
    protected static final String KEY_SETTINGS_VALUE    = "settings-value";

    // Override when needed
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    // Override when needed
    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    protected void updateBooleanPreference( String key, boolean newStatus) {
        SharedPreferences.Editor prefEditor = getActivity().getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE).edit();
        prefEditor.putBoolean( key, newStatus);
        prefEditor.apply();
    }

    protected void updateStringPreference( String key, String newStatus) {
        SharedPreferences.Editor prefEditor = getActivity().getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE).edit();
        prefEditor.putString( key, newStatus);
        prefEditor.apply();
    }

    protected String getStringPreference( String key, String defaultValue) {
        return getActivity().getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE).getString( key, defaultValue);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // send broadcast when change settings
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected static void sendSettingsChangedBroadcast(Context ctx, String action, String key, String newValue) {
        Intent bc = new Intent();
        bc.setAction( action);
        bc.putExtra( KEY_SETTINGS_KEY, key);
        bc.putExtra( KEY_SETTINGS_VALUE, newValue);
        ctx.sendBroadcast(bc);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // send global broadcast
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected void sendSystemBroadcast( Context ctx, String action, String className, String objectName, String message) {
        Intent bc = new Intent();
        bc.setAction( action);
        bc.putExtra( SystemModel.KEY_CLASS, className);
        bc.putExtra( SystemModel.KEY_OBJECT, objectName);
        bc.putExtra( SystemModel.KEY_MESSAGE, message);
        ctx.sendBroadcast(bc);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // helper functions
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // for switch
    protected static void updateSummary( Preference pref, boolean switchState , int onResId, int offResId) {
        pref.setSummary( switchState ? onResId : offResId);
    }
    // for text
    protected static void updateSummary( Preference pref, String summary) {
        pref.setSummary( summary);
    }
    // for replacement text
    protected static void updateSummary( Preference pref, String replacement, int resId) {
        String defTxt = pref.getContext().getResources().getString( resId);
        pref.setSummary( defTxt.replace( "#", replacement));
    }
    // for list
    protected static void updateSummaryForList( Preference pref, String newValue, int resIdKeyList, int resIdValueList) {
        String[] keyArr = pref.getContext().getResources().getStringArray(resIdKeyList);
        String[] valueArr = pref.getContext().getResources().getStringArray(resIdValueList);
        int cnt=0;
        for( String key : keyArr) {
            if( key.equals(newValue)) {
                pref.setSummary( valueArr[cnt]);
                return;
            }
            cnt++;
        }
        pref.setSummary( newValue);
    }
}
