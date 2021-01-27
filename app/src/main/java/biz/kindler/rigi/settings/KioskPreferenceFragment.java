package biz.kindler.rigi.settings;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserManager;
import android.preference.Preference;
import android.provider.Settings;
import biz.kindler.rigi.DeviceOwnerReceiver;
import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 06.06.17.
 * redesign 27.01.2021
 */

public class KioskPreferenceFragment extends BasePreferenceFragment {

    private final static String 	        TAG = KioskPreferenceFragment.class.getSimpleName();

    private static String   KIOSK_INFO              = "kioskInfo";
    private static String   KIOSK_RECOMMENDATION    = "kioskRecommendation";
    private static String   GOTO_SOUND_SETTINGS     = "soundSettings";
    private static String   START_LOCKTASK          = "startLockTask";
    private static String   STOP_LOCKTASK           = "stopLockTask";
    private static String   EXIT_APP                = "exitApp";
    private static String   RECOMMENDATION_FOR_DEVICE_OWNER_OK             = "no further action required";
    private static String   RECOMMENDATION_FOR_BECOME_DEVICE_ADMIN         = "click here to become \"device admin\"";
    private static String   RECOMMENDATION_FOR_DEVICE_ADMIN_OK             = "- become developer: tap 7 times on build number\n- activate debug mode in developer section\n- connect device via USB and start the Rigi App\n- type command in console: adb shell dpm set-device-owner biz.kindler.rigi/.DeviceOwnerReceiver\n- if dpm reported success, restart the device";
    private static String   RECOMMENDATION_FOR_FACTORY_RESET               = "Factory reset required and set up device without any account (skip account configuration)";

    private static int      RESULT_BECOME_DEVICE_ADMIN      = 6;

    private Preference      mKioskInfoPreference;
    private Preference      mKioskRecommendation;
    private Preference      mStartLockTaskPreference;
    private Preference      mStopLockTaskPreference;

    private DevicePolicyManager mDpm;
    private ComponentName mAdminComponent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_kiosk);
        setHasOptionsMenu(true);

        mDpm = (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponent = new ComponentName(getContext(), DeviceOwnerReceiver.class);

        mKioskInfoPreference = findPreference(KIOSK_INFO);

        mKioskRecommendation = findPreference(KIOSK_RECOMMENDATION);
        mKioskRecommendation.setOnPreferenceClickListener( this);

        Preference soundSettingsPreference = findPreference(GOTO_SOUND_SETTINGS);
        soundSettingsPreference.setOnPreferenceClickListener( this);
        soundSettingsPreference.setSummary("goto System Settings");

        // start locktask
        mStartLockTaskPreference = findPreference(START_LOCKTASK);
        mStartLockTaskPreference.setOnPreferenceClickListener(this);
        // stop locktask
        mStopLockTaskPreference = findPreference(STOP_LOCKTASK);
        mStopLockTaskPreference.setOnPreferenceClickListener(this);

        // exit app
        Preference exitAppPreference = findPreference(EXIT_APP);
        exitAppPreference.setOnPreferenceClickListener(this);

        collectKioskInfo();
        giveKioskRecommendation();
    }

    private void collectKioskInfo() {
        mKioskInfoPreference.setSummary("collected information...");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                StringBuffer buff = new StringBuffer();
                boolean isDeviceOwner = isDeviceOwner();
                buff.append("is app device owner: " + isDeviceOwner + "\n");
                buff.append("is device admin: " + isDeviceAdmin() + "\n");
                buff.append("users: " + getUserCnt() + ", accounts: " + getAccountsCnt() + "\n");
                buff.append("lock task permitted: " + mDpm.isLockTaskPermitted( getComponentName(getContext()).getPackageName()) + "\n");
                if( ! isDeviceOwner) {
                    buff.append("option to take \"app device owner\": " + isDeviceAdmin());
                }
                mKioskInfoPreference.setSummary(buff.toString());

                if( ! isDeviceOwner) {
                    mStartLockTaskPreference.setTitle("");
                    mStopLockTaskPreference.setTitle("");
                }
            }
        }, 1000);
    }

    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), DeviceAdminReceiver.class);
    }

    private void giveKioskRecommendation() {
        mKioskRecommendation.setSummary("Empfehlung:");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if( isDeviceOwner()) {
                    mKioskRecommendation.setSummary(RECOMMENDATION_FOR_DEVICE_OWNER_OK);
                    mStartLockTaskPreference.setEnabled(true);
                    mStopLockTaskPreference.setEnabled(true);
                } else if( ! isDeviceAdmin() && getAccountsCnt() == 0) {
                    mKioskRecommendation.setSummary(RECOMMENDATION_FOR_BECOME_DEVICE_ADMIN);
                } else if( isDeviceAdmin()) {
                    mKioskRecommendation.setSummary(RECOMMENDATION_FOR_DEVICE_ADMIN_OK);
                } else if( getAccountsCnt() > 0) {
                    mKioskRecommendation.setSummary(RECOMMENDATION_FOR_FACTORY_RESET);
                }
            }
        }, 2000);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if( preference.getKey().equals(KIOSK_RECOMMENDATION) &&! isDeviceAdmin() && getAccountsCnt() == 0)
            becomeDeviceAdmin();
        else if( preference.getKey().equals(GOTO_SOUND_SETTINGS))
            gotoSoundSettings();
        else if( preference.getKey().equals(START_LOCKTASK))
            startLockTask();
        else if( preference.getKey().equals(STOP_LOCKTASK))
            stopLockTask();
        else if( preference.getKey().equals(EXIT_APP))
            exitApp();

        return false;
    }

    private void becomeDeviceAdmin() {
        final Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponent);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "become device admin for Rigi App");
        startActivityForResult(intent, RESULT_BECOME_DEVICE_ADMIN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == RESULT_BECOME_DEVICE_ADMIN) {
            collectKioskInfo();
            giveKioskRecommendation();
        }
    }

    private int getAccountsCnt() {
        final Account[] accounts = AccountManager.get(getContext()).getAccounts();
        return accounts.length;
    }

    public boolean isDeviceOwner() {
        return (mDpm.isDeviceOwnerApp(getActivity().getApplicationContext().getPackageName()));
    }

    private boolean isDeviceAdmin() {
        return mDpm.isAdminActive(mAdminComponent); // find out if we are a device administrator
    }

    private void gotoSoundSettings() {
        getActivity().startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
    }

    private int getUserCnt() {
        UserManager userManager = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
