package biz.kindler.rigi.modul.sonnerie;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.R;
import biz.kindler.rigi.Util;
import biz.kindler.rigi.settings.CamPreferenceFragment;


/**
 * Created by P.Kindler
 * patrick.kindler@schindler.com (kindlepa)
 * TMG PORT Technology
 * 2019-08-20
 */
public class DoorCamLiveActivity extends AppCompatActivity implements View.OnClickListener {


    private final static String TAG = DoorCamLiveActivity.class.getSimpleName();

    private static final String MAIN_CAM_IMG_TAG = "MainCamImgTag";
    private static final String RECORD_ON_TAG    = "RecordONTag";
    private static final String RECORD_OFF_TAG   = "RecordOFFTag";
    private static final int    UPDATE_INTERVALL = 0;//50;
    private static final int    UPDATE_INTERVALL_FOR_RETRAY = 50;//50;

    private static final int    SHOW_DOWNLOAD_IMAGE_DURATION    = 500; // ms

    public static final String  OPEN_BY         = "open-by";
    public static final int     OPEN_MANUELL    = 0;
    public static final int     OPEN_AUTO       = 1;

    private static int          SOCKET_TIMEOUT_MS           = 3000;
    private static int          MAX_RETRIES                 = 10;

    private static final int    MAN_OPEN_DO_CLOSE_ACTIVITY_AFTER    = 300000; //120000;
    private static final int    AUTO_OPEN_DO_CLOSE_ACTIVITY_AFTER   = 60000; //120000;

    private RequestQueue        mRequestQueue;
    private Handler             mTimerHandler;
    private ImageView           mImageView;
    private ImageView           mStoredImageSymbol;
    private ImageButton         mRecordBtn;
    private int                 mRotation;
    private String              mDoorCamUrl;
    private String              mImgPath;
    private boolean             mRunCamLoading;
    private int                 mMaxStoredPicOnRing;
    private int                 mStoredPicOnRingCnt;
    private int                 mStorePicPeriod;
    private Bitmap              mCurrBitmap;
    private Timer               mStorePicTimer;
    private Timer               mCloseActivityTimer;
    private int                 mOpenBy;    // OPEN_MANUELL | OPEN_AUTO


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_live_cam);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Haustür Kamera");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Bundle b = getIntent().getExtras();
        mOpenBy = b.getInt(OPEN_BY, OPEN_MANUELL);
        Log.d(TAG, "DoorCamLiveActivity open by " + (mOpenBy == OPEN_MANUELL ? "MANUELL" : "SONNERIE"));
        mStoredPicOnRingCnt = 0;

        // cam image
        mImageView = (ImageView) findViewById(R.id.live_image);
        mImageView.setImageResource(R.drawable.cam_logo2);
        mImageView.setOnClickListener( this);

        // download symbol
        mStoredImageSymbol = (ImageView) findViewById(R.id.download_image);

        // record button
        mRecordBtn = (ImageButton) findViewById(R.id.record_button);
        mRecordBtn.setTag( RECORD_OFF_TAG);
        mRecordBtn.setVisibility( View.VISIBLE);
        mRecordBtn.setOnClickListener( this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mRequestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mDoorCamUrl = prefs.getString(CamPreferenceFragment.DOOR_CAM, null);  //
        mRotation = Integer.parseInt(prefs.getString(CamPreferenceFragment.CAM_ROTATION, "0"));
        mImgPath = new File(getImgPathDir()).getAbsolutePath();
        mStorePicPeriod = Integer.parseInt(prefs.getString(CamPreferenceFragment.STORE_PIC_INTERVALL, "3")) * 1000;
        mMaxStoredPicOnRing = Integer.parseInt(prefs.getString(CamPreferenceFragment.MAX_STORE_PIC_ON_BELL_RING, "15"));

        Log.d(TAG, "camUrl: " + mDoorCamUrl + ", rotation: " + mRotation + ", storePicPeriod(ms): " + mStorePicPeriod + ", maxStoredPicOnRing: " + mMaxStoredPicOnRing);

        mRunCamLoading = true;
        mStoredPicOnRingCnt = 0;
        mTimerHandler = new Handler();
        mTimerHandler.post(camRequestRunnable);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mImageView.setImageBitmap(null);
            }
        }, 1000);

        if(mOpenBy == OPEN_AUTO) {
            startRecordPictures();
        }

        mCloseActivityTimer = new Timer();
        mCloseActivityTimer.schedule( new CloseActivityTimerTask(), mOpenBy == OPEN_MANUELL ? MAN_OPEN_DO_CLOSE_ACTIVITY_AFTER : AUTO_OPEN_DO_CLOSE_ACTIVITY_AFTER);
    }

    @Override
    public void onPause() {
        super.onPause();
        mRunCamLoading = false;
        mTimerHandler.removeCallbacks(camRequestRunnable);
    }

    private Runnable camRequestRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "fetching cam image [" + mDoorCamUrl + "]");
            loadImage();
        }
    };

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();

        if (tag == null || tag.toString().equals(MAIN_CAM_IMG_TAG)) {
            Log.i(TAG, "Closed Cam Activity by user click");
            finish();
        } else if (tag != null && (tag.equals(RECORD_ON_TAG) || tag.equals(RECORD_OFF_TAG))) {
            if (tag.equals(RECORD_OFF_TAG)) {
                startRecordPictures();
            } else if (tag.equals(RECORD_ON_TAG)) {
                stopRecordPictures();
            }
        }
    }

    private void startRecordPictures() {
        showRecordOn();
        mStorePicTimer = new Timer();
        mStorePicTimer.schedule( new StorePicTimerTask(), 0, mStorePicPeriod);
    }

    private void stopRecordPictures() {
        showRecordOff();
        mStorePicTimer.cancel();
    }

    private void showRecordOn() {
        mRecordBtn.setBackgroundResource(R.drawable.cam_rec);
        mRecordBtn.setTag(RECORD_ON_TAG);
    }

    private void showRecordOff() {
        mRecordBtn.setBackgroundResource(R.drawable.cam_gray);
        mRecordBtn.setTag(RECORD_OFF_TAG);
    }

    private void loadImage() {
        ImageRequest imgRequest = new ImageRequest( mDoorCamUrl, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        if( mRotation == 0)
                            mCurrBitmap = bitmap;
                        else {
                            mCurrBitmap = rotateBitmap(bitmap, Float.valueOf(mRotation));
                          //  mImageView.setRotation(mRotation); not working
                        }

                        mImageView.setImageBitmap(mCurrBitmap);

                        if(mRecordBtn.getVisibility() == View.INVISIBLE) {
                            mRecordBtn.setVisibility(View.VISIBLE);
                        }

                        runDelayedCamRequest(UPDATE_INTERVALL);
                    }
                }, 0, 0, ImageView.ScaleType.FIT_CENTER, Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "updatePicture failed [camUrl:" + mDoorCamUrl + "] response: " + error);
                        Util.showToastInUiThread(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG);
                        mImageView.setImageBitmap( BitmapFactory.decodeResource(getResources(), R.drawable.cam_logo_funny2));
                        mRecordBtn.setVisibility(View.INVISIBLE);
                        runDelayedCamRequest(UPDATE_INTERVALL_FOR_RETRAY);
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "text/html,application/xhtml+xml,application/xml"); //
                return headers;
            }

        };

        imgRequest.setShouldCache(false);
        imgRequest.setRetryPolicy(new DefaultRetryPolicy(SOCKET_TIMEOUT_MS, MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(imgRequest);
    }

    private void runDelayedCamRequest(final int delay) {
        if( mRunCamLoading) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mTimerHandler.post(camRequestRunnable);
                }
            }, delay);
        }
    }

    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return source == null ? null : Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);//true);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Store picture Timertask
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public String getImgPathDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "cam";
    }

    private class StorePicTimerTask extends TimerTask {

        public void run() {
            try {
                if( mStoredPicOnRingCnt < mMaxStoredPicOnRing) {
                    String filename = new Date().getTime() + ".jpg";
                    File currFile = new File(mImgPath + File.separator + filename);

                    FileOutputStream fOutputStream = new FileOutputStream(currFile);
                    mCurrBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);
                    fOutputStream.flush();
                    fOutputStream.close();

                    // show the download symbol for a short time
                    showDownloadImage(SHOW_DOWNLOAD_IMAGE_DURATION);
                    mStoredPicOnRingCnt++;

                    Log.d(TAG, currFile.getName() + " ok (" + readableFileSize(currFile.length()) + ") mStoredPicOnRingCnt: " + mStoredPicOnRingCnt);
                } else {
                    if( mStorePicTimer != null) {
                        mStorePicTimer.cancel();
                    }
                    showRecordOffInUIThread();
                    Log.d(TAG, "MaxStoredPicOnRing reached: [mStoredPicOnRingCnt: " + mStoredPicOnRingCnt + ", mMaxStoredPicOnRing: " + mMaxStoredPicOnRing + "]");
                }
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
            }
        }

        // thanks to: https://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
        public String readableFileSize(long size) {
            if(size <= 0) return "0";
            final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
            int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

        private void showDownloadImage( final int durationMs) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mStoredImageSymbol.setVisibility(View.VISIBLE);
                }
            });

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mStoredImageSymbol.setVisibility(View.INVISIBLE);
                }
            }, durationMs);
        }

        private void showRecordOffInUIThread() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    showRecordOff();
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // close activity after a while task
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class CloseActivityTimerTask extends TimerTask {

        public void run() {
            Log.i(TAG, "Closed Cam Activity by timer");
            finish();
        }
    }

}
