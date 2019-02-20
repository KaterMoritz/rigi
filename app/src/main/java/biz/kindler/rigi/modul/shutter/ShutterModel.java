package biz.kindler.rigi.modul.shutter;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.TwoButtonDataHolder;
import biz.kindler.rigi.modul.clock.TimeAndDateModel;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import biz.kindler.rigi.settings.ShutterPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 29.11.16.
 */

public class ShutterModel extends BaseModel {

    private final static String TAG = ShutterModel.class.getSimpleName();

    // Settings
    public static final String  PREF_SHUTTER_WAKEUP_SWITCH          = "shutter_wakeup_switch";
    public static final String  PREF_SHUTTER_WAKEUP_TIME            = "wakeup_time";
    public static final String  PREF_SHUTTER_WAKEUP_TIME_STEP       = "wakeup_time_step";

    // Item names
    private static final String GARTEN                              = "Store_EG_Garten";
    private static final String SOFA                                = "Store_EG_Sofa";
    private static final String ESSZIMMER                           = "Store_EG_Esszimmer";
    private static final String BLUMENFENSTER                       = "Store_EG_Blumenfenster";
    private static final String KUECHE                              = "Store_EG_Kueche";
    private static final String SCHLAFZIMMER                        = "Store_OG_Schlafzimmer";
    private static final String SCHLAFZIMMER_PRESET12               = "Store_OG_Schlafzimmer_Preset12"; // OFF = 98% ON = 85%
    private static final String SCHLAFZIMMER_PRESET34               = "Store_OG_Schlafzimmer_Preset34"; // OFF = 20% ON = 70%

    private static final String BUTTON_CLOSE_TEXT                   = "schliessen";
    private static final String BUTTON_OPEN_TEXT                    = "öffnen";
    private static final String BUTTON_STOP_TEXT                    = "stop";
    public static final String  BUTTON_DOWN_TEXT                    = "ab";
    public static final String  BUTTON_UP_TEXT                      = "auf";

    private static final String ON                                  = "ON";
    private static final String OFF                                 = "OFF";
    private static final String UP                                  = "UP";
    private static final String DOWN                                = "DOWN";
    private static final String STOP                                = "STOP";
    private static final String CLOSED                              = "100"; // percent
    private static final String OPEN                                = "0";   // percent

    private static final String TRACK_SHUTTER_CLOCK_DOZE            = "track-shutter-clock-doze";
    private static final String TRACK_SHUTTER_CLOCK_WAKEUP          = "track-shutter-clock-wakeup";
    private static final String TRACK_SHUTTER_CLOCK_OPEN            = "track-shutter-clock-open";

    private static final int    GARDEN_SHUTTER_MOVING_TIME          = 32000; // ms
    private static final int    SOUTH_EAST_SHUTTER_MOVING_TIME      = 20000; // ms

    private TwoButtonDataHolder mDataHolder;

    private String              mGardenState                        = "";
    private String              mSofaState                          = "";
    private String              mEsszimmerState                     = "";
    private String              mBlumenfensterState                 = "";
    private String              mKuecheState                        = "";
    private String              mSchlafzimmerState                  = "";
    private boolean             mGardenShutterMoving;
    private boolean             mSouthEastShutterMoving;

    private Timer               mGardenShutterMovingTimer;
    private Timer               mSouthEastShutterMovingTimer;
    private Timer               mShutterWakeupTimer;
    private Timer               mShutterWakeupStepTimer;

    private boolean             mIntelligentShowHideModul = true; // todo read from settings

    public ShutterModel( Context ctx) {
        super(ctx, MainActivity.SHUTTER);

        mDataHolder = new ShutterDataHolder();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.SHUTTER);
        intentFilter.addAction(ShutterPreferenceFragment.ACTION_SHUTTER_SETTINGS_CHANGED);
        intentFilter.addAction(TimeAndDateModel.ACTION_DAY_SEGMENT);
        intentFilter.addAction(GARTEN);
        intentFilter.addAction(SOFA);
        intentFilter.addAction(ESSZIMMER);
        intentFilter.addAction(BLUMENFENSTER);
        intentFilter.addAction(KUECHE);
        intentFilter.addAction(SCHLAFZIMMER);
        ctx.registerReceiver(this, intentFilter);


        mGardenShutterMovingTimer = new Timer();
        mSouthEastShutterMovingTimer = new Timer();
    }

    public TwoButtonDataHolder getDataHolder() {
        return mDataHolder;
    }

    protected void initItems() {

        sendItemReadBroadcast(GARTEN);
        sendItemReadBroadcast(SOFA);
        sendItemReadBroadcast(ESSZIMMER);
        sendItemReadBroadcast(BLUMENFENSTER);
        sendItemReadBroadcast(KUECHE);
        sendItemReadBroadcast(SCHLAFZIMMER);

        initShutterWakeup();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive( context, intent);

        String action = intent.getAction();
        String value = intent.getStringExtra(ItemManager2.VALUE);
        if( action != null) {
            if (action.equals(GARTEN))
                handleUpdateGardenShutterState(getCorrectState(value));
            else if (action.equals(SOFA) || action.equals(ESSZIMMER) || action.equals(BLUMENFENSTER) || action.equals(KUECHE))
                handleUpdateSouthEastShutterState(action, getCorrectState(value));
            else if (action.equals(SCHLAFZIMMER))
                handleUpdateSchlafzimmerShutterState(value);
            else if (action.equals(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.SHUTTER))
                handleButtonClicked(intent.getIntExtra(MainListAdapter.KEY_BUTTON_NR, -1));
            else if (action.equals(ShutterPreferenceFragment.ACTION_SHUTTER_SETTINGS_CHANGED))
                handleShutterSettingsChanged();
            else if (action.equals(TimeAndDateModel.ACTION_DAY_SEGMENT)) {
                if (intent.getBooleanExtra(TimeAndDateModel.KEY_LUNCH_TIME, false))
                    handleLunchtime();
                else if (intent.getBooleanExtra(TimeAndDateModel.KEY_SUNSET_TIME, false))
                    handleSunset();
                else if (intent.getBooleanExtra(TimeAndDateModel.KEY_MIDNIGHT, false))
                    handleMidnight();
            }
        }
    }

    private String getCorrectState( String value) {
        if( value != null) {
            if (value.equals("UP") || value.equals("0.0") || value.equals("0"))
                return OPEN;
            else if (value.equals("DOWN") || value.equals("100.0") || value.equals("100"))
                return CLOSED;
        }
        return value;
    }

    private void handleUpdateGardenShutterState(String value) {
        if( value != null) {
            mGardenState = value;
            updateGartenButtonsState();
            Log.d(TAG, "handleUpdateState: itemName=" + GARTEN + ",itemState=" + value);

            if (checkIfAllEGClosed()) {
                handleAllClosed();
                Log.d(TAG, "All shutters in EG are closed");
            } else if (checkIfAllEGOpen()) {
                handleAllOpen();
                Log.d(TAG, "All shutters in EG are open");
            }
        }
    }

    private void handleUpdateSouthEastShutterState( String itemName, String value) {
        if( itemName.equals(SOFA))
            mSofaState = value;
        else if( itemName.equals(ESSZIMMER))
            mEsszimmerState = value;
        else if( itemName.equals(BLUMENFENSTER))
            mBlumenfensterState = value;
        else if( itemName.equals(KUECHE))
            mKuecheState = value;

        updateSouthEastButtonsState();
        Log.d(TAG, "handleUpdateState: itemName=" + itemName + ",itemState=" + value);

        if( checkIfAllEGClosed()) {
            handleAllClosed();
            Log.d(TAG, "All shutters in EG are closed");
        }  else if( checkIfAllEGOpen()) {
            handleAllOpen();
            Log.d(TAG, "All shutters in EG are open");
        }
    }

    private void handleUpdateSchlafzimmerShutterState( String value) {
        mSchlafzimmerState = value;
        Log.d(TAG, "handleUpdateState: itemName=" + SCHLAFZIMMER + ",itemState=" + value);
    }

    private boolean checkIfAllEGClosed() {
        try {
            return mGardenState.equals(CLOSED) && mSofaState.equals(CLOSED) && mEsszimmerState.equals(CLOSED) && mBlumenfensterState.equals(CLOSED) && mKuecheState.equals(CLOSED);
        } catch( Exception ex) {
            Log.w(TAG, ex.getMessage());
            return false;
        }
    }

    private boolean checkIfAllEGOpen() {
        try {
            return mGardenState.equals( OPEN) && mSofaState.equals( OPEN) && mEsszimmerState.equals( OPEN) && mBlumenfensterState.equals( OPEN) && mKuecheState.equals( OPEN);
        } catch( Exception ex) {
            Log.w(TAG, ex.getMessage());
            return false;
        }
    }

    private void handleLunchtime() {
        if (mIntelligentShowHideModul && isModulInList()) {
            sendUpdateListItemBroadcast(false);
            Log.d(TAG, "Its Lunchtime, Modul Shutter auto remove from list");
        }
    }

    private void handleSunset() {
        if (mIntelligentShowHideModul && !isModulInList()) {
            sendUpdateListItemBroadcast( true);
            Log.d(TAG, "Its Sunset, Modul Shutter auto show in list");
        }
    }

    private void handleMidnight() {
        if( mIntelligentShowHideModul && ! isModulInList()) {
            sendUpdateListItemBroadcast( true);
            Log.d(TAG, "Its midnight, Modul Shutter auto show in list");
        }
    }

    private void handleButtonClicked( int btnNr) {
        if( btnNr <=2)
            handleEastSouthButtonClicked( btnNr);
        else
            handleGartenButtonClicked( btnNr);
    }

    private void handleShutterSettingsChanged() {
        if( mShutterWakeupTimer != null)
            mShutterWakeupTimer.cancel();

        initShutterWakeup();
    }

    private void updateGartenButtonsState() {
        if( mGardenShutterMoving)
            showGardenStopButton();  // moving
        else if( mGardenState != null && mGardenState.equals( OPEN))
            showGardenStateOpen(); // // normal state open
        else if( mGardenState != null && mGardenState.equals( CLOSED))
            showGardenStateClosed(); // normal state closed
        else
            showGardenUpAndDownButtons(); // stopped or unknown

        String infoTxt = "";
        try {
            infoTxt = getInfoText();
        } catch(Exception ex) {
            infoTxt = ex.getMessage();
        }
        mDataHolder.setInfo( infoTxt);
        sendUpdateListItemBroadcast();
    }

    private void updateSouthEastButtonsState() {
        if( mSouthEastShutterMoving)
            showSouthEastStopButton();  // moving
        else if( mSofaState !=null && mSofaState.equals( OPEN) && mEsszimmerState != null && mEsszimmerState.equals( OPEN) && mBlumenfensterState != null && mBlumenfensterState.equals( OPEN) && mKuecheState != null && mKuecheState.equals( OPEN))
            showSouthEastStateOpen(); // // normal state open
        else if( mSofaState != null && mSofaState.equals( CLOSED) && mEsszimmerState != null && mEsszimmerState.equals( CLOSED) && mBlumenfensterState != null && mBlumenfensterState.equals( CLOSED) && mKuecheState != null && mKuecheState.equals( CLOSED))
            showSouthEastStateClosed(); // normal state closed
        else
            showSouthEastUpAndDownButtons(); // stopped or unknown

        String infoTxt = "";
        try {
            infoTxt = getInfoText();
        } catch(Exception ex) {
            infoTxt = ex.getMessage();
        }
        mDataHolder.setInfo( infoTxt);
        sendUpdateListItemBroadcast();
    }

    private String getInfoText() throws Exception {
        if( mGardenShutterMoving && mSouthEastShutterMoving) {
            if( mGardenState.equals(CLOSED)) // only check garden shutter if all close or all open
                return "Alle schliessen...";
            else if( mGardenState.equals(OPEN))
                return "Alle öffnen...";
        }
        else if( ! mGardenShutterMoving && ! mSouthEastShutterMoving) {
            if (mGardenState.equals(CLOSED) && mSofaState.equals(CLOSED) && mEsszimmerState.equals(CLOSED) && mBlumenfensterState.equals(CLOSED) && mKuecheState.equals(CLOSED))
                return "Alle geschlossen";
            else if (mGardenState.equals(OPEN) && mSofaState.equals(OPEN) && mEsszimmerState.equals(OPEN) && mBlumenfensterState.equals(OPEN) && mKuecheState.equals(OPEN))
                return "Alle offen";
        }
        else if( mGardenShutterMoving && ! mSouthEastShutterMoving) {
            if( mGardenState.equals(CLOSED))
                return "Garten schliesst...";
            else if(mGardenState.equals(OPEN))
                return "Garten öffnet...";
        }
        else if( ! mGardenShutterMoving && mSouthEastShutterMoving) {
            if( mSofaState.equals(CLOSED)) // only check sofa shutter if south and east are moving up or down
                return "Süd+Ost schliessen...";
            else if( mSofaState.equals(OPEN))
                return "Süd+Ost öffnen...";
        }
        return "-";
    }

    private void handleAllClosed() {
        Log.d(TAG, "handleAllClosed");
        if( mIntelligentShowHideModul && isModulInList() && isPM()) {
            startTimerForHideModul( 60);
            Log.d(TAG, "handle All shutters Closed, Modul Shutter auto remove from list");
        }
    }

    private void handleAllOpen() {
        Log.d(TAG, "handleAllOpen");
        if( mIntelligentShowHideModul && isModulInList() && isAM()) {
            startTimerForHideModul( 60);
            Log.d(TAG, "handle All shutters Open, Modul Shutter auto remove from list");
        }
    }

    private boolean isWeakupForToday() {
        int weekdayToday = new GregorianCalendar().get( Calendar.DAY_OF_WEEK);
        HashSet<String> list = (HashSet<String>)getPrefs().getStringSet(ShutterPreferenceFragment.WAKEUP_DAYS, null);
        return list.contains(String.valueOf(weekdayToday));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // shows button state
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // garden
    private void showGardenStateOpen() {
        mDataHolder.setButtonText(TwoButtonDataHolder.B2, BUTTON_CLOSE_TEXT);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B2, true);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B21, false);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B22, false);
    }

    private void showGardenStateClosed() {
        mDataHolder.setButtonText(TwoButtonDataHolder.B2, BUTTON_OPEN_TEXT);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B2, true);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B21, false);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B22, false);
    }

    private void showGardenStopButton() {
        mDataHolder.setButtonText(TwoButtonDataHolder.B2, BUTTON_STOP_TEXT);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B2, true);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B21, false);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B22, false);
    }

    private void showGardenUpAndDownButtons() {
        mDataHolder.setButtonText(TwoButtonDataHolder.B21, BUTTON_UP_TEXT);
        mDataHolder.setButtonText(TwoButtonDataHolder.B22, BUTTON_DOWN_TEXT);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B2, false);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B21, true);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B22, true);
    }
    // south and east
    private void showSouthEastStateOpen() {
        mDataHolder.setButtonText(TwoButtonDataHolder.B1, BUTTON_CLOSE_TEXT);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B1, true);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B11, false);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B12, false);
    }

    private void showSouthEastStateClosed() {
        mDataHolder.setButtonText(TwoButtonDataHolder.B1, BUTTON_OPEN_TEXT);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B1, true);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B11, false);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B12, false);
    }

    private void showSouthEastStopButton() {
        mDataHolder.setButtonText(TwoButtonDataHolder.B1, BUTTON_STOP_TEXT);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B1, true);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B11, false);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B12, false);
    }

    private void showSouthEastUpAndDownButtons() {
        mDataHolder.setButtonText(TwoButtonDataHolder.B11, BUTTON_UP_TEXT);
        mDataHolder.setButtonText(TwoButtonDataHolder.B12, BUTTON_DOWN_TEXT);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B1, false);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B11, true);
        mDataHolder.setButtonVisible(TwoButtonDataHolder.B12, true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // handle button clicks
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void handleGartenButtonClicked(int btnNr) {
        String currBtnTxt = mDataHolder.getButtonText(TwoButtonDataHolder.B2);
        if (currBtnTxt.equals(BUTTON_CLOSE_TEXT) || btnNr == TwoButtonDataHolder.B22)
            handleGartenButtonCloseClicked();
        else if (currBtnTxt.equals(BUTTON_OPEN_TEXT) || btnNr == TwoButtonDataHolder.B21)
            handleGartenButtonOpenClicked();
        else if (currBtnTxt.equals(BUTTON_STOP_TEXT))
            handleGartenButtonStopClicked();

        sendUpdateListItemBroadcast();
    }

    private void handleGartenButtonOpenClicked() {
        Log.i(TAG, "GartenButtonOpenClicked");
        mGardenShutterMoving = true;
        sendItemCmdBroadcast( GARTEN, UP);
        mDataHolder.setInfo( "Garten öffnet...");
        showGardenStopButton();
        sendUpdateListItemBroadcast();
        mGardenShutterMovingTimer.schedule( new GardenShutterMovingTimerTask(), GARDEN_SHUTTER_MOVING_TIME);
    }

    private void handleGartenButtonCloseClicked() {
        Log.i(TAG, "GartenButtonCloseClicked");
        mGardenShutterMoving = true;
        sendItemCmdBroadcast( GARTEN, DOWN);
        mDataHolder.setInfo( "Garten schliesst...");
        showGardenStopButton();
        sendUpdateListItemBroadcast();
        mGardenShutterMovingTimer.schedule( new GardenShutterMovingTimerTask(), GARDEN_SHUTTER_MOVING_TIME);
    }

    private void handleGartenButtonStopClicked() {
        Log.i(TAG, "GartenButtonStopClicked");
        mGardenShutterMoving = false;
        sendItemCmdBroadcast( GARTEN, STOP);
        mDataHolder.setInfo( "gestoppt");
        showGardenUpAndDownButtons();
        sendUpdateListItemBroadcast();
    }

    private void handleEastSouthButtonClicked( int btnNr) {
        String currBtnTxt = mDataHolder.getButtonText(TwoButtonDataHolder.B1);
        if (currBtnTxt.equals(BUTTON_CLOSE_TEXT) || btnNr == TwoButtonDataHolder.B12)
            handleSouthEastButtonCloseClicked();
        else if (currBtnTxt.equals(BUTTON_OPEN_TEXT) || btnNr == TwoButtonDataHolder.B11)
            handleSouthEastButtonOpenClicked();
        else if (currBtnTxt.equals(BUTTON_STOP_TEXT))
            handleSouthEastButtonStopClicked();

        sendUpdateListItemBroadcast();
    }

    private void handleSouthEastButtonOpenClicked() {
        Log.i(TAG, "SouthEastButtonOpenClicked");
        mSouthEastShutterMoving = true;
        sendItemCmdBroadcast( SOFA, UP);
        sendItemCmdBroadcast( ESSZIMMER, UP);
        sendItemCmdBroadcast( BLUMENFENSTER, UP);
        sendItemCmdBroadcast( KUECHE, UP);
        mDataHolder.setInfo( "Süd+Ost öffnen...");
        showSouthEastStopButton();
        sendUpdateListItemBroadcast();
        mSouthEastShutterMovingTimer.schedule( new SouthEastShutterMovingTimerTask(), SOUTH_EAST_SHUTTER_MOVING_TIME);
    }

    private void handleSouthEastButtonCloseClicked() {
        Log.i(TAG, "SouthEastButtonCloseClicked");
        mSouthEastShutterMoving = true;
        sendItemCmdBroadcast( SOFA, DOWN);
        sendItemCmdBroadcast( ESSZIMMER, DOWN);
        sendItemCmdBroadcast( BLUMENFENSTER, DOWN);
        sendItemCmdBroadcast( KUECHE, DOWN);
        mDataHolder.setInfo( "Süd+Ost schliessen...");
        showSouthEastStopButton();
        sendUpdateListItemBroadcast();
        mSouthEastShutterMovingTimer.schedule( new SouthEastShutterMovingTimerTask(), SOUTH_EAST_SHUTTER_MOVING_TIME);
    }

    private void handleSouthEastButtonStopClicked() {
        Log.i(TAG, "SouthEastButtonStopClicked");
        mSouthEastShutterMoving = false;
        sendItemCmdBroadcast( SOFA, STOP);
        sendItemCmdBroadcast( ESSZIMMER, STOP);
        sendItemCmdBroadcast( BLUMENFENSTER, STOP);
        sendItemCmdBroadcast( KUECHE, STOP);
        mDataHolder.setInfo( "gestoppt");
        showSouthEastUpAndDownButtons();
        sendUpdateListItemBroadcast();
    }

    private void initShutterWakeup() {
        if( getPrefs().getBoolean( PREF_SHUTTER_WAKEUP_SWITCH, false)) {
            Date startDateWakeup = getShutterWakeupStart( 0);
            if( startDateWakeup.before( new Date()))
                startDateWakeup = getShutterWakeupStart( 1);

            mShutterWakeupTimer = new Timer();
            mShutterWakeupTimer.schedule(new ShutterWakeupTimerTask(TRACK_SHUTTER_CLOCK_DOZE), startDateWakeup);
            sendSystemBroadcast(SystemModel.ACTION_LOG, getClass().getName(), "scheduled shutter wakeup", getDateTimeFormatter().format(startDateWakeup));
            Log.i(TAG, "scheduled shutter wakeup [" + getDateTimeFormatter().format(startDateWakeup) + "]");
        }
    }

    private Date getShutterWakeupStart(int addDays) {
        String timeAsString = getPrefs().getString(PREF_SHUTTER_WAKEUP_TIME, "08:00");
        String[] timeHrMin = timeAsString.split( ":");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeHrMin[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeHrMin[1]));
        calendar.set(Calendar.SECOND, 0);
        if( addDays > 0)
            calendar.add( Calendar.DAY_OF_YEAR, addDays);
        return calendar.getTime();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Timertask for Shutter Moving
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class GardenShutterMovingTimerTask extends TimerTask {

        public void run() {
            mGardenShutterMoving = false;
            sendItemReadBroadcast(GARTEN);
        }
    }

    private class SouthEastShutterMovingTimerTask extends TimerTask {

        public void run() {
            mSouthEastShutterMoving = false;
            sendItemReadBroadcast(SOFA);
            sendItemReadBroadcast(ESSZIMMER);
            sendItemReadBroadcast(BLUMENFENSTER);
            sendItemReadBroadcast(KUECHE);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Timertask for Shutter Wakeup
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class ShutterWakeupTimerTask extends TimerTask {

        private String mStep;

        public ShutterWakeupTimerTask( String step) {
            mStep = step;
        }

        public void run() {
            sendSystemBroadcast(SystemModel.ACTION_LOG, getClass().getName(), "running shutter wakeup [step:" + mStep + ",SchlafzimmerState:" + mSchlafzimmerState + "]", getDateTimeFormatter().format(new Date()));
            Log.i(TAG, "running shutter wakeup [step:" + mStep + ",mSchlafzimmerState:" + mSchlafzimmerState + "] " + getDateTimeFormatter().format(new Date()));

            if( mStep == TRACK_SHUTTER_CLOCK_DOZE) {
                // schedule wakeup for the next day
                initShutterWakeup();
                if( isWeakupForToday())
                    doShutterSleeproomDoze(mSchlafzimmerState);
                else
                    Log.i(TAG, "today is not a day for shutter wakeup");
            }
            else if( mStep == TRACK_SHUTTER_CLOCK_WAKEUP)
                doShutterSleeproomWakeup(mSchlafzimmerState);
            else if( mStep == TRACK_SHUTTER_CLOCK_OPEN)
                doShutterSleeproomOpen(mSchlafzimmerState);
        }
    }


    private void doShutterSleeproomDoze( String schlafzimmerState) {
        if( schlafzimmerState == null || schlafzimmerState.equals( CLOSED)|| schlafzimmerState.equals("NULL")) {
            sendItemCmdBroadcast( SCHLAFZIMMER_PRESET12, ON);
            Log.i(TAG, "SchlafzimmerPreset12 sendCmdOn");
            int stepTimeMinutes = Integer.valueOf( getPrefs().getString( PREF_SHUTTER_WAKEUP_TIME_STEP, "1"));
            mShutterWakeupStepTimer = new Timer();
            mShutterWakeupStepTimer.schedule( new ShutterWakeupTimerTask(TRACK_SHUTTER_CLOCK_WAKEUP), stepTimeMinutes * 60000);
        }
    }

    private void doShutterSleeproomWakeup( String schlafzimmerState) {
        if( ! schlafzimmerState.equals( CLOSED) && ! schlafzimmerState.equals( OPEN)) { // must be between fully closed and fully open
            sendItemCmdBroadcast( SCHLAFZIMMER_PRESET34, ON);
            Log.i(TAG, "SchlafzimmerPreset34 sendCmdOn");
            int stepTimeMinutes = Integer.valueOf( getPrefs().getString( PREF_SHUTTER_WAKEUP_TIME_STEP, "1"));
            mShutterWakeupStepTimer = new Timer();
            mShutterWakeupStepTimer.schedule( new ShutterWakeupTimerTask(TRACK_SHUTTER_CLOCK_OPEN), stepTimeMinutes * 60000);
        }
    }

    private void doShutterSleeproomOpen( String schlafzimmerState) {
        if( ! schlafzimmerState.equals( CLOSED) && ! schlafzimmerState.equals( OPEN)) { // must be between fully closed and fully open
            sendItemCmdBroadcast( SCHLAFZIMMER, UP);
            Log.i(TAG, "mSchlafzimmer sendCmdUp");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
