package biz.kindler.rigi.settings;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 16.08.17.
 */

public class BluetoothListPreference extends ListPreference {

    private ArrayList<BluetoothDevice> mBTDeviceList;

    public BluetoothListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BluetoothListPreference(Context context) {
        super(context);
    }

    public void setLookupData(ArrayList<BluetoothDevice> btDeviceList) {
        mBTDeviceList = btDeviceList;
    }

    @Override
    protected View onCreateDialogView() {
        ListView view = new ListView(getContext());
        view.setAdapter(adapter());

        int sizeOfDevices = mBTDeviceList.size();
        CharSequence[] entries = new CharSequence[sizeOfDevices];
        CharSequence[] entryValues = new CharSequence[sizeOfDevices];

        for( int cnt=0; cnt<sizeOfDevices; cnt++) {
            BluetoothDevice dev = mBTDeviceList.get(cnt);
            entries[cnt] = dev.getName() == null ? "unknown" : dev.getName();
            entryValues[cnt] = dev.getAddress();
        }

        setEntries(entries);
        setEntryValues(entryValues);
        //setValueIndex( -1);
        //setValueIndex(initializeIndex());
        return view;
    }

    private ListAdapter adapter() {
        return new ArrayAdapter(getContext(), android.R.layout.select_dialog_singlechoice);
    }

}