package biz.kindler.rigi.modul.weatherstation;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.TwoLinesDataHolder;
import biz.kindler.rigi.modul.clock.TimeAndDateModel;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import biz.kindler.rigi.settings.GeneralPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 09.12.16.
 */

public class WeatherstationModel extends BaseModel {

    private final static String     TAG = WeatherstationModel.class.getSimpleName();

    // Item names
    private static final String     RAINING             = "WS_Regen";     // trocken oder regen
    private static final String     TEMPERATURE         = "WS_Aussentemp";
    private static final String     WIND                = "WS_Wind";
    private static final String     BRIGHTNESS          = "WS_Helligkeit";

    public static final String      UNKNOWN             = "unknown";
    public static final String      NIGHT               = "night";
    public static final String      DAY                 = "day";

    public static  String           ACTION_LUX_LEVEL    = "action-lux-level";
    public static  String           SUNRISE_BY_LUX      = "key-sunrise-by-lux";
    public static  String           SUNSET_BY_LUX       = "key-sunset-by-lux";
    public static  String           NOW_DAY_BY_LUX      = "now-day-by-lux";  // its a property for read ( between sunrise by lux and sunset by lux
    public static  String           NOW_NIGHT_BY_LUX    = "now-night-by-lux";

    private static final int        STATE_UNKNOWN       = 0;
    private static final int        STATE_NIGHT         = 1;
    private static final int        STATE_DAY           = 2;
    private static final int        DRY                 = 1;
    private static final int        RAIN                = 2;

    private TwoLinesDataHolder      mDataHolder;
    private int                     mCondition;
    private int                     mDayOrNight;
    private String                  mTemperature;
    private String                  mWind;
    private String                  mBrightness;
    private String                  mSunrise;
    private String                  mSunset;
    private boolean                 mSunriseNotiSended;
    private boolean                 mSunsetNotiSended;

    public WeatherstationModel( Context ctx) {
        super(ctx, MainActivity.WEATHERSTATION);

        mDataHolder = new WeatherstationDataHolder();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TimeAndDateModel.ACTION_NEW_DAY);
        intentFilter.addAction(RAINING);
        intentFilter.addAction(TEMPERATURE);
        intentFilter.addAction(WIND);
        intentFilter.addAction(BRIGHTNESS);

        ctx.registerReceiver(this, intentFilter);
    }

    @Override
    protected void initItems() throws Exception {
        sendItemReadBroadcast( RAINING);
        sendItemReadBroadcast( TEMPERATURE);
        sendItemReadBroadcast( WIND);
        sendItemReadBroadcast( BRIGHTNESS);
        sendUpdateListItemBroadcast( true);
    }

    public TwoLinesDataHolder getDataHolder() {
        return mDataHolder;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive( context, intent);

        boolean hasDataUpdate = false;
        String action = intent.getAction();
        String value = intent.getStringExtra(ItemManager2.VALUE);

        Log.d(TAG, "action: " + action + ", value: " + (value == null ? "null" : value));

        if (action.equals(RAINING))
            hasDataUpdate = handleRainAction( value);
        else if (action.equals(TEMPERATURE))
            hasDataUpdate = handleTemperaturAction( value);
        else if (action.equals(WIND))
            hasDataUpdate = handleWindAction( value);
        else if (action.equals(BRIGHTNESS))
            hasDataUpdate = handleBrightnessAction( value);

        if( hasDataUpdate) {
            mDataHolder.setLine2(getTextForCondition());
            mDataHolder.setLine2Center(getTextForTemperature() + "               " + getTextForWind() + "               " + getTextForBrightness());
            mDataHolder.setLine2Right(getTextForSunriseAndSunset());
            mDataHolder.setIcon1Visible( true);
            mDataHolder.setIcon2Visible( true);
            sendUpdateListItemBroadcast();
        }

        else if( action.equals(TimeAndDateModel.ACTION_NEW_DAY)) {
            mSunrise = intent.getStringExtra( TimeAndDateModel.KEY_SUNRISE_TIME);
            mSunset = intent.getStringExtra( TimeAndDateModel.KEY_SUNSET_TIME);
            mSunriseNotiSended = false;
            mSunsetNotiSended = false;
        }
    }

    private boolean handleRainAction( String itemState) {
        if( itemState == null) {
            mCondition = STATE_UNKNOWN;
            return true;
        }
        else if(itemState.equals( "ON") && mCondition != RAIN) {
            mCondition = RAIN;
            mDataHolder.setImgResId(R.drawable.raining);
            return true;
        }
        else if(itemState.equals( "OFF") && mCondition != DRY) {
            mCondition = DRY;
            mDataHolder.setImgResId(R.drawable.weatherstation);
            return true;
        }
        else if( ! itemState.equals( "OFF") && ! itemState.equals( "ON") && mCondition != STATE_UNKNOWN) {
            mCondition = STATE_UNKNOWN;
            return true;
        } else
            return false;
    }

    private boolean handleTemperaturAction( String itemState) {
        if( itemState == null) {
            mTemperature = null;
            return true;
        }
        else if( itemState.equals(mTemperature))
            return false;
        else {
            mTemperature = itemState;
            return true;
        }
    }

    private boolean handleWindAction( String itemState) {
        if( itemState == null) {
            mWind = null;
            return true;
        }
        else if( itemState.equals(mWind))
            return false;
        else {
            mWind = itemState;
            return true;
        }
    }

    private boolean handleBrightnessAction( String itemState) {
        if( itemState == null) {
            mBrightness = null;
            return true;
        }
        else if( itemState.equals(mBrightness))
            return false;
        else {
            mBrightness = itemState;
            checkDayOrNight( mBrightness);

            checkLuxLevelForNotification( mBrightness);
            return true;
        }
    }

    private void checkLuxLevelForNotification( String brightness) {
        try {
            float luxVal = Float.parseFloat(brightness);

            if( ! mSunriseNotiSended && isAM() && luxVal > Integer.parseInt(getPrefs().getString(GeneralPreferenceFragment.PREFS_DAWN_LEVEL, "100"))) {
                sendLuxLevelBroadcast(SUNRISE_BY_LUX, brightness);
                Log.i(TAG, "ACTION_LUX_LEVEL sended SUNRISE_BY_LUX (" + brightness + " lux)");
                mSunriseNotiSended = true;
                storePrefsBoolean(NOW_DAY_BY_LUX, true);
                storePrefsBoolean(NOW_NIGHT_BY_LUX, false);
            }
            else if( ! mSunsetNotiSended && isPM() && luxVal < Integer.parseInt(getPrefs().getString(GeneralPreferenceFragment.PREFS_TWILIGHT_LEVEL, "100"))) {
                sendLuxLevelBroadcast( SUNSET_BY_LUX, brightness);
                Log.i(TAG, "ACTION_LUX_LEVEL sended SUNSET_BY_LUX (" + brightness + " lux)");
                mSunsetNotiSended = true;
                storePrefsBoolean(NOW_DAY_BY_LUX, false);
                storePrefsBoolean(NOW_NIGHT_BY_LUX, true);
            }
        } catch( Exception ex) {
            Log.w(TAG, "parseFloat (" + brightness + ") " + ex.getMessage());
        }
    }

    private void sendLuxLevelBroadcast( String keyExtra, String brightness) {
        Intent bc = new Intent();
        bc.setAction( ACTION_LUX_LEVEL);
        bc.putExtra( keyExtra, true);
        bc.putExtra( "brightness", brightness);
        getContext().sendBroadcast(bc);
    }

    private void checkDayOrNight( String lux) {
        if( lux != null) {
            try {
                float luxVal = Float.parseFloat(lux);
                if( luxVal > 500 && mDayOrNight != STATE_DAY) {
                    mDayOrNight = STATE_DAY;
                    sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "day or night", getDayOrNightAsString());
                    sendSystemBroadcast( SystemModel.ACTION_DAY_OR_NIGHT, getClass().getName(), "day or night", getDayOrNightAsString());
                } else if( luxVal <= 500 && mDayOrNight != STATE_NIGHT) {
                    mDayOrNight = STATE_NIGHT;
                    sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "day or night", getDayOrNightAsString());
                    sendSystemBroadcast( SystemModel.ACTION_DAY_OR_NIGHT, getClass().getName(), "day or night", getDayOrNightAsString());
                }
            } catch( Exception ex) {
                sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "parse lux", ex.getMessage());
            }
        }
    }

    private String getTextForCondition() {
        if( mCondition == STATE_UNKNOWN)
            return "";
        else
            return mCondition == RAIN ? "es regnet" : "trocken";
    }

    private String getTextForTemperature() {
        if( mTemperature == null || mTemperature.length() == 0)
            return "";
        else {
            try {
                Float floatVal = Float.parseFloat(mTemperature);
                return String.format("%.01f", floatVal) + "°C";
            } catch( Exception ex) {
                sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "temperatur", ex.getMessage());
                return "";
            }
        }
    }

    private String getTextForWind() {
        if( mWind == null)
            return "";
        else {
            try {
                Float floatVal = Float.parseFloat(mWind);
                if( floatVal == 0)
                    return "windstill";
                else
                    return "Wind " + String.format("%.01f", floatVal) + " m/s";
            } catch( Exception ex) {
                sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "wind", ex.getMessage());
                return "";
            }
        }
    }

    private String getTextForBrightness() {
        if( mBrightness == null)
            return "";
        else {
            try {
                Float floatVal = Float.parseFloat(mBrightness);
                if (floatVal > 1000) {
                    Float kVal = floatVal / 1000;
                    return kVal.intValue() + " KLux";
                } else if (floatVal < 1000 && floatVal >= 1) {
                    return floatVal.intValue() + " lux";  // without ,
                } else
                    return mBrightness + " lux";
            } catch( Exception ex) {
                sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "brightness", ex.getMessage());
                return "";
            }
        }
    }

    private String getTextForSunriseAndSunset() {
        return (mSunrise == null ? "" : mSunrise) + "       " + (mSunset == null ? "" : mSunset);
    }

    private String getDayOrNightAsString() {
        return mDayOrNight == STATE_NIGHT ? NIGHT : (mDayOrNight == STATE_DAY ? DAY : UNKNOWN);
    }

    public static long roundUp(long num, long divisor) {
        return (num + divisor - 1) / divisor;
    }

}
