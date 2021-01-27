package biz.kindler.rigi;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import biz.kindler.rigi.settings.KioskPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 01.12.16.
 */

public class DeviceOwnerReceiver extends DeviceAdminReceiver {
    private final static String 	        TAG = DeviceOwnerReceiver.class.getSimpleName();
    /**
     * Called on the new profile when device owner provisioning has completed. Device owner
     * provisioning is the process of setting up the device so that its main profile is managed by
     * the mobile device management (MDM) application set up as the device owner.
     */
    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        // Enable the profile
        DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = getComponentName(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            manager.setProfileName(componentName, "biz.kindler.rigi");
            manager.setLockTaskPackages(componentName, new String[]{"biz.kindler.rigi"});
        }

        // Open the main screen
        Intent launch = new Intent(context, MainActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch);
    }

    /**
     * @return A newly instantiated {@link android.content.ComponentName} for this
     * DeviceAdminReceiver.
     */
    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), DeviceOwnerReceiver.class);
    }

    @Override
    public void onLockTaskModeEntering(Context context, Intent intent, String pkg) {
        super.onLockTaskModeEntering(context, intent, pkg);
      //  Toast.makeText(context, "onLockTaskModeEntering", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onLockTaskModeEntering");
    }

    @Override
    public void onLockTaskModeExiting(Context context, Intent intent) {
        super.onLockTaskModeExiting(context, intent);
      //  Toast.makeText(context, "onLockTaskModeExiting", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onLockTaskModeExiting");
    }

}
