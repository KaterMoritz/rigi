package biz.kindler.rigi.modul.system;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.GregorianCalendar;
import java.util.Set;

import biz.kindler.rigi.R;
import biz.kindler.rigi.settings.LogPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 06.06.17.
 */

// https://stackoverflow.com/questions/8446504/android-writing-log-to-the-file

public class _StoreLogTask extends AsyncTask<Void, String, String> {

    private final static String TAG  = _StoreLogTask.class.getSimpleName();

    protected String    filePathAndName;
    private Context     mCtx;
    private boolean     mcClearLogAfter;

    public _StoreLogTask(Context ctx, boolean clearLogAfter) {
        mCtx = ctx;
        mcClearLogAfter = clearLogAfter;
    }

    @Override
    protected String doInBackground(Void... params) {
        int dayOfYear = new GregorianCalendar().get(java.util.Calendar.DAY_OF_YEAR);
        String fileName = "rigi_"+ dayOfYear + ".log";
        String path = mCtx.getExternalFilesDir(null).getPath();

        filePathAndName = path + fileName;

        try {
            publishProgress( "schreibe logfile...");
            String statusWriteLog = writeLogFile( filePathAndName);
            publishProgress( statusWriteLog);
            if( mcClearLogAfter) {
                Runtime.getRuntime().exec("logcat -c");  // clear logcat
                Log.d(TAG, "logcat cleared");
                publishProgress( "logcat cleared");
            }
            return statusWriteLog;
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return e.getMessage();
        }
    }

    // --> Important: <-- Go to: Device - Settings - Developer - Buffer size for logging -> todo: increase to maximum (16MB)
    private String writeLogFile( String filePathAndName) throws Exception {
        File currLogFile = new File(filePathAndName);
        if( currLogFile.exists()) {
            boolean status = currLogFile.delete();
            if( ! status)
                throw new Exception( "delete file: " + filePathAndName + " FAILED");
        }

        String prog = "logcat";
        //  String filter = "MainActivity:D ItemManager:D *:S"; // *:S always required, means SILENT all others
       /* String filter = "RigiApplication:D MainActivity:D MainListAdapter:D ItemManager:D SettingsActivity:D GeneralPreferenceFragment:D SitzplatzPreferenceFragment:D " +
                "ShutterPreferenceFragment:D LightPreferenceFragment:D KioskPreferenceFragment:D LogPreferenceFragment:D LetterboxModel:D VBLModel:D TimeAndDateModel:D " +
                "BackgroundModel:D *:S"; */

        String filter = getFilter(mCtx) + "*:S"; // *:S always required, means SILENT all others

        String param = "-df " + filePathAndName;
        String fullCmd = prog + " " + filter + " " + param;
        Log.d(TAG, "fullCmd: " + fullCmd);
        Process process = Runtime.getRuntime().exec(fullCmd);
        int exitVal = process.waitFor();
        long fileSize = new File(filePathAndName).length();
        return filePathAndName + (exitVal == 0 ? " OK" : " FAILED:" + String.valueOf(exitVal)) + " [" + readableFileSize(fileSize) + "]";
    }

    private static String getFilter( Context ctx) {
        Set<String> selectedSet = null;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        selectedSet = preferences.getStringSet(LogPreferenceFragment.MODULLIST_DEBUG, null);
        String[] allModules = ctx.getResources().getStringArray(R.array.logmodules);
        String globalLogLevel = preferences.getString( LogPreferenceFragment.LOGLEVEL, "E");

        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < allModules.length; i++) {
            boolean selected = selectedSet.contains(allModules[i]);
            buff.append(allModules[i] + (selected ? ":D " : ":" + globalLogLevel + " "));
        }
        return buff.toString();
    }

    public static void startLogging() {

    }


    protected void onProgressUpdate(String... progress) {
        sendSystemBroadcast( mCtx, SystemModel.ACTION_LOG, getClass().getName(), "WRITE LOG progress", progress[0]);
    }

    protected void onPostExecute(String result) {
        sendSystemBroadcast( mCtx, SystemModel.ACTION_LOG, getClass().getName(), "WRITE LOG result", result);
    }

    // thanks to: https://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
    public String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // send global broadcast
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected void sendSystemBroadcast( Context ctx, String action, String className, String objectName, String message) {
        Intent bc = new Intent();
        bc.setAction( action);
        bc.putExtra( SystemModel.KEY_CLASS, className);
        bc.putExtra( SystemModel.KEY_OBJECT, objectName);
        bc.putExtra( SystemModel.KEY_MESSAGE, message);
        ctx.sendBroadcast(bc);
    }
}
