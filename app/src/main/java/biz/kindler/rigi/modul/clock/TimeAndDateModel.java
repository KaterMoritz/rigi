package biz.kindler.rigi.modul.clock;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.system.Log;



/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 27.11.16.
 */

public class TimeAndDateModel extends BaseModel {

    private final static String 	TAG = TimeAndDateModel.class.getSimpleName();

    public static  String       ACTION_NEW_DAY      =   "action-new-day";
    public static  String       ACTION_DAY_SEGMENT  =   "action-day-segment"; // sunrise, sunset, lunchtime
    public static  String       KEY_SUNRISE_TIME    =   "key-sunrise-time";
    public static  String       KEY_SUNSET_TIME     =   "key-sunset-time";
    public static  String       KEY_LUNCH_TIME      =   "key-lunch-time";
    public static  String       KEY_MIDNIGHT        =   "key-midnight";
    public static  String       KEY_APP_START_TODAY =   "key-app-start-today";
    public static  String       KEY_DAY_OF_YEAR     =   "key-day-of-year";
    public static  String       KEY_DAY_OF_MONTH    =   "key-day-of-month";

    private static final String LATITUDE            =   "47.098755";  // its Buchrain
    private static final String LONGITUDE           =   "8.353241";   // its Buchrain
    private static final String TIMEZONE            =   "Europe/Paris";


    private TimeAndDateView     mView;
    private GregorianCalendar   mGCal;
    private int                 mCurrDay;
    private int                 mAppStartDayOfYear;

    public TimeAndDateModel( Context ctx) {
        super(ctx, MainActivity.TIMEANDDATE);

        mView = new TimeAndDateView( getContext());
        mGCal = new GregorianCalendar(Locale.GERMAN);

        mAppStartDayOfYear = new GregorianCalendar().get(java.util.Calendar.DAY_OF_YEAR);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        ctx.registerReceiver(this, intentFilter);
    }

    @Override
    protected void initItems() throws Exception {} // no items

    @Override
    public void onReceive(Context context, Intent intent) {
        Date now = new Date();
        mGCal.setTime(now);

        int currDay = mGCal.get(Calendar.DAY_OF_YEAR);
        if (currDay != mCurrDay) {
            Log.i(TAG, "close log for today [day: " + currDay + "]");
            mCurrDay = currDay;
            handleNewDay();
        }

        if( mGCal.get( Calendar.SECOND) > 30)
            mGCal.add( Calendar.MINUTE, 1);

        mView.setTime( mGCal.getTime());
    }

    private void handleNewDay() {
        int dayOfYear = mGCal.get(Calendar.DAY_OF_YEAR);
        int dayOfMonth = mGCal.get(Calendar.DAY_OF_MONTH);
        String[] sunriseSunset = getSunriseAndSunset();
        Date[] sunriseSunsetAsDate = getSunriseAndSunsetDate();
        Date sunriseDate = sunriseSunsetAsDate[0];
        Date sunsetDate = sunriseSunsetAsDate[1];
        Date lunchDate = getLunchtime();
        Date midnightDate = getMidnight();

        Log.i(TAG, "handleNewDay [dayOfYear:" + dayOfYear + ",dayOfMonth:" + dayOfMonth + ",sunrise:" + sunriseSunset[0] + ",sunset:" + sunriseSunset[1] + "]");

        if( sunriseDate.after(new Date())) {
            new Timer().schedule(new DaySegmentTimerTask(KEY_SUNRISE_TIME), sunriseDate);
            Log.i(TAG, KEY_SUNRISE_TIME + " scheduled: " + getDateTimeFormatter().format(sunriseDate));
        }

        if( lunchDate.after(new Date())) {
            new Timer().schedule(new DaySegmentTimerTask(KEY_LUNCH_TIME), lunchDate);
            Log.i(TAG, KEY_LUNCH_TIME + " scheduled: " + getDateTimeFormatter().format(lunchDate));
        }

        if( sunsetDate.after(new Date())) {
            new Timer().schedule(new DaySegmentTimerTask(KEY_SUNSET_TIME), sunsetDate);
            Log.i(TAG, KEY_SUNSET_TIME + " scheduled: " + getDateTimeFormatter().format(sunsetDate));
        }

        new Timer().schedule(new DaySegmentTimerTask(KEY_MIDNIGHT), midnightDate);
        Log.i(TAG, KEY_MIDNIGHT + " scheduled: " + getDateTimeFormatter().format(midnightDate));

        sendNewDayBroadcast(dayOfYear, dayOfMonth, sunriseSunset[0], sunriseSunset[1]);

        mView.setDate(mGCal.getTime());
        mGCal.add(Calendar.DAY_OF_WEEK, 1);
        mView.setTomorrowDay( mGCal.getTime());
        mGCal.add(Calendar.DAY_OF_WEEK, 1);
        mView.setAfterTomorrowDay( mGCal.getTime());
    }

    private void sendNewDayBroadcast( int dayOfYear, int dayOfMonth, String sunrise, String sunset) {
        Intent bc = new Intent();
        bc.setAction( ACTION_NEW_DAY);
        bc.putExtra( KEY_DAY_OF_YEAR, dayOfYear);
        bc.putExtra( KEY_DAY_OF_MONTH, dayOfMonth);
        bc.putExtra( KEY_SUNRISE_TIME, sunrise);
        bc.putExtra( KEY_SUNSET_TIME, sunset);
        bc.putExtra( KEY_APP_START_TODAY, dayOfYear == mAppStartDayOfYear);
        getContext().sendBroadcast(bc);
    }

    private void sendDaySegmentBroadcast( String key) {
        Intent bc = new Intent();
        bc.setAction( ACTION_DAY_SEGMENT);
        bc.putExtra( key, true);
        getContext().sendBroadcast(bc);
    }

    private String[] getSunriseAndSunset() {
        com.luckycatlabs.sunrisesunset.dto.Location location = new com.luckycatlabs.sunrisesunset.dto.Location(LATITUDE, LONGITUDE);
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, TIMEZONE);
        String officialSunrise = calculator.getOfficialSunriseForDate(Calendar.getInstance());
        String officialSunset = calculator.getOfficialSunsetForDate(Calendar.getInstance());
        return new String[] { officialSunrise, officialSunset};
    }

    private Date[] getSunriseAndSunsetDate() {
        com.luckycatlabs.sunrisesunset.dto.Location location = new com.luckycatlabs.sunrisesunset.dto.Location(LATITUDE, LONGITUDE);
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, TIMEZONE);
        Date officialSunrise = calculator.getOfficialSunriseCalendarForDate(Calendar.getInstance()).getTime();
        Date officialSunset = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance()).getTime();
        return new Date[] { officialSunrise, officialSunset};
    }

    private Date getLunchtime() {
        GregorianCalendar gcal = new GregorianCalendar(Locale.GERMAN);
        gcal.set( Calendar.HOUR_OF_DAY, 12);
        gcal.set( Calendar.MINUTE, 0);
        gcal.set( Calendar.SECOND, 0);
        return gcal.getTime();
    }

    private Date getMidnight() {
        GregorianCalendar gcal = new GregorianCalendar(Locale.GERMAN);
        gcal.set( Calendar.HOUR_OF_DAY, 23);
        gcal.set( Calendar.MINUTE, 59);
        gcal.set( Calendar.SECOND, 59);
        return gcal.getTime();
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Timertask
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class DaySegmentTimerTask extends TimerTask {

        String type;

        public DaySegmentTimerTask( String type) {
            this.type = type;
        }

        public void run() {
            sendDaySegmentBroadcast(type);
        }
    }

}
