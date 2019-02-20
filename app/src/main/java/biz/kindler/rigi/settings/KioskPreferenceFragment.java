package biz.kindler.rigi.settings;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.preference.Preference;

import biz.kindler.rigi.DeviceOwnerReceiver;
import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 06.06.17.
 */

public class KioskPreferenceFragment extends BasePreferenceFragment {

    private final static String 	        TAG = KioskPreferenceFragment.class.getSimpleName();

    private static String   SET_DEVICE_OWNER        = "cmdDeviceOwner";
    private static String   CLEAR_DEVICE_OWNER      = "clearDeviceOwner";
    private static String   ACTIVE_USERS            = "activeUsers";
    private static String   START_LOCKTASK          = "startLockTask";
    private static String   STOP_LOCKTASK           = "stopLockTask";
    private static String   EXIT_APP                = "exitApp";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_kiosk);
        setHasOptionsMenu(true);

        // set Device owner
        Preference cmdDeviceOwnerPreference = findPreference(SET_DEVICE_OWNER);
        cmdDeviceOwnerPreference.setOnPreferenceClickListener( this);
        // clear  Device owner
        Preference clearDeviceOwnerPreference = findPreference(CLEAR_DEVICE_OWNER);
        clearDeviceOwnerPreference.setOnPreferenceClickListener( this);
        // Aktive Users
        Preference activeUsersPreference = findPreference(ACTIVE_USERS);
        activeUsersPreference.setOnPreferenceClickListener( this);
        activeUsersPreference.setSummary("klick to check");
        // start locktask
        Preference startLockTaskPreference = findPreference(START_LOCKTASK);
        startLockTaskPreference.setOnPreferenceClickListener(this);
        // stop locktask
        Preference stopLockTaskPreference = findPreference(STOP_LOCKTASK);
        stopLockTaskPreference.setOnPreferenceClickListener(this);
        // exit app
        Preference exitAppPreference = findPreference(EXIT_APP);
        exitAppPreference.setOnPreferenceClickListener(this);

        boolean lockTaskPermitted = ((DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE)).isLockTaskPermitted("biz.kindler.rigi");
        startLockTaskPreference.setSummary("LockTaskPermitted: " + lockTaskPermitted);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if( preference.getKey().equals(SET_DEVICE_OWNER))
            setDeviceOwner();
        else if( preference.getKey().equals(CLEAR_DEVICE_OWNER))
            clearDeviceOwner();
        else if( preference.getKey().equals(ACTIVE_USERS))
            checkActiveUsers();
        else if( preference.getKey().equals(START_LOCKTASK))
            startLockTask();
        else if( preference.getKey().equals(STOP_LOCKTASK))
            stopLockTask();
        else if( preference.getKey().equals(EXIT_APP))
            exitApp();

        return false;
    }

    private void setDeviceOwner() {
        try {
            //Runtime.getRuntime().exec("dpm set-device-owner biz.kindler.rigi/.DeviceOwnerReceiver");
            DevicePolicyManager dpManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName deviceAdmin = new ComponentName(getActivity(), DeviceOwnerReceiver.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dpManager.setLockTaskPackages(deviceAdmin, new String[]{"biz.kindler.rigi"});
            }

        } catch (Exception e) {
            Preference pref = findPreference(SET_DEVICE_OWNER);
            pref.setSummary(e.getMessage());
        }
        // return true;
    }

    private void clearDeviceOwner() {
        try {
            DevicePolicyManager dpManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dpManager.clearDeviceOwnerApp("biz.kindler.rigi");
            }
        } catch (Exception e) {
            Preference pref = findPreference(CLEAR_DEVICE_OWNER);
            pref.setSummary(e.getMessage());
        }
        //return true;
    }

    public boolean checkIfDeviceOwner() {
        DevicePolicyManager dpManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        return (dpManager.isDeviceOwnerApp(getActivity().getApplicationContext().getPackageName()));

        // if (dpManager.isDeviceOwnerApp(getActivity().getApplicationContext().getPackageName())) {
        // This app is set up as the device owner. Show the main features.
        //     cmdDeviceOwnerPreference.setSummary("OK - Rigi app is the device owner");
        //} else {
        // This app is not set up as the device owner. Show instructions.
        //     cmdDeviceOwnerPreference.setSummary("Rigi app is NOT the device owner (click to set-device-owner) \n [aktive users: " + userCnt + "]");
        // showFragment(InstructionFragment.newInstance());
        // }
    }

    public void showPreferenceIfDeviceOwner(boolean isDeviceOwner) {
        Preference cmdDeviceOwnerPreference = findPreference("cmdDeviceOwner");
        if (isDeviceOwner)
            cmdDeviceOwnerPreference.setSummary("OK - Rigi app is the device owner");
        else
            cmdDeviceOwnerPreference.setSummary("Rigi app is NOT the device owner (click to set-device-owner)");
    }

    private void checkActiveUsers() {
        Preference pref = findPreference(ACTIVE_USERS);
        pref.setSummary(String.valueOf(getUserCnt()));
    }

    private int getUserCnt() {
        UserManager userManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        //int userCnt = userManager.getUserCount();
        int userCnt = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            userCnt = userManager.getUserProfiles().size();
        }
        return userCnt;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void startLockTask() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().startLockTask();
        }
    }

    private void stopLockTask() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getActivity().stopLockTask();
    }

    private void exitApp() {
        getActivity().finishAffinity();
        System.exit(0);
    }
}
