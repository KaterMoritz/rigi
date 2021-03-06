package biz.kindler.rigi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import biz.kindler.rigi.modul.background.BackgroundModel;
import biz.kindler.rigi.settings.GeneralPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 15.11.16.
 */

public class Util {

    public static boolean isNowLaterAs( String hh_mm) {
        try {
            String[] hhmm = hh_mm.split( ":");
            GregorianCalendar gc = new GregorianCalendar();
            gc.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hhmm[0]));
            gc.set(Calendar.MINUTE, Integer.parseInt(hhmm[1]));
            return new Date().after(gc.getTime());
        } catch( Exception ex) {
            System.out.println( "Util.isLaterAs [" + hh_mm + "] exc: " + ex.getMessage());
            return false;
        }
    }

    public static String getHost( Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(GeneralPreferenceFragment.OPENHAB_SERVER, "");
    }

    public static String inputStreamToString( InputStream inputStream) throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        for (String line; (line = r.readLine()) != null; )
            total.append(line).append('\n');

        return total.toString();
    }

    public static InputStream stringToInputStream( String string) throws Exception {
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }

    public static int getDayOfYearToday() {
        return new GregorianCalendar().get(Calendar.DAY_OF_YEAR);
    }

    public static String getBackgroundWebcamUrl( Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getString(BackgroundModel.BACKGROUND_CAMURL, "https://rigipic.ch/rigikapellekulm.jpg");
    }

    public static void showToastInUiThread( final Context ctx, final int stringRes, final int showTime) {

        Handler mainThread = new Handler( Looper.getMainLooper());
        mainThread.post( new Runnable() {
            @Override
            public void run() {
                Toast.makeText( ctx, ctx.getString(stringRes), showTime).show();
            }
        });
    }

    public static void showToastInUiThread( final Context ctx, final String stringTxt, final int showTime) {

        Handler mainThread = new Handler( Looper.getMainLooper());
        mainThread.post( new Runnable() {
            @Override
            public void run() {
                if( ctx != null)
                    Toast.makeText( ctx, stringTxt, showTime).show();
            }
        });
    }
}
