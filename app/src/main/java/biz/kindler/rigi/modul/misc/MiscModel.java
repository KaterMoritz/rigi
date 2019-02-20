package biz.kindler.rigi.modul.misc;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.text.SimpleDateFormat;
import java.util.Date;

import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.weatherstation.WeatherstationModel;
import biz.kindler.rigi.settings.LightPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 21.03.17.
 */

public class MiscModel extends BaseModel {

    private final static String TAG = MiscModel.class.getSimpleName();

    // Item names
    private static final String KUECHE_MANUELL                              = "Licht_Kueche_ManAuto";
    private static final String KUECHE_STATUS                               = "Licht_Kueche_Status";
    private static final String OG_NACHTMODE                                = "OG_Nachtmode";
    private static final String BUERO_MANUELL                               = "Licht_OG_Buero_Decke_ManAuto";
    private static final String BUERO_STATUS                                = "Licht_OG_Buero_Decke_Status"; // "Licht_OG_Buero_Decke_Switch"; // !! TODO in OpenHAB item
    private static final String KINDERZIMMER_MANUELL                        = "Licht_OG_Kinderzimmer_ManAuto";
    private static final String KINDERZIMMER_STATUS                         = "Licht_OG_Kinderzimmer_Status"; // "Licht_OG_Kinderzimmer";       // !! TODO in OpenHAB item
    private static final String BEWEGUNG_WOHNZIMMER                         = "Bewegung_Wohnzimmer";
    private static final String HELLIGKEIT_WOHNZIMMER                       = "Helligkeit_Wohnzimmer";
    private static final String SWITCH_MITTELWAND                           = "Mittelwand";

    private static final float SENSOR_MAX_LUX                               = 1800; // eff its 2000lux (see:

    public static final String  PREF_DAYLIGHT_FOR_KITCHEN_SENSOR_SWITCH     = "pref_daylight_for_kitchen_sensor_switch";

    private boolean mKuecheReqForMan;
    private boolean mBueroReqForMan;
    private boolean mKinderzimmerReqForMan;
  //  private GregorianCalendar mOnTimeGCal;
   // private GregorianCalendar mOffTimeGCal;
    private String  mLastValueBewegungWohnzimmer = "";
    private int mIdleCntBewegungWohnzimmer;
    private boolean mMittelwandSwitchState;
    private boolean mSensorLuxAboveOperating;
    private float mWohnzimmerLux;
    private SimpleDateFormat mDateTimeFormatter = new SimpleDateFormat("HH:mm");


    public MiscModel( Context ctx) {
        super( ctx, MainActivity.MISC);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WeatherstationModel.ACTION_LUX_LEVEL);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
      //  intentFilter.addAction(TimeAndDateModel.ACTION_DAY_SEGMENT);
        intentFilter.addAction(BEWEGUNG_WOHNZIMMER);
        intentFilter.addAction(HELLIGKEIT_WOHNZIMMER);
        intentFilter.addAction(KUECHE_STATUS);
        intentFilter.addAction(BUERO_STATUS);
        intentFilter.addAction(KINDERZIMMER_STATUS);
        ctx.registerReceiver(this, intentFilter);
    }

    protected void initItems() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive( context, intent);

        String action = intent.getAction();

        if (action.equals(WeatherstationModel.ACTION_LUX_LEVEL))
            handleLuxLevelReached( intent.hasExtra(WeatherstationModel.SUNRISE_BY_LUX), intent.hasExtra(WeatherstationModel.SUNSET_BY_LUX));
        else if( action.equals( Intent.ACTION_TIME_TICK))
            handleTimeTick();
        else if( action.equals(KUECHE_STATUS))
            handleKuecheStatus( intent.getStringExtra(ItemManager2.VALUE));
        else if( action.equals(BUERO_STATUS))
            handleBueroStatus( intent.getStringExtra(ItemManager2.VALUE));
        else if( action.equals(KINDERZIMMER_STATUS))
            handleKinderzimmerStatus( intent.getStringExtra(ItemManager2.VALUE));
      //  else if( action.equals(TimeAndDateModel.ACTION_DAY_SEGMENT))
      //      handleDaySegmentChanged( intent.getBooleanExtra( TimeAndDateModel.KEY_MIDNIGHT, false), intent.getBooleanExtra( TimeAndDateModel.KEY_LUNCH_TIME, false));
        else if( action.equals( BEWEGUNG_WOHNZIMMER))
            handleBewegungWohnzimmer( intent.getStringExtra(ItemManager2.VALUE));
        else if( action.equals( HELLIGKEIT_WOHNZIMMER))
            handleHelligkeitWohnzimmer( intent.getStringExtra(ItemManager2.VALUE));
    }

    private void handleLuxLevelReached( boolean sunrise, boolean sunset) {
        if (sunrise)
            handleSunriseForSensor();
        else if (sunset)
            handleSunsetForSensor();
    }

    private void handleTimeTick() {
        handleTimetickForKueche();
        handleTimetickForOG();
        handleTimeTickForBewegungWohnzimmer();
    }

    private void handleTimetickForKueche() {
        if( mKuecheReqForMan) {
            sendItemReadBroadcast(KUECHE_STATUS);
            Log.i(TAG, "ACTION_TIME_TICK, sendItemReadBroadcast:KUECHE_DIMMER [mKuecheReqForMan: " + mKuecheReqForMan + "]");
        }
    }

    private void handleTimetickForOG() {
        if( ! mBueroReqForMan && ! mKinderzimmerReqForMan && isNightModeSwitchOn() && isNowTimeForStartNightmode()) {
            mBueroReqForMan = true;
            mKinderzimmerReqForMan = true;
            Log.i(TAG, "ACTION_TIME_TICK, set mBueroReqForMan and mKinderzimmerReqForMan to true");
        }
        if( mBueroReqForMan)
            sendItemReadBroadcast(BUERO_STATUS);
        if( mKinderzimmerReqForMan)
            sendItemReadBroadcast(KINDERZIMMER_STATUS);

        if( isNightModeSwitchOn() && isNowTimeForEndNightmode()) {
            sendItemCmdBroadcast( BUERO_MANUELL, "OFF");
            sendItemCmdBroadcast( KINDERZIMMER_MANUELL, "OFF");
        }
    }

    private boolean isNightModeSwitchOn() {
        return getPrefs().getBoolean( LightPreferenceFragment.PREF_NIGHTMODE_OG_SWITCH, false);
    }

    private boolean isNowTimeForStartNightmode() {
        String onTime = getPrefs().getString(LightPreferenceFragment.PREF_NIGHTMODE_ON_TIME, "23:00");
        String now = mDateTimeFormatter.format( new Date());
        return onTime.equals( now);
    }

    private boolean isNowTimeForEndNightmode() {
        String offTime = getPrefs().getString(LightPreferenceFragment.PREF_NIGHTMODE_OFF_TIME, "08:00");
        String now = mDateTimeFormatter.format( new Date());
        return offTime.equals( now);
    }

    private void handleTimeTickForBewegungWohnzimmer() {
        mIdleCntBewegungWohnzimmer++;

        Log.d(TAG, "handleTimeTickForBewegungWohnzimmer (" + mIdleCntBewegungWohnzimmer + ")");

        if( isMittelwandSwitchAuto() && mIdleCntBewegungWohnzimmer == getMittelwandOnDuration() && ! mSensorLuxAboveOperating) {
            Log.i(TAG, "send SWITCH_WOHNZIMMER: OFF");
            sendItemCmdBroadcast( SWITCH_MITTELWAND, "OFF");
            mMittelwandSwitchState = false;
        }
    }

    private void handleHelligkeitWohnzimmer( String luxValue) {
        try {
            float newLuxVal = Float.parseFloat(luxValue);

            if( newLuxVal > mWohnzimmerLux && newLuxVal > SENSOR_MAX_LUX) {
                mSensorLuxAboveOperating = true;
                if( ! mMittelwandSwitchState) {
                    sendItemCmdBroadcast(SWITCH_MITTELWAND, "ON");
                    mMittelwandSwitchState = true;
                    Log.i(TAG, "Wohnzimmer LUX level above sensor limit (2kLux) [curr: " + luxValue + " lx] switch SWITCH_MITTELWAND to ON");
                }
            }
            /*
            else if( newLuxVal < mWohnzimmerLux && newLuxVal < SENSOR_MAX_LUX) {
                mSensorLuxAboveOperating = false;
                if( mMittelwandSwitchState) {
                    sendItemCmdBroadcast(SWITCH_MITTELWAND, "OFF");
                    mMittelwandSwitchState = false;
                    Log.i(TAG, "Wohnzimmer LUX level below sensor limit (2kLux)  [curr: " + luxValue + " lx] switch SWITCH_MITTELWAND to OFF");
                }
            } */
            mWohnzimmerLux = newLuxVal;
        } catch(Exception ex) {
            Log.w( TAG, ex.getMessage());
        }
    }

    private void handleKuecheStatus( String value) {
        Log.i(TAG, "KUECHE_STATUS value: " + value);

        if( value.equals( "OFF") && mKuecheReqForMan) {
            mKuecheReqForMan = false;
            sendItemCmdBroadcast( KUECHE_MANUELL, "ON");
            Log.i(TAG, "KUECHE_DIMMER is OFF and mRequestForManuell true: sendItemCmdBroadcast KUECHE_MANUELL ON");
        }
    }

    private void handleBueroStatus( String value) {
        Log.i(TAG, "BUERO_STATUS value: " + value);
        if( mBueroReqForMan && value.equals( "OFF")) {
            mBueroReqForMan = false;
            sendItemCmdBroadcast( BUERO_MANUELL, "ON");
            Log.i(TAG, "sendItemCmdBroadcast BUERO_MANUELL ON (BUERO_STATUS is OFF)");
        }
    }

    private void handleKinderzimmerStatus( String value) {
        Log.i(TAG, "KINDERZIMMER_STATUS value: " + value);
        if( mKinderzimmerReqForMan && value.equals( "OFF")) {
            mKinderzimmerReqForMan = false;
            sendItemCmdBroadcast( KINDERZIMMER_MANUELL, "ON");
            Log.i(TAG, "sendItemCmdBroadcast KINDERZIMMER_MANUELL ON (KINDERZIMMER_STATUS is OFF)");
        }
    }
/*
    private void handleDaySegmentChanged( boolean midnight, boolean lunchtime) {
        if( midnight) {
            mOnTimeGCal = null;
            Log.d(TAG, "its midnight, set mOnTimeGCal to null");
        }
        if( lunchtime) {
            mOffTimeGCal = null;
            Log.d(TAG, "its lunchtime, set mOffTimeGCal to null");
        }
    } */

    private void handleBewegungWohnzimmer( String value) {
        if( ! mLastValueBewegungWohnzimmer.equals(value)) {
            mLastValueBewegungWohnzimmer = value;
            if (value.equals("ON")) {
                mIdleCntBewegungWohnzimmer = 0;

                if( ! mMittelwandSwitchState) {
                    Log.i(TAG, "send SWITCH_MITTELWAND: ON");
                    sendItemCmdBroadcast(SWITCH_MITTELWAND, "ON");
                    mMittelwandSwitchState = true;
                }
            }
        }
    }

    private boolean isMittelwandSwitchAuto() {
        return getPrefs().getBoolean( LightPreferenceFragment.PREF_MITTELWAND_AUTO_SWITCH, false);
    }

    private int getMittelwandOnDuration() {
        return Integer.parseInt( getPrefs().getString( LightPreferenceFragment.PREF_MITTELWAND_ON_DURATION, "10"));
    }
/*
    private boolean isNightmodeOnTimeslot() {
        if( getPrefs().getBoolean( LightPreferenceFragment.PREF_NIGHTMODE_OG_SWITCH, false)) {
            if( mOnTimeGCal == null) {
                String[] onTimeHrMin = getPrefs().getString(LightPreferenceFragment.PREF_NIGHTMODE_ON_TIME, "23:00").split(":");
                mOnTimeGCal = new GregorianCalendar();
                mOnTimeGCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt( onTimeHrMin[0]));
                mOnTimeGCal.set(Calendar.MINUTE, Integer.parseInt( onTimeHrMin[1]));
            }
            long now = new Date().getTime();
            long onTime = mOnTimeGCal.getTime().getTime();

            return isPM() && onTime > now;
        }
        return false;
    } */

    /*
    private void handleSunriseForSensor() {
        if( getPrefs().getBoolean( MiscModel.PREF_DAYLIGHT_FOR_KITCHEN_SENSOR_SWITCH, false)) {
            sendItemCmdBroadcast( KUECHE_MANUELL, "ON"); // set kitchen light to manuell
            sendItemCmdBroadcast( KUECHE_STATUS, "OFF");   // and send switch off
            Log.i(TAG, "handleSunriseForSensor [KUECHE_MANUELL:ON, KUECHE_DIMMER:0]");
        }
    } */

    private void handleSunriseForSensor() {
        if (getPrefs().getBoolean(MiscModel.PREF_DAYLIGHT_FOR_KITCHEN_SENSOR_SWITCH, false)) {
            mKuecheReqForMan = true;
            Log.i(TAG, "handleSunriseForSensor [mKuecheReqForMan: " + mKuecheReqForMan + "]");
        }
    }

    private void handleSunsetForSensor() {
        if( getPrefs().getBoolean( MiscModel.PREF_DAYLIGHT_FOR_KITCHEN_SENSOR_SWITCH, false)) {
            mKuecheReqForMan = false;
            sendItemCmdBroadcast(KUECHE_MANUELL, "OFF");  // switch to automatic (sensor) mode
            Log.i(TAG, "handleSunsetForSensor [KUECHE_MANUELL:OFF]");
        }
    }
}
