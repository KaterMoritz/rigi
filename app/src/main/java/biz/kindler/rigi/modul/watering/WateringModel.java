package biz.kindler.rigi.modul.watering;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;

import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.OneButtonDataHolder;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.settings.WateringPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 23.06.17.
 */

public class WateringModel extends BaseModel {

    private final static String TAG = WateringModel.class.getSimpleName();

    private OneButtonDataHolder     mDataHolder;
    private MyCountDownTimer        mTimer;
    private boolean                 mTimerRunning;
    private int                     mAutoState;
    private int                     mSwitchState;

    private static int              UNKNOWN = 0;
    private static int              ON = 1;
    private static int              OFF = 2;
    private static int              MANUELL = 1;
    private static int              AUTOMATIC = 2;

    private static final String     WATERING_SWITCH                 = "Watering";

   // private static int              TIMER_DURATION                  = 5400000;  // 900000 ms = 15Min  | 5400000 = 1 Std 30 Min
    private static int              HIDE_MODUL_AFTER_DEACTIVATED    = 300;  // 300 sec = 5 Min
    //public static String            BUTTON_DEFAULT_TEXT             = "manuell " + TIMER_DURATION / 60000 + " Min";
    public static String            BUTTON_ON_TEXT                  = "Ein";
    public static String            BUTTON_OFF_TEXT                 = "Aus";
    public static String            INFO_AUTO_TEXT                  = "automatik";
    public static String            INFO_MAN_TEXT                   = "manuell";

    public WateringModel( Context ctx) {
        super(ctx, MainActivity.WATERING);

        mDataHolder = new WateringDataHolder();
        mDataHolder.setButtonInfo( INFO_MAN_TEXT + " " + getDurationTimeAsText( getTimerDuration()));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.WATERING);
        intentFilter.addAction(WateringPreferenceFragment.ACTION_WATERING_SETTINGS_CHANGED);
        intentFilter.addAction(WATERING_SWITCH);
        ctx.registerReceiver(this, intentFilter);

        mTimer = new MyCountDownTimer(getTimerDuration(), 60000);
    }

    public OneButtonDataHolder getDataHolder() {
        return mDataHolder;
    }

    @Override
    protected void initItems() throws Exception {
        sendItemReadBroadcast(WATERING_SWITCH);

        handleAuto( "OFF"); // TODO: implement AUTO watering
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        String value = intent.getStringExtra(ItemManager2.VALUE);

        if( action.equals(WATERING_SWITCH))
            handleWateringState(value);
        else if (action.equals(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.WATERING)) {
            if (mTimerRunning)
                onTimerFinishOrCancel();
            else
                onTimerStart();
        }
        else if( action.equals(WateringPreferenceFragment.ACTION_WATERING_SETTINGS_CHANGED))
            handleWateringSettingsChanged();
    }

    private void handleWateringState( String value) {
        if( value != null) {
            if( value.equals( "ON")) {
                mSwitchState = ON;
                mDataHolder.setHighlighted(true);
                mDataHolder.setImgResId( R.drawable.giesskanne);
                if( ! isModulInList())
                    sendUpdateListItemBroadcast(true);
            }
            else if( value.equals( "OFF")) {
                mSwitchState = OFF;
                mDataHolder.setHighlighted(false);
                mDataHolder.setImgResId( R.drawable.geranientopf);
                if( isModulInList())
                    startTimerForHideModul( 60);
            }
        }
        updateInfoText();
    }

    private void handleAuto( String value) {
        if( value != null) {
            if( value.equals( "ON"))
                mAutoState = ON;
            else if( value.equals( "OFF"))
                mAutoState = OFF;
        }
        updateInfoText();
    }

    private void updateInfoText() {
        String part1 = mAutoState == UNKNOWN ? "" : (mAutoState == ON ? INFO_AUTO_TEXT : INFO_MAN_TEXT);
        String part2 = mSwitchState == UNKNOWN ? "" : ", " + (mSwitchState == ON ? "Ventil offen" : "Ventil geschlossen");;
        mDataHolder.setInfo( part1 + part2);
        sendUpdateListItemBroadcast();
    }
/*
    private void updateInfoText() {
        String part1 = mManAutoInfo == null ? "" : mManAutoInfo;
        String part2 = mOnOffInfo == null ? "" : ", " + mOnOffInfo;
        mDataHolder.setInfo( part1 + part2);

        mDataHolder.setHighlighted( true);
        //  mDataHolder.setImgResId( R.drawable.giesskanne);

        sendUpdateListItemBroadcast(MainActivity.WATERING);
    } */

    private void onTimerStart() {
        mTimer.start();
        mTimerRunning = true;
        sendItemCmdBroadcast( WATERING_SWITCH, "ON");

        mDataHolder.setButtonText(BUTTON_OFF_TEXT);
        mDataHolder.setButtonInfo( "noch " + getDurationTimeAsText( getTimerDuration()));//getRemainingTimeAsText(TIMER_DURATION));
        updateInfoText();

      //  mDataHolder.setInfo( BUTTON_ON_TEXT + " " + INFO_MAN_TEXT);
      //  mDataHolder.setHighlighted( true);
      //  mDataHolder.setImgResId( R.drawable.giesskanne);
       // sendUpdateListItemBroadcast(MainActivity.FORECOURT);
    }

    private void onTimerFinishOrCancel() {
        mTimer.cancel();
        mTimerRunning = false;
        sendItemCmdBroadcast( WATERING_SWITCH, "OFF");

        mDataHolder.setButtonInfo( INFO_MAN_TEXT + " " + getDurationTimeAsText( getTimerDuration())); // BUTTON_DEFAULT_TEXT);
        mDataHolder.setButtonText( BUTTON_ON_TEXT);
        mDataHolder.setInfo( BUTTON_OFF_TEXT + " " + INFO_MAN_TEXT);
        mDataHolder.setHighlighted( false);
        mDataHolder.setImgResId( R.drawable.geranientopf);
        sendUpdateListItemBroadcast();

        startTimerForHideModul( HIDE_MODUL_AFTER_DEACTIVATED);
    }

    private String getDurationTimeAsText( long durationMs) {
        int hours   = (int) ((durationMs / (1000*60*60)) % 24);
        int minutes = (int) ((durationMs / (1000*60)) % 60);

        if (hours > 0)
            return hours + " Std " + (minutes > 0 ? minutes + " Min" : "");
        else
            return minutes + " Min";
    }

    private void handleWateringSettingsChanged() {
        onTimerFinishOrCancel();
        mTimer = new MyCountDownTimer(getTimerDuration(), 60000);
        Log.d(TAG, "handleWateringSettingsChanged [new value:" + getTimerDuration() + "]");
    }

    private long getTimerDuration() {
        return Long.parseLong(getPrefs().getString( WateringPreferenceFragment.MANUELL_DURATION, "5400000"));
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
            mDataHolder.setButtonInfo( "noch " + getDurationTimeAsText( millisUntilFinished));// getRemainingTimeAsText( millisUntilFinished));
            sendUpdateListItemBroadcast();
            if( ! isModulInList()) // todo: and settings automatic show in list
                sendUpdateListItemBroadcast( true);
        }

        @Override
        public void onFinish() {
            onTimerFinishOrCancel();
        }
    }
}
