package biz.kindler.rigi.modul.garden;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import biz.kindler.rigi.settings.SitzplatzPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 03.12.16.
 */

public class GardenModel extends BaseModel {

    private final static String TAG = GardenModel.class.getSimpleName();
    // Item names
    private static final String     GARDEN_PLUG     = "Steckdose_Garten_Sitzplatz";

    private Timer                   mPlugOnTimer;
    private Timer                   mPlugOffTimer;
    //private OpenHABItem             mGardenPlugItem;


    public GardenModel( Context ctx) {
        super(ctx, MainActivity.GARDEN);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SitzplatzPreferenceFragment.ACTION_SITZPLATZ_PLUG_SETTINGS_CHANGED);
        ctx.registerReceiver(this, intentFilter);

        initPlugSwitch();
    }

    protected void initItems() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        if( action.equals(SitzplatzPreferenceFragment.ACTION_SITZPLATZ_PLUG_SETTINGS_CHANGED))
            handleGardenPlugSettingsChanged();
    }

    private void initPlugSwitch() {
        boolean plugSwitch = getPrefs().getBoolean( SitzplatzPreferenceFragment.PLUG_TIMER_SWITCH, false);

        if( plugSwitch) {
            String onTime = getSwitchOnDateFromSettings();
            Date switchOnTime = getSwitchDate(onTime, 0);
            if (switchOnTime != null && switchOnTime.before(new Date()))
                switchOnTime = getSwitchDate(onTime, 1);

            if( switchOnTime != null) {
                String switchOnTimeAsString = getDateTimeFormatter().format(switchOnTime);
                mPlugOnTimer = new Timer();
                mPlugOnTimer.schedule(new SwitchOnTimerTask(), switchOnTime);
                sendSystemBroadcast(SystemModel.ACTION_LOG, getClass().getName(), GARDEN_PLUG, "scheduled ON (init): " + switchOnTimeAsString);
                Log.d(TAG, GARDEN_PLUG + " scheduled ON (init): " + switchOnTimeAsString);

                String offTime = getSwitchOffDateFromSettings();
                Date switchOffTime = getSwitchDate(offTime, 0);
                if (switchOffTime.before(new Date()))
                    switchOffTime = getSwitchDate(offTime, 1);

                String switchOffTimeAsString = getDateTimeFormatter().format(switchOffTime);
                mPlugOffTimer = new Timer();
                mPlugOffTimer.schedule(new SwitchOffTimerTask(), switchOffTime);
                sendSystemBroadcast(SystemModel.ACTION_LOG, getClass().getName(), GARDEN_PLUG, "scheduled OFF (init): " + switchOffTimeAsString);
                Log.d(TAG, GARDEN_PLUG + " scheduled OFF (init): " + switchOffTimeAsString);
            }
        }
    }

    private void handleGardenPlugSettingsChanged() {
        if (mPlugOnTimer != null)
            mPlugOnTimer.cancel();

        if (mPlugOffTimer != null)
            mPlugOffTimer.cancel();

        initPlugSwitch();
    }

    private String getSwitchOnDateFromSettings() {
        return getPrefs().getString(SitzplatzPreferenceFragment.PLUG_TIMER_ON, "17:00");
    }

    private String getSwitchOffDateFromSettings() {
        return getPrefs().getString(SitzplatzPreferenceFragment.PLUG_TIMER_OFF, "00:05");
    }

    private Date getSwitchDate(String hhmm, int addDays) {
        if( hhmm != null) {
            String[] timeHrMin = hhmm.split(":");
            if (timeHrMin.length == 2) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeHrMin[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeHrMin[1]));
                calendar.set(Calendar.SECOND, 0);
                if (addDays > 0)
                    calendar.add(Calendar.DAY_OF_YEAR, addDays);
                return calendar.getTime();
            } else
                return null;
        } else
            return null;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Timertask
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class SwitchOnTimerTask extends TimerTask {

        public void run() {
            sendItemCmdBroadcast(GARDEN_PLUG, "ON");
            sendSystemBroadcast(SystemModel.ACTION_LOG, getClass().getName(), GARDEN_PLUG, "switched ON");
            Log.i(TAG, GARDEN_PLUG + " switched ON");

            Date switchOffTime = getSwitchDate(getSwitchOffDateFromSettings() , 0);
            if (switchOffTime.before(new Date()))
                switchOffTime = getSwitchDate(getSwitchOffDateFromSettings(), 1);

            String switchOffTimeAsString = getDateTimeFormatter().format(switchOffTime);
            sendSystemBroadcast(SystemModel.ACTION_LOG, getClass().getName(), GARDEN_PLUG, "scheduled OFF: " + switchOffTimeAsString);
            Log.d(TAG, GARDEN_PLUG + " scheduled OFF: " + switchOffTimeAsString);

            mPlugOffTimer.schedule(new SwitchOffTimerTask(), switchOffTime);
        }
    }

    private class SwitchOffTimerTask extends TimerTask {

        public void run() {
            sendItemCmdBroadcast(GARDEN_PLUG, "OFF");
            sendSystemBroadcast(SystemModel.ACTION_LOG, getClass().getName(), GARDEN_PLUG, "switched OFF");
            Log.i(TAG, GARDEN_PLUG + " switched OFF");

            Date switchOnTime = getSwitchDate(getSwitchOnDateFromSettings(), 0);
            if (switchOnTime.before(new Date()))
                switchOnTime = getSwitchDate(getSwitchOnDateFromSettings(), 1);

            String switchOnTimeAsString = getDateTimeFormatter().format(switchOnTime);
            sendSystemBroadcast(SystemModel.ACTION_LOG, getClass().getName(), GARDEN_PLUG, "scheduled ON: " + switchOnTimeAsString);
            Log.d(TAG, GARDEN_PLUG + " scheduled ON: " + switchOnTimeAsString);
            mPlugOnTimer.schedule(new SwitchOnTimerTask(), switchOnTime);
        }
    }
}
