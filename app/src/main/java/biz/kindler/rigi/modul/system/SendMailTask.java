package biz.kindler.rigi.modul.system;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.creativityapps.gmailbackgroundlibrary.BackgroundMail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import biz.kindler.rigi.settings.LogPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 06.06.17.
 */

public class SendMailTask extends AsyncTask<String, String, String> {

    private final static String TAG = SendMailTask.class.getSimpleName();
    private static long MAX_FILESIZE    = 5000000; // bytes (5 MB)
    private Context mCtx;

    public SendMailTask(Context ctx) {
        mCtx = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        String fileName = params[0];
        String path = Log.getLogPath();

        try {
            publishProgress("sende email...");
            return sendEmail(path, fileName);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return e.getMessage();
        }
    }

    private String sendEmail(String filePath, final String fileName) throws Exception {
        File logFile = new File(filePath + fileName);
        long fileSize = logFile.length();
        Log.i(TAG, "logfile size: " + fileSize/1000 + " KB [file:" + filePath + fileName + "]");
        final String logData;

        if( fileSize > MAX_FILESIZE)
            logData = "no attachment send, file too large (" + fileSize/1000 + " KB) max is " + MAX_FILESIZE / 1000000 + " MB";
        else
            logData = readFromFile(logFile);

        //Let this be the code in your n'th level thread from main UI thread
        Handler h = new Handler(Looper.getMainLooper());
        h.post(new Runnable() {
            public void run() {
                BackgroundMail.newBuilder(mCtx)
                        .withUsername("patrick.kindler.ch@gmail.com")
                        .withPassword("pt69plpt69pl")
                        .withMailto("patrick.kindler.ch@gmail.com")
                        .withType(BackgroundMail.TYPE_PLAIN)
                        .withSubject("Rigi APP [" + fileName + "," + getDeviceName() + "]")
                        .withBody(logData)
                        .withOnSuccessCallback(new BackgroundMail.OnSuccessCallback() {
                            @Override
                            public void onSuccess() {
                                Log.i(TAG, "send mail sucessfully [" + fileName + "]");
                            }
                        })
                        .withOnFailCallback(new BackgroundMail.OnFailCallback() {
                            @Override
                            public void onFail() {
                                Log.e(TAG, "send mail failed [" + fileName + "]");
                            }
                        })
                        .send();
            }
        });

        return fileName + " sended to patrick.kindler.ch@gmail.com";
    }

    protected void onProgressUpdate(String... progress) {
        sendSystemBroadcast(mCtx, SystemModel.ACTION_LOG, getClass().getName(), "in progress", progress[0]);
    }

    protected void onPostExecute(String result) {
        sendSystemBroadcast(mCtx, SystemModel.ACTION_LOG, getClass().getName(), "result", result);
    }

    private String readFromFile(File file) {
        String ret = "";

        try {
            FileInputStream inputStream = new FileInputStream(file.getAbsolutePath());

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null)
                    stringBuilder.append(receiveString + "\n");

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("read log from file", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("ead log from file", "Can not read file: " + e.toString());
        }

        return ret;
    }

    private String getDeviceName() {
        return PreferenceManager.getDefaultSharedPreferences(mCtx).getString(LogPreferenceFragment.DEVICENAME, "unknown");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // send global broadcast
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected void sendSystemBroadcast(Context ctx, String action, String className, String objectName, String message) {
        Intent bc = new Intent();
        bc.setAction(action);
        bc.putExtra(SystemModel.KEY_CLASS, className);
        bc.putExtra(SystemModel.KEY_OBJECT, objectName);
        bc.putExtra(SystemModel.KEY_MESSAGE, message);
        ctx.sendBroadcast(bc);
    }
}
