package biz.kindler.rigi.settings;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.SwitchPreference;
import androidx.core.app.ActivityCompat;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.system.Log;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 13.08.17.
 */

public class SoundPreferenceFragment extends BasePreferenceFragment {

    private final static String TAG = SoundPreferenceFragment.class.getSimpleName();

    public static final String BTDEVICES = "btdevices";
    public static final String PAIREDDEVICES = "paireddevices";
    public static final String MYBTSOUNDADAPTER = "mybtsoundadapter";
    public static final String BTSOUNDSWITCH = "bt_sound_switch";

    private SwitchPreference mBTSoundDeviceSwitchPref;
    private BluetoothListPreference mBTDevicesPref;
    private Preference mPairedDevicesPref;
    private Preference mMyBTSoundAdapterPref;

    private BluetoothAdapter mBluetoothAdapter;
    private MyBroadcastReceicer mBCReceiver;
    private ArrayList<BluetoothDevice> mBTDeviceList;

    int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_sound);
        setHasOptionsMenu(true);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        mBTDeviceList = new ArrayList<>();

        mBCReceiver = new MyBroadcastReceicer();

        mBTSoundDeviceSwitchPref = (SwitchPreference)findPreference(BTSOUNDSWITCH);
        mBTSoundDeviceSwitchPref.setOnPreferenceClickListener(this);
        mBTSoundDeviceSwitchPref.setSummary( getBTSoundDeviceSwitchSummary());

        mBTDevicesPref = (BluetoothListPreference) findPreference(BTDEVICES);
        mBTDevicesPref.setOnPreferenceChangeListener(this);
        mBTDevicesPref.setOnPreferenceClickListener(this);
        mBTDevicesPref.setEntries(new CharSequence[0]);
        mBTDevicesPref.setEntryValues(new CharSequence[0]);
        mBTDevicesPref.setLookupData(mBTDeviceList);
        mBTDevicesPref.setSummary("suche...");

        mMyBTSoundAdapterPref = (Preference) findPreference(MYBTSOUNDADAPTER);
        mMyBTSoundAdapterPref.setOnPreferenceClickListener(this);
        mMyBTSoundAdapterPref.setSummary( getStoredSoundAdapterName());

        mPairedDevicesPref = (Preference) findPreference(PAIREDDEVICES);
        mPairedDevicesPref.setOnPreferenceClickListener(this);
        mPairedDevicesPref.setSummary("suche...");


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                startBTDiscoveryWithDelay(1);
                updatePairedDevicesSummary();
            } else {
                mBTDevicesPref.setSummary("Bluetooth ist ausgeschaltet");
                mPairedDevicesPref.setSummary("Bluetooth ist ausgeschaltet");
            }
        } else {
            mBTDevicesPref.setSummary("no bluetooth adapter");
            mPairedDevicesPref.setSummary("no bluetooth adapter");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getContext().registerReceiver(mBCReceiver, filter);
    }

    public void onPause() {
        super.onPause();
        try {
            getContext().unregisterReceiver(mBCReceiver);
        } catch (Exception ex) {
            Log.w(TAG, ex.getMessage());
        }
    }

    private String getStoredSoundAdapterName() {
        return mMyBTSoundAdapterPref.getSharedPreferences().getString(MYBTSOUNDADAPTER, null);
    }

    private void setStoredSoundAdapterName( String name) {
        mMyBTSoundAdapterPref.getSharedPreferences().edit().putString(MYBTSOUNDADAPTER, name).apply();
    }

    private String getBTSoundDeviceSwitchSummary() {
        return mBTSoundDeviceSwitchPref.isChecked() ? "Bluetooth Adapter" : "Geräte Lautsprecher / Kopfhörer";
    }

    private void setBTSoundDeviceSwitch( boolean status) {
        mBTSoundDeviceSwitchPref.getSharedPreferences().edit().putBoolean(BTSOUNDSWITCH, status).apply();
    }

    private void startBTDiscoveryWithDelay( final int delaySec) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if( mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.startDiscovery();
                    Log.w(TAG, "mBluetoothAdapter.startDiscovery");
                }
            }
        }, delaySec * 1000);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(BTDEVICES)) {
            // updatePrefList((DynamicListPreference)preference);

            preference.setDefaultValue( null);

            if (mBluetoothAdapter != null) {
                if (!mBluetoothAdapter.isEnabled()) {
                    mBTDevicesPref.setSummary("schalte Bluetooth ein");
                    mBluetoothAdapter.enable();
                    startBTDiscoveryWithDelay(4);
                }
                else
                    startBTDiscoveryWithDelay(0);
            }
            return false;

        } else if (preference.getKey().equals(MYBTSOUNDADAPTER)) {
            //  getContext().startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));

            String myBTSoundDeviceName = getStoredSoundAdapterName();

            if( mBluetoothAdapter != null && myBTSoundDeviceName != null && myBTSoundDeviceName.length() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Kopplung mit " + myBTSoundDeviceName + " aufheben?").setPositiveButton("Ja", unpairDialogClickListener).setNegativeButton("Nein", unpairDialogClickListener).show();
                return false;
            }

        } else if (preference.getKey().equals(PAIREDDEVICES)) {
            mPairedDevicesPref.setSummary("suche...");
            updatePairedDevicesSummary();
        } else if (preference.getKey().equals(BTSOUNDSWITCH)) {
            mBTSoundDeviceSwitchPref.setSummary( getBTSoundDeviceSwitchSummary());
            setBTSoundDeviceSwitch( mBTSoundDeviceSwitchPref.isChecked());
        }

        return true;
    }

    private void unpairDevice( String deviceName) {
        if( deviceName != null) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if( device.getName().equals( deviceName)) {
                        try {
                            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
                            m.invoke(device, (Object[]) null);
                            Log.i(TAG, "Removing bond with " + deviceName + " successfully");
                        } catch (Exception e) {
                            Log.e( TAG, "Removing bond with " + deviceName + " has been failed: " + e.getMessage());
                            mPairedDevicesPref.setSummary(e.getMessage());
                        }
                    }
                }
            }
            setStoredSoundAdapterName( "");
            mMyBTSoundAdapterPref.setSummary(getStoredSoundAdapterName());
            updatePairedDevicesSummary();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        super.onPreferenceChange(preference, newValue);
        String key = preference.getKey();
        if (key.equals(BTDEVICES)) {
            String reqPairDevID = (String)newValue;
            BluetoothDevice btDev = getBTDeviceByAddress(reqPairDevID);
            if(btDev == null)
                preference.setSummary( "device not found");
            else {
                String deviceName = btDev.getName();
                boolean status = btDev.createBond();
                preference.setSummary( deviceName + " Kopplung " + (status ? "OK" : " Fehler"));
                updatePairedDevicesSummary();
                if( status) {
                    setStoredSoundAdapterName( deviceName);
                    mMyBTSoundAdapterPref.setSummary( getStoredSoundAdapterName());
                }
            }
            // updateSummary( mDeviceNamePref, newValue.toString());
            return true;
        }
        return false;
    }

    private void updatePairedDevicesSummary() {
        mPairedDevicesPref.setSummary( "suche...");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    String pairedDeviceName = getPairedDeviceAsString();
                    updateStringPreference(PAIREDDEVICES, pairedDeviceName);
                    mPairedDevicesPref.setSummary(pairedDeviceName);
                } catch( Exception ex) {
                    mPairedDevicesPref.setSummary(ex.getMessage());
                }
            }
        }, 2000);
    }

    private BluetoothDevice getBTDeviceByAddress( String address) {
        Iterator<BluetoothDevice> iter = mBTDeviceList.iterator();
        while( iter.hasNext()) {
            BluetoothDevice btDev = iter.next();
            if(btDev.getAddress().equals(address))
                return btDev;
        }
        return null;
    }


    private String getPairedDeviceAsString() throws Exception {
        StringBuffer data = new StringBuffer();
        if( mBluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            Iterator<BluetoothDevice> iter = pairedDevices.iterator();
            while( iter.hasNext()) {
                BluetoothDevice btDev = iter.next();
                data.append( btDev.getName());
            }
        }
        return data.toString();
    }

    DialogInterface.OnClickListener unpairDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    unpairDevice( getStoredSoundAdapterName());
                    updatePairedDevicesSummary();
                    break;
            }
        }
    };
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class MyBroadcastReceicer extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if( ! mBTDeviceList.contains(device)) {
                    mBTDeviceList.add(device);
                    mBTDevicesPref.setSummary( "gefunden: " + mBTDeviceList.size());
                }
            }
        }
    }


}
