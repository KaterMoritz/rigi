package biz.kindler.rigi.modul.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.net.URL;

import biz.kindler.rigi.modul.clock.TimeAndDateModel;
import biz.kindler.rigi.modul.entree.ScreensaverActivity;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.settings.GeneralPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 27.11.16.
 */

public class BackgroundModel extends BroadcastReceiver {

    private final static String TAG = BackgroundModel.class.getSimpleName();

    public static final String  BACKGROUND_MODE         = "pref_background";
    public static final String  STANDARD                = "standard";
    public static final String  SET1                    = "set1";
    public static final String  SET2                    = "set2";
    public static final String  DEVICE                  = "device";
    public static final String  WEBCAM                  = "webcam";
    public static final String  WEBRANDOM               = "webrandom";
    private static final String LOREMFLICKR_KEYWORD     = "loremflickr_keyword";  // for WEBRANDOM
    private static final String WEBCAM_URL              = "https://rigipic.ch/rigikapellekulm.jpg";
    private static final String LOREMFLICKR_URL         = "http://loremflickr.com/800/1280/";   // add search keyword param
    private static final int    LEFT_PART_OF_IMAGE      = 0;
    private static final int    CENTER_PART_OF_IMAGE    = 1;
    private static final int    RIGHT_PART_OF_IMAGE     = 2;

    private BackgroundView      mView;
    private Context             mCtx;
    private int                 mTickCnt;
    private int                 mWebcamPartOfImage;

    public BackgroundModel( Context ctx) {
        mCtx = ctx;
        mView = new BackgroundView( ctx);
        mView.setModel(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TimeAndDateModel.ACTION_NEW_DAY);
        intentFilter.addAction(GeneralPreferenceFragment.ACTION_BACKGROUND_MODE_SETTINGS_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        ctx.registerReceiver(this, intentFilter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        boolean actionNewDay = action.equals(TimeAndDateModel.ACTION_NEW_DAY);
        boolean settingsChanged = action.equals(GeneralPreferenceFragment.ACTION_BACKGROUND_MODE_SETTINGS_CHANGED);
        int dayOfMonth = intent.getIntExtra( TimeAndDateModel.KEY_DAY_OF_MONTH, 0);

        if (action.equals(Intent.ACTION_TIME_TICK))
            mTickCnt++;

        updateBackground( settingsChanged, actionNewDay, dayOfMonth);
    }

    private void updateBackground( boolean settingsChanged, boolean newDay, int dayOfMonth) {
        String bgMode = getBackgroundMode();

        if( bgMode.equals( STANDARD) || bgMode.equals( SET1) || bgMode.equals( SET2)) {
            if( settingsChanged || newDay) {
                mView.setPicture(dayOfMonth);
                Log.d(TAG, "update Picture [bgmode: " + bgMode + "] dayOfMonth: " + dayOfMonth);
            }
        } else if( bgMode.equals( WEBCAM)) {
            if((mTickCnt>=30 || settingsChanged || newDay) && isScreensaferOff()) {
                mTickCnt = 0;
                Log.d(TAG, "update Picture by timer [bgmode: " + bgMode + "]");
                new WebcamContent().execute( WEBCAM_URL);
            }

        } else if( bgMode.equals(WEBRANDOM)) {
            if((mTickCnt>=60 || settingsChanged || newDay) && isScreensaferOff()) {
                mTickCnt = 0;
                Log.d(TAG, "update Picture by timer [bgmode: " + bgMode + "] keyword: " + getWebrandomKeyword());
                new LoremflickrContent().execute( LOREMFLICKR_URL);
            }
        }
    }

    public void nextWebcamPic() {
        Log.d(TAG, "update webcam pic by user click");
        new WebcamContent().execute( WEBCAM_URL);
    }

    public void nextWebrandomPic() {
        Log.d(TAG, "update webrandom pic by user click");
        new LoremflickrContent().execute( LOREMFLICKR_URL);
    }

    private String getBackgroundMode() {
        return PreferenceManager.getDefaultSharedPreferences(mCtx).getString(BackgroundModel.BACKGROUND_MODE, STANDARD);
    }

    private boolean isScreensaferOff() {
        return PreferenceManager.getDefaultSharedPreferences(mCtx).getBoolean(ScreensaverActivity.SCREENSAVER_STATUS, true);
    }

    private String getWebrandomKeyword() {
        return PreferenceManager.getDefaultSharedPreferences(mCtx).getString(LOREMFLICKR_KEYWORD, "cat");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Webcam
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class WebcamContent extends AsyncTask<String, String, Bitmap> {
        String url = null;

        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];

            try {
                Log.d(TAG, "WebcamContent doInBackground [url: " + url + "]");
                return BitmapFactory.decodeStream( new URL(url).openConnection().getInputStream());
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
                return null;
            }
        }

        // see: https://stackoverflow.com/questions/12837523/android-how-to-cut-some-part-of-image-and-show-it-in-imageview
        @Override
        protected void onPostExecute( Bitmap bmpImg) {
            if (bmpImg != null) {
                Matrix matrix = new Matrix();
                Bitmap resizedBitmap = null;

                switch (mWebcamPartOfImage) {
                    case LEFT_PART_OF_IMAGE:
                        resizedBitmap = Bitmap.createBitmap(bmpImg, 0, 0, 400, 600, matrix, true);
                        break;
                    case CENTER_PART_OF_IMAGE:
                        resizedBitmap = Bitmap.createBitmap(bmpImg, 200, 0, 400, 600, matrix, true);
                        break;
                    case RIGHT_PART_OF_IMAGE:
                        resizedBitmap = Bitmap.createBitmap(bmpImg, 400, 0, 400, 600, matrix, true);
                        break;
                }

                mWebcamPartOfImage++;
                if (mWebcamPartOfImage > RIGHT_PART_OF_IMAGE)
                    mWebcamPartOfImage = LEFT_PART_OF_IMAGE;

                if (resizedBitmap != null) {
                    BitmapDrawable bmd = new BitmapDrawable(mCtx.getResources(), resizedBitmap);

                    if (bmd != null)
                        mView.showPicture(bmd);
                    else
                        Log.w(TAG, "BitmapDrawable is null [" + (url == null ? "null" : url) + "]");
                } else
                    Log.w(TAG, "resizedBitmap is null [" + (url == null ? "null" : url) + "]");
            } else
                Log.w(TAG, "bmpImg is null [" + (url == null ? "null" : url) + "]");
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Loremflickr
    // http://loremflickr.com/800/1280/cat  (see: http://loremflickr.com/)
    // loremflicks doc: 2016-09-15: To limit use of server resources, served images will not be larger than 1280 pixels to a side.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class LoremflickrContent extends AsyncTask<String, String, Drawable> {

        @Override
        protected Drawable doInBackground(String... params) {
            String url = params[0];
            String searchKeyword = getWebrandomKeyword();
            String fullUrl = url + searchKeyword;

            try {
                Log.d(TAG, "LoremflickrContent doInBackground [url: " + fullUrl + "]");
                return Drawable.createFromStream(((java.io.InputStream) new java.net.URL(fullUrl).getContent()), "loremflickr");
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute( Drawable drawImg) {
            if( drawImg != null)
                mView.showPicture( drawImg);
        }
    }
}
