package biz.kindler.rigi.modul.entree;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import biz.kindler.rigi.modul.system.Log;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 27.11.16.
 */

public class EntreeView {

    private final static String TAG = EntreeView.class.getSimpleName();

    public static float            DISPLAY_BRIGHT  = 0.7f;
    public static float            DISPLAY_DARK    = 0f;

    private Context    mCtx;

    public EntreeView( Context ctx) {
        mCtx = ctx;
       // Activity activity = (Activity)ctx;
    }

    public void setDisplayBrightness( final float value) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    WindowManager.LayoutParams layout = ((Activity)mCtx).getWindow().getAttributes();
                    layout.screenBrightness = value; // bright ? 0.7f : 0f;   // 1.0f
                    Log.d(TAG, "screenBrightness: " + value);
                    ((Activity)mCtx).getWindow().setAttributes(layout);;
                } catch (Exception e) {
                    Log.w(TAG, e.getMessage());
                }
            }
        });
    }
}
