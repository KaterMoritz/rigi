package biz.kindler.rigi.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.text.TextUtils;
import android.view.WindowManager;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Set;

import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.system.Log;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 06.06.17.
 */

public class LogPreferenceFragment extends BasePreferenceFragment {

    private final static String TAG             = LogPreferenceFragment.class.getSimpleName();

    public static final String DEVICENAME       = "devicename";
    public static final String LOGLEVEL         = "loglevel";
    public static final String MODULLIST_DEBUG  = "modulListDebug";
    public static final String SEND_LOG_DAILY   = "sendLogDaily";
    public static final String SEND_LOG_NOW     = "sendLogNow";
    public static final String TEST_EXC         = "testException";
    public static final String SYSTEMSERVICE    = "systemservice";

    private EditTextPreference                  mDeviceNamePref;
    private ListPreference                      mLogLevelPref;
    private ListPreference                      mSendLogNowPref;
    private SwitchPreference                    mSendLogDailySwitchPref;
    private MultiSelectListPreference           mModulListDebugListPref;
    private EditTextPreference                  mSystemServicePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_log);
        setHasOptionsMenu(true);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mDeviceNamePref = (EditTextPreference)findPreference(DEVICENAME);
        mDeviceNamePref.setOnPreferenceChangeListener(this);
        updateSummary( mDeviceNamePref, mDeviceNamePref.getSharedPreferences().getString(DEVICENAME, "unknown"));

        mLogLevelPref = (ListPreference)findPreference( LOGLEVEL);
        mLogLevelPref.setOnPreferenceChangeListener(this);
        updateSummaryForLoglevel(null);

        mModulListDebugListPref = (MultiSelectListPreference)findPreference( MODULLIST_DEBUG);
        mModulListDebugListPref.setOnPreferenceChangeListener(this);
        updateSummaryForModulDebugList(null);

        mSendLogDailySwitchPref = (SwitchPreference)findPreference( SEND_LOG_DAILY);
        mSendLogDailySwitchPref.setOnPreferenceChangeListener(this);
        updateSummaryForSendLogDailySwitchState(null);

        mSendLogNowPref = (ListPreference)findPreference(SEND_LOG_NOW);
        mSendLogNowPref.setOnPreferenceChangeListener( this);
        mSendLogNowPref.setOnPreferenceClickListener( this);
        setLogListLookupData( mSendLogNowPref);

        Preference testExcPreference = findPreference(TEST_EXC);
        testExcPreference.setOnPreferenceClickListener( this);

        mSystemServicePref = (EditTextPreference)findPreference(SYSTEMSERVICE);
        mSystemServicePref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        super.onPreferenceChange( preference, newValue);
        String key = preference.getKey();
        if( key.equals( DEVICENAME)) {
            updateSummary( mDeviceNamePref, newValue.toString());
            return true;
        } else if( key.equals( SEND_LOG_DAILY)) {
            updateSummaryForSendLogDailySwitchState(newValue);
            return true;
        } else if(key.equals( MODULLIST_DEBUG)) {
            biz.kindler.rigi.modul.system.Log.setLogConfig( TextUtils.join( " ", (Set<String>)newValue));
            updateSummaryForModulDebugList(newValue);
            return true;
        } else if(key.equals( LOGLEVEL)) {
            biz.kindler.rigi.modul.system.Log.setRootLoglevel( newValue.toString());
            updateSummaryForLoglevel(newValue);
            return true;
        } else if (key.equals(SEND_LOG_NOW)) {
            updateSummaryForSendLogClicked( newValue);
            return true;
        } else if( key.equals(SYSTEMSERVICE))
            sendSystemServiceCommand(preference, newValue);

        return false;
    }

    private void updateSummaryForLoglevel( Object newValue) {
        String loglevel = "";
        if( newValue == null)
            loglevel = mLogLevelPref.getSharedPreferences().getString( LOGLEVEL, "");
        else if( newValue instanceof String)
            loglevel = ((String)newValue).toString();

        mLogLevelPref.setSummary( getLoglevelLong(loglevel));
    }

    private String getLoglevelLong( String level) {
        if( level.equals( "D"))
            return "Debug";
        else if( level.equals( "I"))
            return "Information";
        else if( level.equals( "W"))
            return "Warning";
        else if( level.equals( "E"))
            return "Error";
        else
            return "?";
    }

    private void updateSummaryForModulDebugList(Object newValue) {
        String summary = "";
        Set<String> selections = null;
        if( newValue != null)
            selections = (Set<String>)newValue;
        else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            selections = preferences.getStringSet(MODULLIST_DEBUG, null);
        }

        String[] selected = selections.toArray(new String[]{});
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < selected.length; i++)
            buff.append(selected[i] + ",");

        if( buff.length() > 0)
            summary = buff.substring(0, buff.length() -1);
        else
            summary = "keine";

        mModulListDebugListPref.setSummary(summary);
    }

    private void updateSummaryForSendLogDailySwitchState( Object newValue) {
        boolean switchState = false;
        if( newValue == null)
            switchState = mSendLogDailySwitchPref.getSharedPreferences().getBoolean( SEND_LOG_DAILY, true);
        else if( newValue instanceof Boolean)
            switchState = ((Boolean)newValue).booleanValue();

        updateBooleanPreference( SEND_LOG_DAILY, switchState);
        mSendLogDailySwitchPref.setSummary( switchState ? "Sendet täglich an Entwickler" : "Sendet nicht automatisch an Entwickler");
    }

    private void updateSummaryForSendLogClicked( Object newValue) {
        sendSystemBroadcast( mSendLogNowPref.getContext(), SEND_LOG_NOW, LogPreferenceFragment.class.getName(), newValue.toString(), "");
        mSendLogNowPref.setSummary( "send LOG für " + newValue + " OK (check System Log für status)");
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        if( key.equals(TEST_EXC))
            handleTestExceptionClicked( preference);
        else if (key.equals(SEND_LOG_NOW))
            setLogListLookupData( (ListPreference)preference);

        return false;
    }

    // adb pull /storage/emulated/0/rigilog/rigi_158.log /Users/sidiplasmac/temp/rigi/
    private void setLogListLookupData(ListPreference preference) {

        String logPath = Log.getLogPath();
        Log.d(TAG, "logPath: " + logPath);

        File directory = new File(logPath);
        File[] files = directory.listFiles();

        if( files != null && files.length > 0) {
            Arrays.sort(files, Collections.reverseOrder());

            String[] entriesArr = new String[files.length];
            String[] entryValuesArr = new String[files.length];

            int dayOfYearToday = new GregorianCalendar().get(java.util.Calendar.DAY_OF_YEAR);

            if (files != null) {

                for (int i = 0; i < files.length; i++) {
                    String filename = files[i].getName();
                    entriesArr[i] = getFileDateReadable(dayOfYearToday, files[i]);
                    entryValuesArr[i] = filename;
                }
            }

            preference.setEntries(entriesArr);
            preference.setEntryValues(entryValuesArr);
            preference.setValueIndex(0);
        }
    }

    private String getFileDateReadable( int dayOfYearToday, File file) {
        String filename = file.getName();
        String filesize = readableFileSize(file.length());

        int dayOfYearOfFile = Integer.parseInt(filename.substring( 5, filename.length() -4));
        if( dayOfYearOfFile == dayOfYearToday)
            return "heute" + " [" + filesize + "]";
        else if( dayOfYearOfFile == dayOfYearToday -1)
            return "gestern" + " [" + filesize + "]";
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("d MMM");
            GregorianCalendar gc = new GregorianCalendar();
            gc.set(Calendar.DAY_OF_YEAR, dayOfYearOfFile);
            return sdf.format(gc.getTime()) + " [" + filesize + "]";
        }
    }

    private void handleTestExceptionClicked(Preference preference) {
        sendSystemBroadcast( preference.getContext(), SEND_LOG_NOW, LogPreferenceFragment.class.getName(), null, "tracepot");
        preference.setSummary( "OK (check System Log für status)");
    }

    private void sendSystemServiceCommand(Preference preference, Object newValue) {
        sendSystemBroadcast( preference.getContext(), SYSTEMSERVICE, LogPreferenceFragment.class.getName(), null, String.valueOf(newValue));
    }

    // thanks to: https://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
    public String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
