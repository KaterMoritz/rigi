package biz.kindler.rigi.modul.entree;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 02.12.16.
 */

public class ScreensaverActivity extends AppCompatActivity {

    private final static String TAG = ScreensaverActivity.class.getSimpleName();

    public static String        KEY_PREFS_SCREENSAVER_ON    = "screensaver-on";
    public static String        KEY_PREFS_SCREENSAVER_OFF   = "screensaver-off";
    public static String        SCREENSAVER_STATUS          = "screensaver-status";

    private Timer               mScreensaverTimer;
    private String              mTimerOff;
    private SimpleDateFormat    mDateTimeFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screensaver);

        mDateTimeFormatter = new SimpleDateFormat("HH:mm dd.MM.yy");
        mDateTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        final Bundle extras = getIntent().getExtras();
        if (extras != null)
            mTimerOff = extras.getString( KEY_PREFS_SCREENSAVER_OFF, "04:00");

        CoordinatorLayout layout  = (CoordinatorLayout)findViewById(R.id.screensaver);
        layout.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        Log.i(TAG, "Screensaver switched ON");
        setStatusInPrefs( true);

        Date endDateScreensaver = getScreensaverEnd( 0);
        if( endDateScreensaver.before( new Date()))
            endDateScreensaver = getScreensaverEnd( 1);

        mScreensaverTimer = new Timer();
        mScreensaverTimer.schedule( new TimerTask() {
            public void run() {
                Log.i(TAG, "Screensaver switched OFF");
                setStatusInPrefs( false);
                finish();
            }
        }, endDateScreensaver);
        String endScreensaver = mDateTimeFormatter.format(endDateScreensaver);
        sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "Screensaver", "scheduled OFF (init): " + endScreensaver);

        Log.d(TAG, "Screensaver scheduled OFF (init): " + endScreensaver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getSupportActionBar().hide();
    }

    private Date getScreensaverEnd( int addDays) {
        String[] timeHrMin = mTimerOff.split( ":");
        // set the screensaver time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeHrMin[0]));
        calendar.set(Calendar.MINUTE, Integer.valueOf(timeHrMin[1]));
        calendar.set(Calendar.SECOND, 0);
        if( addDays > 0)
            calendar.add( Calendar.DAY_OF_YEAR, addDays);
        return calendar.getTime();
    }

    protected void sendSystemBroadcast( String action, String className, String objectName, String message) {
        Intent bc = new Intent();
        bc.setAction( action);
        bc.putExtra( SystemModel.KEY_CLASS, className);
        bc.putExtra( SystemModel.KEY_OBJECT, objectName);
        bc.putExtra( SystemModel.KEY_MESSAGE, message);
        sendBroadcast(bc);
    }

    private void setStatusInPrefs( boolean status) {
        SharedPreferences.Editor editor = getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE).edit();
        editor.putBoolean( SCREENSAVER_STATUS, status);
        editor.apply();
    }

}
