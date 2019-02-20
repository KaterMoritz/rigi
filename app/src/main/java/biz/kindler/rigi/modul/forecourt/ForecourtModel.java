package biz.kindler.rigi.modul.forecourt;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.OneButtonDataHolder;
import biz.kindler.rigi.modul.clock.TimeAndDateModel;
import biz.kindler.rigi.modul.garage.GarageModel;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import biz.kindler.rigi.modul.weatherstation.WeatherstationModel;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 21.12.16.
 */

public class ForecourtModel extends BaseModel {

    private final static String TAG = ForecourtModel.class.getSimpleName();

    private OneButtonDataHolder     mDataHolder;
    private MyCountDownTimer        mTimer;
    private boolean                 mTimerRunning;
    private String                  mDayOrNight;
    private String                  mLightInfoTxt = "";
    private String                  mManAutoInfoTxt = "";

   // private static final String     LIGHT_SWITCH            = "Aussenlicht";
    private static final String     LIGHT_DIMMER            = "Aussenlicht_Dimmer";
    private static final String     LIGHT_MANUELL           = "Aussenlicht_ManAuto";
    private static final String     LIGHT_GLASLICHT         = "Licht_Glas_Eingang";

    private static int              TIMER_DURATION          = 900000;  // 900000 ms = 15Min
    public static String            BUTTON_DEFAULT_TEXT     = "manuell " + TIMER_DURATION / 60000 + " Min";
    public static String            BUTTON_ON_TEXT          = "Ein";
    public static String            BUTTON_OFF_TEXT         = "Aus";
    public static String            INFO_AUTO_TEXT          = "automatik";
    public static String            INFO_MAN_TEXT           = "manuell";


    public ForecourtModel( Context ctx) {
        super( ctx, MainActivity.FORECOURT);

        mDayOrNight = "";

        mDataHolder = new ForecourtDataHolder();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.FORECOURT);
        intentFilter.addAction(SystemModel.ACTION_DAY_OR_NIGHT);
        intentFilter.addAction(TimeAndDateModel.ACTION_DAY_SEGMENT);
        intentFilter.addAction( LIGHT_DIMMER);
        intentFilter.addAction( LIGHT_MANUELL);
        intentFilter.addAction( GarageModel.GARAGE_TOR_STATE);
        ctx.registerReceiver(this, intentFilter);

        mTimer = new MyCountDownTimer(TIMER_DURATION, 60000);
    }

    public OneButtonDataHolder getDataHolder() {
        return mDataHolder;
    }

    protected void initItems() {
        //sendItemReadBroadcast(LIGHT_SWITCH);
        sendItemReadBroadcast(LIGHT_DIMMER);
        sendItemReadBroadcast(LIGHT_MANUELL);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive( context, intent);

        String action = intent.getAction();
        String value = intent.getStringExtra(ItemManager2.VALUE);

        if( action.equals(LIGHT_DIMMER))
            handleDimmer( value);
        else if( action.equals(LIGHT_MANUELL))
            handleLightManAuto( value);
        else if( action.equals(GarageModel.GARAGE_TOR_STATE))
            handleGarageTorState( value);
        else if (action.equals(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.FORECOURT)) {
           if (mTimerRunning)
               onTimerFinishOrCancel();
            else
               onTimerStart();
        } else if( action.equals( SystemModel.ACTION_DAY_OR_NIGHT)) {
            String dayOrNight = intent.getStringExtra(SystemModel.KEY_MESSAGE);
            if( ! mDayOrNight.equals( dayOrNight)) {
                if( mDayOrNight.equals(WeatherstationModel.DAY) && dayOrNight.equals(WeatherstationModel.NIGHT)) {
                    sendItemCmdBroadcast( LIGHT_GLASLICHT, "ON");
                    sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), LIGHT_GLASLICHT, "ON");
                }
                else if( mDayOrNight.equals(WeatherstationModel.NIGHT) && dayOrNight.equals(WeatherstationModel.DAY)) {
                    sendItemCmdBroadcast( LIGHT_GLASLICHT, "OFF");
                    sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), LIGHT_GLASLICHT, "OFF");
                }
                mDayOrNight = dayOrNight;
            }
        }  else if (action.equals(TimeAndDateModel.ACTION_DAY_SEGMENT)) {
            if( intent.getBooleanExtra(TimeAndDateModel.KEY_SUNRISE_TIME, false))
                handleSunrise();
            else if( intent.getBooleanExtra(TimeAndDateModel.KEY_SUNSET_TIME, false))
                handleSunset();
        }
    }

    private void handleDimmer( String value) {
        if( value != null && ! value.equalsIgnoreCase( "NULL")) {
            if( value.equals( "0") || value.equals( "OFF")) {
                mLightInfoTxt = "Licht aus";
                mDataHolder.setHighlighted(false);
                if( isModulInList())
                    startTimerForHideModul( 60);
            }
            else {
                if( ! value.equals( "ON"))
                    mLightInfoTxt = "Licht ein " + value + "%";
                mDataHolder.setHighlighted(true);
                if( ! isModulInList())
                    sendUpdateListItemBroadcast( true);
            }
        }
        sendInfo();
    }

    private void handleLightManAuto( String value) {
        if( value != null && ! value.equalsIgnoreCase( "NULL")) {
            if( value.equals( "ON"))
                mManAutoInfoTxt =  INFO_MAN_TEXT;
            else if( value.equals( "OFF"))
                mManAutoInfoTxt =  INFO_AUTO_TEXT;
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

    private void handleGarageTorState( String value) {
        if ( value != null && value.equals("ON") && mDayOrNight != null && mDayOrNight.equals(WeatherstationModel.NIGHT)) {
            // todo: and property auto light outside when garage open
            onTimerStart();
            Log.i(TAG, "Garage open and night, start timer and switch light on");
        }
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
        new Timer().schedule( new LightOnTimerTask(), 500);

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
            sendUpdateListItemBroadcast();
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
    private class LightOnTimerTask extends TimerTask {

        public void run() {
            sendItemCmdBroadcast( LIGHT_DIMMER, "100");
        }
    }

    private class LightOffTimerTask extends TimerTask {

        public void run() {
            sendItemCmdBroadcast( LIGHT_DIMMER, "0");
        }
    }
}
