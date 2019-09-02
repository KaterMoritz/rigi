package biz.kindler.rigi;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.settings.LogPreferenceFragment;
import biz.kindler.rigi.settings.UpdatePreferenceFragment;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APPLICATION_LOG;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.EVENTSLOG;
import static org.acra.ReportField.PACKAGE_NAME;
import static org.acra.ReportField.REPORT_ID;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_CRASH_DATE;



/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 01.06.17.
 */

@ReportsCrashes(
        formUri = "https://collector.tracepot.com/fc882521",
        customReportContent = { ANDROID_VERSION, APP_VERSION_CODE, APP_VERSION_NAME, PACKAGE_NAME, REPORT_ID, STACK_TRACE, USER_APP_START_DATE, USER_CRASH_DATE, APPLICATION_LOG, org.acra.ReportField.LOGCAT, EVENTSLOG })

public class RigiApplication extends Application {

    private final static String 	TAG = RigiApplication.class.getSimpleName();

    private static Context mCtx;


    @Override
    public void onCreate () {
        super.onCreate();

        mCtx = getApplicationContext();

        Log.d(TAG, "Rigi app started");

        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefsEditor.putString( UpdatePreferenceFragment.APP_START, String.valueOf(new Date().getTime()));
        prefsEditor.commit();

/*
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        } */
/*
        try {
            Runtime.getRuntime().exec("logcat -c");  // clear logcat
            Log.d(TAG, "logcat cleared");
        } catch (IOException e) {
            Log.w(TAG, e.getMessage());
        } */
    }

    public static Context getAppContext() {
        return mCtx;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        initMyLog();

        // Create an ConfigurationBuilder. It is prepopulated with values specified via annotation.
        // Set any additional value of the builder and then use it to construct an ACRAConfiguration.
        ACRA.init(this);
        Log.d(TAG, "ACRA init");
    }

    private void initMyLog() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> modullistDebugSet = preferences.getStringSet(LogPreferenceFragment.MODULLIST_DEBUG, null);
        String modullistDebugString = modullistDebugSet ==null ? "" : TextUtils.join( " ", modullistDebugSet);
        String rootLoglevel = preferences.getString(LogPreferenceFragment.LOGLEVEL, "i");

        biz.kindler.rigi.modul.system.Log.setDayOfYear( new GregorianCalendar().get(java.util.Calendar.DAY_OF_YEAR));
        biz.kindler.rigi.modul.system.Log.setLogConfig( modullistDebugString);
        biz.kindler.rigi.modul.system.Log.setRootLoglevel( rootLoglevel);

        Log.d(TAG, "MyLog init");
    }

}
