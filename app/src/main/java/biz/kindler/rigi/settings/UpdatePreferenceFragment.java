package biz.kindler.rigi.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import biz.kindler.rigi.R;

/**
 * Created by P.Kindler
 * patrick.kindler@schindler.com (kindlepa)
 * TMG PORT Technology
 * 2019-08-13
 */
public class UpdatePreferenceFragment extends BasePreferenceFragment {

    private final static String TAG = UpdatePreferenceFragment.class.getSimpleName();

    private static final String LOCAL_VERSION                   = "local-version";
    private static final String SERVER_VERSION                  = "server-version";
    private static final String SERVER_VERSION_URL              = "http://www.kindler.biz/rigi/rigi.txt";
    private static final String SERVER_DOWNLOAD_URL             = "http://www.kindler.biz/rigi/rigi.apk";

    private Preference          mServerVersionPref;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_info);
        setHasOptionsMenu(true);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        Preference localVersionPref = findPreference( LOCAL_VERSION);
        localVersionPref.setSummary(getLocalVersion());

        mServerVersionPref = findPreference( SERVER_VERSION);
        mServerVersionPref.setOnPreferenceClickListener(this);
        mServerVersionPref.setSummary("checking...");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getServerVersion();
            }
        }, 1000);
    }


    private String getLocalVersion() {
        try {
            PackageInfo pinfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            return pinfo.versionName;
        } catch (Exception e) {
            return "-";
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if( preference.getKey().equals( SERVER_VERSION)) {
            gotoDownloadLink();
            return true;
        } else
            return false;
    }

    private void gotoDownloadLink() {
        Uri webpage = Uri.parse(SERVER_DOWNLOAD_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            getActivity().finish();
            getActivity().stopLockTask();
            startActivity(intent);
            getActivity().finishAffinity();
        }
    }

    private void getServerVersion() {
        new Thread(new Runnable(){
            public void run(){
                try {
                    URL url = new URL(SERVER_VERSION_URL); // the version info file
                    HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(60000); // timing out in a minute
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String str;
                    StringBuffer buff= new StringBuffer();
                    while ((str = in.readLine()) != null) {
                        buff.append(str);
                    }
                    String serverVersion = buff.toString();
                    String localVersion = getLocalVersion();
                    boolean updateAvailable = ! serverVersion.equals(localVersion);
                    showServerInfoInUIThread( updateAvailable ? serverVersion + " (click für update)" : serverVersion, updateAvailable);

                    in.close();
                } catch (FileNotFoundException e) {
                    showServerInfoInUIThread("not found", false);
                } catch (Exception e) {
                    showServerInfoInUIThread(e.getMessage(), false);
                }
            }
        }).start();
    }

    private void showServerInfoInUIThread(final String txt, final boolean enableForClick) {
        new Handler( Looper.getMainLooper()).post( new Runnable() {
            @Override
            public void run() {
                mServerVersionPref.setSummary(txt);
                if( ! enableForClick) {
                    mServerVersionPref.setOnPreferenceClickListener(null);
                }
            }
        });
    }

}
