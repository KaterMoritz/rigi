package biz.kindler.rigi.modul.entree;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.StrictMode;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.OneButtonDataHolder;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import biz.kindler.rigi.modul.weatherstation.WeatherstationModel;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 27.11.16.
 */

public class EntreeModel extends BaseModel {

    private final static String TAG = EntreeModel.class.getSimpleName();

    public static final String PREFS_DISPLAY_OFF_AFTER_MINUTES = "display-off-after-minutes";
    // Item names
    private static final String SOMEONE_MOVING = "Bewegung_Ankleide";//   // Light2
    private static final String LIGHT_DIMMER = "Licht_Ankleide_Dimmer";
    private static final String LIGHT_MANUELL = "Licht_Ankleide_ManAuto";

    private static int TIMER_DURATION = 900000;  // 900000 ms = 15Min
    public static String BUTTON_DEFAULT_TEXT = "manuell " + TIMER_DURATION / 60000 + " Min";
    public static String BUTTON_ON_TEXT = "Ein";
    public static String BUTTON_OFF_TEXT = "Aus";
    public static String INFO_AUTO_TEXT = "automatik";
    public static String INFO_MAN_TEXT = "manuell";

    private EntreeView mView;
    private OneButtonDataHolder mDataHolder;
    private MyCountDownTimer mTimer;
    private boolean mTimerRunning;
    private boolean mSomeoneMoving;
    private int mMovingInEntreeTimerCnt;
    private Timer mScreensaverTimer;
    private String mLightInfoTxt;
    private String mManAutoInfoTxt;
   // private OpenHABItem mLightHabItem;
   // private OpenHABItem mManuellHabItem;


    public EntreeModel(Context ctx) {
        super(ctx, MainActivity.ENTREE);

        mView = new EntreeView(ctx);

        mDataHolder = new EntreeDataHolder();

        IntentFilter intentFilter = new IntentFilter();
       // intentFilter.addAction(OpenHABItem.ACTION_UPDATE_STATE);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
       // intentFilter.addAction(TimeAndDateModel.ACTION_DAY_SEGMENT);
        intentFilter.addAction(WeatherstationModel.ACTION_LUX_LEVEL);
        intentFilter.addAction(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.ENTREE);
        intentFilter.addAction( SOMEONE_MOVING);
        intentFilter.addAction( LIGHT_MANUELL);
        intentFilter.addAction( LIGHT_DIMMER);
        ctx.registerReceiver(this, intentFilter);

        mTimer = new MyCountDownTimer(TIMER_DURATION, 60000);

        Date startDateScreensaver = getScreensaverStart(0);
        if (startDateScreensaver.before(new Date()))
            startDateScreensaver = getScreensaverStart(1);

        mScreensaverTimer = new Timer();
        mScreensaverTimer.schedule(new ScreensaverTimerTask(), startDateScreensaver);
        String startScreensaver = getDateTimeFormatter().format(startDateScreensaver);
        Log.d(TAG, "Screensaver scheduled ON (init): " + startScreensaver);
        sendSystemBroadcast(SystemModel.ACTION_LOG, getClass().getName(), "Screensaver", "scheduled ON (init): " + startScreensaver);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    private Date getScreensaverStart(int addDays) {
        // set the screensaver time
        String timeAsString = getPrefs().getString(ScreensaverActivity.KEY_PREFS_SCREENSAVER_ON, "23:55");
        String[] timeHrMin = timeAsString.split(":");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeHrMin[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeHrMin[1]));
        calendar.set(Calendar.SECOND, 0);
        if (addDays > 0)
            calendar.add(Calendar.DAY_OF_YEAR, addDays);
        return calendar.getTime();
    }

    public OneButtonDataHolder getDataHolder() {
        return mDataHolder;
    }

    protected void initItems() {
        mDataHolder.setButtonInfo(BUTTON_DEFAULT_TEXT);
        sendItemReadBroadcast(LIGHT_MANUELL);
        sendItemReadBroadcast(LIGHT_DIMMER);
    }

    private void handleSomeoneMoving(String itemState) {
        if (itemState.equals("ON")) {
            mMovingInEntreeTimerCnt = 0;  // restart timer
            mView.setDisplayBrightness(EntreeView.DISPLAY_BRIGHT);
            mSomeoneMoving = true;
        } else if (itemState.equals("OFF"))
            mSomeoneMoving = false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        String value = intent.getStringExtra(ItemManager2.VALUE);

        if( action.equals(SOMEONE_MOVING))
            handleSomeoneMoving(value);
        else if( action.equals(LIGHT_MANUELL))
            handleLightManAuto( value);
        else if( action.equals(LIGHT_DIMMER))
            handleLDimmer( value);
        else if (action.equals(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.ENTREE)) {
            if (mTimerRunning)
                onTimerFinishOrCancel();
            else
                onTimerStart();
        } else if (action.equals(Intent.ACTION_TIME_TICK)) {
            if (!mSomeoneMoving)
                mMovingInEntreeTimerCnt++;

            if (mMovingInEntreeTimerCnt >= Integer.parseInt(getPrefs().getString(PREFS_DISPLAY_OFF_AFTER_MINUTES, "2")))
                handleMovingInEntreeTimerFinish();
        } else if (action.equals(WeatherstationModel.ACTION_LUX_LEVEL)) {
            if( intent.hasExtra(WeatherstationModel.SUNRISE_BY_LUX))
                handleSunrise();
            else if( intent.hasExtra(WeatherstationModel.SUNSET_BY_LUX))
                handleSunset();
        }
    }

    private void handleLightManAuto( String value) {
        if( value != null && ! value.equals( "NULL")) {
            if( value.equals( "ON"))
                mManAutoInfoTxt =  INFO_MAN_TEXT;
            else if( value.equals( "OFF"))
                mManAutoInfoTxt =  INFO_AUTO_TEXT;
        }
        sendInfo();
    }

    private void handleLDimmer( String value) {
        if( value != null && ! value.equals( "NULL")) {
            if( value.equals( "0") || value.equals( "OFF"))
                mLightInfoTxt = "Licht aus";
            else {
                if( ! value.equals( "ON"))
                    mLightInfoTxt = "Licht ein " + value + "%";
            }
        }
        sendInfo();
    }

    private void sendInfo() {
        if( mLightInfoTxt == null)
            mDataHolder.setInfo( mManAutoInfoTxt);
        else
            mDataHolder.setInfo( mManAutoInfoTxt + ", " + mLightInfoTxt);
        sendUpdateListItemBroadcast();
    }

    private void handleSunrise() {
        if( ! mTimerRunning) // todo && settings true for auto show/hide
            sendUpdateListItemBroadcast( false);
    }

    private void handleSunset() {
        if( ! mTimerRunning) // todo && settings true for auto show/hide
            sendUpdateListItemBroadcast( true);
    }

    private void onTimerStart() {
        mTimer.start();
        mTimerRunning = true;
        sendItemCmdBroadcast( LIGHT_MANUELL, "ON");
        // delay for the next command
        new Timer().schedule( new Light100TimerTask(), 500);

        mDataHolder.setButtonText(BUTTON_OFF_TEXT);
        mDataHolder.setButtonInfo(getRemainingTimeAsText(TIMER_DURATION));
        sendInfo();
    }

    private void onTimerFinishOrCancel() {
        mTimer.cancel();
        mTimerRunning = false;
        sendItemCmdBroadcast( LIGHT_MANUELL, "OFF");
        // delay for the next command
        new Timer().schedule( new LightOffTimerTask(), 500);

        mDataHolder.setButtonInfo( BUTTON_DEFAULT_TEXT);
        mDataHolder.setButtonText( BUTTON_ON_TEXT);
        sendInfo();
    }

    private String getRemainingTimeAsText( long millisUntilFinished) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(millisUntilFinished);
        return "noch " + cal.get(Calendar.MINUTE) + " Min";
    }

    private void handleMovingInEntreeTimerFinish() {
        mView.setDisplayBrightness( EntreeView.DISPLAY_DARK);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class MyCountDownTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            mDataHolder.setButtonInfo( getRemainingTimeAsText( millisUntilFinished));
            //sendUpdateListItemBroadcast(MainActivity.ENTREE);
            sendInfo();

            //ArrayList<PayloadDataHolder> payloadList = new ArrayList();
           // payloadList.add( new PayloadDataHolder( R.id.button_info, getRemainingTimeAsText( millisUntilFinished)));
            // TODO notifyItemChanged(payloadList);
        }

        @Override
        public void onFinish() {
            onTimerFinishOrCancel();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Timertask
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class ScreensaverTimerTask extends TimerTask {

        public void run() {
            String timeAsString = getPrefs().getString( ScreensaverActivity.KEY_PREFS_SCREENSAVER_OFF, "04:55");
            Intent screensaverIntent = new Intent(getContext(), ScreensaverActivity.class);
            screensaverIntent.putExtra( ScreensaverActivity.KEY_PREFS_SCREENSAVER_OFF, timeAsString);
            getContext().startActivity(screensaverIntent);
            mScreensaverTimer.schedule( new ScreensaverTimerTask(), getScreensaverStart( 1));
        }
    }

    private class Light100TimerTask extends TimerTask {

        public void run() {
            sendItemCmdBroadcast( LIGHT_DIMMER, "100");
        }
    }

    private class LightOffTimerTask extends TimerTask {

        public void run() {
            sendItemCmdBroadcast( LIGHT_DIMMER, "50");
        }
    }
}
