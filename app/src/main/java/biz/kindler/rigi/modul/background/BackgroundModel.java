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
import android.os.Handler;
import android.preference.PreferenceManager;

import java.net.URL;

import biz.kindler.rigi.Util;
import biz.kindler.rigi.modul.clock.TimeAndDateModel;
import biz.kindler.rigi.modul.entree.ScreensaverActivity;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.settings.GeneralPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 27.11.16.
 */

public class BackgroundModel extends BroadcastReceiver {

    private final static String TAG = BackgroundModel.class.getSimpleName();

    public static final String  BACKGROUND_CAMURL         = "background_camurl";
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                nextWebcamPic();
            }
        }, 5000);
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
        if((mTickCnt>=30 || settingsChanged || newDay) && isScreensaferOff()) {
            mTickCnt = 0;
            Log.d(TAG, "update Picture by timer");
            new WebcamContent().execute( Util.getBackgroundWebcamUrl(mCtx));
        }
    }

    public void nextWebcamPic() {
        Log.d(TAG, "update webcam pic by user click");
        new WebcamContent().execute( Util.getBackgroundWebcamUrl(mCtx));
    }

    private boolean isScreensaferOff() {
        return PreferenceManager.getDefaultSharedPreferences(mCtx).getBoolean(ScreensaverActivity.SCREENSAVER_STATUS, true);
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

                try {
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
                } catch(Exception ex) {
                    Log.w(TAG, ex.getMessage());
                    resizedBitmap = bmpImg;
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
}
