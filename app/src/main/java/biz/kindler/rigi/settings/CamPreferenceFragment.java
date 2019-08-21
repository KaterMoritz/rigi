package biz.kindler.rigi.settings;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.view.WindowManager;

import biz.kindler.rigi.R;

/**
 * Created by P.Kindler
 * patrick.kindler@schindler.com (kindlepa)
 * TMG PORT Technology
 * 2019-08-21
 */
public class CamPreferenceFragment extends BasePreferenceFragment {

    private final static String 	        TAG = CamPreferenceFragment.class.getSimpleName();

    public static final String DOOR_CAM                     = "door_cam";
    public static final String CAM_ROTATION                 = "cam_rotation";
    public static final String STORE_PIC_INTERVALL          = "storepicintervall";
    public static final String MAX_STORE_PIC_ON_BELL_RING   = "maxpiconbellrings";

    private EditTextPreference  mDoorCamPref;
    private ListPreference      mCamRotation;
    private ListPreference      mStorePicIntervall;
    private ListPreference      mMaxStorePicOnBellRing;





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_cam);
        setHasOptionsMenu(false);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getActivity().setTitle("");


        mDoorCamPref = (EditTextPreference)findPreference(DOOR_CAM);
        mDoorCamPref.setOnPreferenceChangeListener(this);
        String doorCamServerIP = mDoorCamPref.getSharedPreferences().getString( DOOR_CAM, "http://192.168.1.112/html/cam_pic.php");
        updateSummary( mDoorCamPref, doorCamServerIP);

        mCamRotation = (ListPreference)findPreference(CAM_ROTATION);
        mCamRotation.setOnPreferenceChangeListener(this);
        String rotation = mCamRotation.getSharedPreferences().getString( CAM_ROTATION, "0");
        updateSummary( mCamRotation, rotation);

        mStorePicIntervall = (ListPreference)findPreference(STORE_PIC_INTERVALL);
        mStorePicIntervall.setOnPreferenceChangeListener(this);
        String intervall = mStorePicIntervall.getSharedPreferences().getString( STORE_PIC_INTERVALL, "3");
        updateSummary( mStorePicIntervall, "alle " + intervall + " Sekunden");

        mMaxStorePicOnBellRing = (ListPreference)findPreference(MAX_STORE_PIC_ON_BELL_RING);
        mMaxStorePicOnBellRing.setOnPreferenceChangeListener(this);
        String maxPicStorePerBell = mMaxStorePicOnBellRing.getSharedPreferences().getString( MAX_STORE_PIC_ON_BELL_RING, "15");
        updateSummary( mMaxStorePicOnBellRing, "maximal " + maxPicStorePerBell);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
         if ( preference.getKey().equals( DOOR_CAM))
            updateSummary(mDoorCamPref, (String)newValue);
         else if ( preference.getKey().equals( CAM_ROTATION))
             updateSummary(mCamRotation, (String)newValue);
         else if ( preference.getKey().equals( STORE_PIC_INTERVALL))
             updateSummary(mStorePicIntervall, "alle " + (String)newValue + " Sekunden");
         else if ( preference.getKey().equals( MAX_STORE_PIC_ON_BELL_RING))
             updateSummary(mMaxStorePicOnBellRing,"maximal " + (String)newValue + " Bilder");

        return true;
    }


}
