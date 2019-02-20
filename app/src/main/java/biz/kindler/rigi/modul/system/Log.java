package biz.kindler.rigi.modul.system;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 09.06.17.
 * see: https://stackoverflow.com/questions/8446504/android-writing-log-to-the-file
 */

public class Log {


    private static final String LINESEP = System.getProperty("line.separator");
    private static boolean mLogcatAppender = true;
    private static File mLogFile;
    private static String mLogconfig;
    private static String mRootLoglevel;

    static {
        setDayOfYear( new GregorianCalendar().get(java.util.Calendar.DAY_OF_YEAR));
        setLogcatAppender( true);
    }

    public static void setDayOfYear( int dayOfYear) {
        mLogFile = new File( getLogPathAsFile(), "/rigilog/" + getFileName( dayOfYear));
        if ( ! mLogFile.exists()) {
            try {
                mLogFile.getParentFile().mkdirs();
                mLogFile.createNewFile();
                logDeviceInfo();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String deleteOldLogfiles( int olderAsXDay) throws Exception {
        File logDir = new File( getLogPathAsFile(), "/rigilog/");
        File[] allFiles = logDir.listFiles();
        StringBuffer statusResult = new StringBuffer();
        int dayOfYearToday = new GregorianCalendar().get(java.util.Calendar.DAY_OF_YEAR);
        if( allFiles.length > olderAsXDay) {
            for (int i = 0; i < allFiles.length; i++) {
                String filename = allFiles[i].getName();
                int dayOfYearOfFile = extractDayOfYearFromFilename(filename);

                if( dayOfYearOfFile < (dayOfYearToday - olderAsXDay)) {
                    Log.d("Files", "FileName: " + filename + " DELETING");  // logcat
                    boolean delStatus = allFiles[i].delete();
                    statusResult.append( filename + (delStatus ? " DELETED" : " FAILED"));
                } else
                    Log.d("Files", "FileName: " + filename + " NOT deleting");  // logcat
            }
        } else
            statusResult.append("no old log files found for delete");

        return statusResult.toString();
    }

    private static int extractDayOfYearFromFilename( String filename) {
        String dayAsString = filename.substring( 5, filename.length() -4);
        return Integer.parseInt( dayAsString); //"rigi_" + dayOfYear + ".log";
    }

    public static String getFileName( int dayOfYear) {
        return "rigi_" + dayOfYear + ".log";
    }

    private static File getLogPathAsFile() {
        return Environment.getExternalStorageDirectory();
    }

    public static String getLogPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/rigilog/";
    }

    public static void setLogcatAppender(boolean status) {
        mLogcatAppender = status;
    }

    // config is: TAG as String wich in Level DEBUG
    public static void setLogConfig( String config) {
        mLogconfig = config;
    }

    public static void setRootLoglevel( String rootLoglevel) {
        mRootLoglevel = rootLoglevel;
    }

    public static void d(String TAG, String message) {
        if( mRootLoglevel.equals("D") || mLogconfig.contains( TAG)) {
            appendLog(TAG + " : " + message);
            if (mLogcatAppender)
                android.util.Log.d(TAG, message);
        }
    }

    public static void i(String TAG, String message) {
        if( mRootLoglevel.equals("D") || mRootLoglevel.equals("I")) {
            appendLog(TAG + " : " + message);
            if (mLogcatAppender)
                android.util.Log.i(TAG, message);
        }
    }

    public static void w(String TAG, String message) {
        if( mRootLoglevel.equals("D") || mRootLoglevel.equals("I") || mRootLoglevel.equals("W")) {
            appendLog(TAG + " : " + message);
            if (mLogcatAppender)
                android.util.Log.w(TAG, message);
        }
    }

    public static void e(String TAG, String message) {
        if( mRootLoglevel.equals("D") || mRootLoglevel.equals("I") || mRootLoglevel.equals("W") || mRootLoglevel.equals("E")) {
            appendLog(TAG + " : " + message);
            if (mLogcatAppender)
                android.util.Log.e(TAG, message);
        }
    }


    private static synchronized void appendLog(String text) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        try {
            final FileWriter fileOut = new FileWriter(mLogFile, true);
            fileOut.append(sdf.format(new Date()) + " : " + text + LINESEP);
            fileOut.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private static void logDeviceInfo() {
        appendLog("Model : " + android.os.Build.MODEL);
        appendLog("Brand : " + android.os.Build.BRAND);
        appendLog("Product : " + android.os.Build.PRODUCT);
        appendLog("Device : " + android.os.Build.DEVICE);
        appendLog("Codename : " + android.os.Build.VERSION.CODENAME);
        appendLog("Release : " + android.os.Build.VERSION.RELEASE);
        appendLog("LogFile : " + mLogFile.getAbsolutePath());
    }
}