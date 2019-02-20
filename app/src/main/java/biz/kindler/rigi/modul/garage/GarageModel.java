package biz.kindler.rigi.modul.garage;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.OneButtonDataHolder;
import biz.kindler.rigi.modul.clock.TimeAndDateModel;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import biz.kindler.rigi.modul.weatherstation.WeatherstationModel;
import biz.kindler.rigi.settings.GaragePreferenceFragment;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 29.11.16.
 */

public class GarageModel extends BaseModel {

    private final static String TAG = GarageModel.class.getSimpleName();

    // item Names
    public final static String GARAGE_TOR_STATE = "Garage_Tor_Status";
    private final static String GARAGE_TOR = "Garage_Tor";
    private final static String GARAGE_LIGHT = "Garage_Licht";
    // Door state
    private final static int UNKNOWN = 0;
    private final static int CLOSED = 1;
    private final static int CLOSING = 2;
    private final static int OPEN = 3;
    private final static int OPENING = 4;
    // Button Text
    private final static String BUTTON_TEXT_STOP = "stop";
    private final static String BUTTON_TEXT_OPEN = "öffnen";
    private final static String BUTTON_TEXT_CLOSE = "schliessen";

    private final static int TOR_MOVING_TIME = 15000; // time for opening or closing in ms

    private OneButtonDataHolder mDataHolder;
    private int                 mTorState;
    private Timer               mTorMovingTimer;
    private String              mLightState;
    private boolean             mTorStopped;  // between closed and open
    private boolean             mIntelligentShowHideModul = true; // todo read from settings


    public GarageModel(Context ctx) {
        super(ctx,MainActivity.GARAGE);

        mDataHolder = new GarageDataHolder();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TimeAndDateModel.ACTION_DAY_SEGMENT);
        intentFilter.addAction(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.GARAGE);
        intentFilter.addAction(MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.GARAGE);
        intentFilter.addAction(GaragePreferenceFragment.ACTION_GARAGE_SETTINGS_CHANGED);
        intentFilter.addAction(GARAGE_TOR_STATE);
        intentFilter.addAction(GARAGE_LIGHT);

        ctx.registerReceiver(this, intentFilter);
    }

    public OneButtonDataHolder getDataHolder() {
        return mDataHolder;
    }

    protected void initItems() {
        sendItemReadBroadcast(GARAGE_TOR_STATE);
        sendItemReadBroadcast(GARAGE_LIGHT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive( context, intent);

        String action = intent.getAction();
        String value = intent.getStringExtra(ItemManager2.VALUE);

        Log.d(TAG, "action: " + action + ", value: " + (value == null ? "null" : value));

        if (action.equals( GARAGE_TOR_STATE))
            handleTorState(value);
        else if (action.equals( GARAGE_LIGHT))
            handleLightState(value);
        else if(action.equals(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.GARAGE))
            handleTorButtonClick();
        else if (action.equals(MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.GARAGE))
            handleOnGaragePanelClicked();
        else if (action.equals(TimeAndDateModel.ACTION_DAY_SEGMENT)) {
            if( intent.getBooleanExtra(TimeAndDateModel.KEY_LUNCH_TIME, false))
                handleLunchtime();
            else if( intent.getBooleanExtra(TimeAndDateModel.KEY_MIDNIGHT, false))
                handleMidnight();
        }
        else if( action.equals(GaragePreferenceFragment.ACTION_GARAGE_SETTINGS_CHANGED))
            checkForSwitchLight();
    }

    private void handleLunchtime() {
        if( mIntelligentShowHideModul && isModulInList()) {
            startTimerForHideModul( 1);
            Log.d(TAG, "Its lunchtime, Modul Garage auto removed from list");
        }
    }

    private void handleMidnight() {
        if( mIntelligentShowHideModul && ! isModulInList()) {
            sendUpdateListItemBroadcast( true);
            Log.d(TAG, "Its midnight, Modul Garage auto show in list");
        }
    }

    private void handleTorButtonClick() {
        Log.d(TAG, "handleTorButtonClick [mTorState:" + mTorState + "," + getTorStateAsText(mTorState) + "]");

        if( mTorState == OPEN) {
            handleTorClosingStart();
            doSendTorCmd();
        }
        else if( mTorState == CLOSING || mTorState == OPENING) {
            if( mTorStopped) {
                if( mTorState == OPENING)
                    handleTorClosingStart();
                else if( mTorState == CLOSING)
                    handleTorOpeningStart();
            }
            else
                handleTorStopRequest();
        }
        else
            doSendTorCmd();
    }

    private void doSendTorCmd() {
        sendItemCmdBroadcast( GARAGE_TOR, "ON");  // only ON, never send OFF
        Log.d(TAG, "doSendTorCmd (ON)");
    }

    private void handleTorState(String itemState) {
        if (itemState.equals("ON") && mTorState == CLOSED)
            handleTorOpeningStart();
        else if (itemState.equals("ON") && mTorState == UNKNOWN)  // when start app and tor is already open
            handleTorOpeningFinished();
        else if (itemState.equals("OFF") && mTorState != CLOSED)
            handleTorClosingFinished();
        Log.d(TAG, "handleTorStateAction: itemState=" + itemState);
    }

    private void handleTorOpeningStart() {
        mTorState = OPENING;
        mTorStopped = false;
        checkForSwitchLight();
        mTorMovingTimer = new Timer();
        mTorMovingTimer.schedule( new OpenTorTimerTask(), TOR_MOVING_TIME);
        mDataHolder.setButtonText(BUTTON_TEXT_STOP);
        mDataHolder.setHighlighted(true);
        mDataHolder.setInfo(getInfoText());
        sendUpdateListItemBroadcast( true);
        Log.d(TAG, "handleTorOpeningStart");
    }

    private void handleTorOpeningFinished() {
        mTorState = OPEN;
        mTorStopped = false;
        mDataHolder.setButtonText(BUTTON_TEXT_CLOSE);
        mDataHolder.setHighlighted(true);
        mDataHolder.setInfo(getInfoText());
        sendUpdateListItemBroadcast( true);
        if( getPrefs().getBoolean(GaragePreferenceFragment.PLAY_SOUND, false))
            playNofificationSound( getContext());
        sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "Garage", "offen");
        Log.i(TAG, "handleTorOpeningFinished");
    }

    private void handleTorClosingStart() {
        mTorState = CLOSING;
        mTorStopped = false;
        mDataHolder.setButtonText(BUTTON_TEXT_STOP);
        mDataHolder.setInfo(getInfoText());
        sendUpdateListItemBroadcast();
        Log.d(TAG, "handleTorClosingStart");
    }

    private void handleTorClosingFinished() {
        mTorState = CLOSED;
        mTorStopped = false;
        checkForSwitchLight();
        mDataHolder.setButtonText(BUTTON_TEXT_OPEN);
        mDataHolder.setHighlighted(false);
        mDataHolder.setInfo(getInfoText());
        sendUpdateListItemBroadcast();
        sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "Garage", "geschlossen");
        Log.i(TAG, "handleTorClosingFinished");
        if( mIntelligentShowHideModul && isModulInList())
            startTimerForHideModul( 60);
    }

    private void handleTorStopRequest() {
        mTorStopped = true;
        doSendTorCmd();

        if( mTorState == OPENING) {
            mDataHolder.setButtonText(BUTTON_TEXT_CLOSE);
            mTorMovingTimer.cancel();
        }
        else if( mTorState == CLOSING)
            mDataHolder.setButtonText(BUTTON_TEXT_OPEN);

        mDataHolder.setInfo( "Tor gestoppt!");
        sendUpdateListItemBroadcast();
        sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "Garage", "stop");
        Log.d(TAG, "handleTorStopRequest");
    }

    private void checkForSwitchLight() {
        String lightMode = getLightMode();
        if( mTorState == OPENING || mTorState == OPEN) {
            if( lightMode.equals( "auto") || ((lightMode.equals( "auto2") && isNowNight()))) {
                Log.d(TAG, "switch light on [tor opening, lightmode: " + lightMode + "]");
                sendItemCmdBroadcast( GARAGE_LIGHT, "ON");
            }
        }
        else if( mTorState == CLOSED) {
            if( lightMode.equals( "auto") || (lightMode.equals( "auto2"))) {  // always switch of in auto mode, even its not switched on
                Log.d(TAG, "switch light off [tor closed, lightmode: " + lightMode + "]");
                sendItemCmdBroadcast( GARAGE_LIGHT, "OFF");
            }
        }
    }

    private void handleLightState( String state) {
        mLightState = state;
        updateInfoText();
    }

    private void updateInfoText() {
        mDataHolder.setInfo(getInfoText());
        sendUpdateListItemBroadcast();
    }

    private void handleOnGaragePanelClicked() {

        new SweetAlertDialog(getContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(getContext().getResources().getString(R.string.garage_man_switch_dialog))
                //.setContentText("It's pretty, isn't it?")
                .setCustomImage(R.drawable.garage)
                .setConfirmButton("Ein", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        sendItemCmdBroadcast( GARAGE_LIGHT, "ON");
                    }
                })
                .setCancelButton("Aus", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        sendItemCmdBroadcast( GARAGE_LIGHT, "OFF");
                    }
                })
                .show();
    }

    private String getInfoText() {
        switch (mTorState) {
            case UNKNOWN:
                return "-" + getLightState() + getLightModeAsText( getLightMode());
            case OPENING:
                return "Tor öffnet...";
            case OPEN:
                return "offen" + getLightState() + getLightModeAsText( getLightMode());
            case CLOSING:
                return "Tor schliesst...";
            case CLOSED:
                return "geschlossen" + getLightState() + getLightModeAsText( getLightMode());
            default:
                return "?";
        }
    }

    private String getTorStateAsText(int doorState) {
        switch( doorState) {
            case UNKNOWN:
                return "UNKNOWN";
            case CLOSED:
                return "CLOSED";
            case CLOSING:
                return "CLOSING";
            case OPEN:
                return "OPEN";
            case OPENING:
                return "OPENING";
            default:
                return "?";
        }
    }

    private String getLightState() {
        if( mLightState == null || mLightState.length() == 0 || mLightState.equals( "NULL"))
            return "";
        else if (mLightState.equals( "ON"))
            return", Licht ein";
        else if( mLightState.equals( "OFF"))
            return", Licht aus";
        else
            return "";
    }

    private String getLightModeAsText( String mode) {
        if(mode.equals( "man"))
            return " [manuell]";
        else if( mode.equals("auto"))
            return " [automatik]";
        else if( mode.equals("auto2"))
            return " [automatik / LUX]";
        else
            return "";
    }

    // "man" "auto" "auto2"
    private String getLightMode() {
        return getPrefs().getString( GaragePreferenceFragment.LIGHT_MODE, getContext().getResources().getStringArray( R.array.lightmode_values)[0]);
    }

    private boolean isNowNight() {
        return getPrefs().getBoolean(WeatherstationModel.NOW_NIGHT_BY_LUX, false);
    }

    public static void playNofificationSound( Context ctx) {
        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);  // TYPE_NOTIFICATION
        Ringtone r = RingtoneManager.getRingtone(ctx, soundUri);
        r.play();
        Log.d(TAG, "playNofificationSound");
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Timertask
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class OpenTorTimerTask extends TimerTask {

        public void run() {
            if( mTorState == OPENING)
                handleTorOpeningFinished();
        }
    }
}