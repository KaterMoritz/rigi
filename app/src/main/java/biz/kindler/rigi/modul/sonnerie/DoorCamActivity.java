package biz.kindler.rigi.modul.sonnerie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.system.Log;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 30.06.17.
 */

public class DoorCamActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = DoorCamActivity.class.getSimpleName();

    private static final String DOORCAM_URL     = "https://rigipic.ch/rigikapellekulm.jpg";
   // private static final String DOORCAM_URL     = "http://192.168.178.35/html/cam_pic.php";
    // private static final String DOORCAM_URL     = "http://192.168.178.27/jpg/image.jpg?size=3";
   //private static final String DOORCAM_URL     = "http://192.168.178.27/axis-cgi/jpg/image.cgi?resolution=320x240&compression=25&camera=1";  // www.axis.com%2Ffiles%2Fmanuals%2Fvapix_video_streaming_52937_en_1307.pdf vapix_video_streaming_52937_en_1307.pdf Seite 11
   // private static final String DOORCAM_URL     = "http://192.168.178.27/axis-cgi/jpg/image.cgi?resolution=320x240&camera=1";  // www.axis.com%2Ffiles%2Fmanuals%2Fvapix_video_streaming_52937_en_1307.pdf vapix_video_streaming_52937_en_1307.pdf Seite 11

    public static final String  SHOW_TYPE       = "show-type";
    public static final int     SHOW_LIVE       = 0;
    public static final int     SHOW_ARCHIVE    = 1;
    public static final String  OPEN_BY         = "open-by";
    public static final int     OPEN_MANUELL    = 0;
    public static final int     OPEN_AUTO       = 1;

    private static final String MAIN_CAM_IMG_TAG = "MainCamImgTag";
    private static final String RECORD_ON_TAG    = "RecordONTag";
    private static final String RECORD_OFF_TAG   = "RecordOFFTag";

    private RelativeLayout      mCamLayout;
    private Timer               mImgUpdateTimer;
    private Timer               mCloseActivityTimer;
    private static final int    UPDATE_INTERVALL                    = 50;
    private static final int    MAN_OPEN_DO_CLOSE_ACTIVITY_AFTER    = 1200000; //120000;
    private static final int    AUTO_OPEN_DO_CLOSE_ACTIVITY_AFTER   = 60000; //120000;
    private static final int    MAX_PICTURES_WHEN_BELL_RINGS        = 15; //TODO: getPrefs()
    private static final int    STORE_EVERY_X_PICTURE               = 20;  //TODO: getPrefs().getInt(SonneriePreferenceFragment.STORE_EVERY_X_PICTURE, false))
    private static final float  IMAGE_ROTATION                      = 90;
    private boolean             mDoKeepUpdateRunning;
    private int                 mStoreEveryXPicture;
    private int                 mPicCnt;
    private int                 mStoredPicCnt;
    private int                 mShowType;  // SHOW_LIVE | SHOW_ARCHIVE
    private int                 mOpenBy;    // OPEN_MANUELL | OPEN_AUTO
    private int                 mDayOfYearToday;
    private String              mImgPath;
    private HorizontalScrollView mArchiveScrollView;
    private TextView            mImgTitleTxtView;
    private SimpleDateFormat    mDateTimeFormatter;
    private SimpleDateFormat    mTimeFormatter;
    private ImageButton         mRecordBtn;
    private ImageView           mStoredImageSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        Bundle b = getIntent().getExtras();
        mShowType = b.getInt(SHOW_TYPE, SHOW_LIVE);
        mOpenBy = b.getInt(OPEN_BY, OPEN_MANUELL);
        mStoredPicCnt = 0;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Haustür Kamera");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        View cView = findViewById(R.id.cam);
        cView.setOnClickListener( this);

        mDateTimeFormatter = new SimpleDateFormat("dd.MM.yy HH:mm");
        mDateTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        mTimeFormatter = new SimpleDateFormat("HH:mm");
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        // the main cam image is the background image
        mCamLayout = (RelativeLayout) findViewById(R.id.cam);
        mCamLayout.setTag( MAIN_CAM_IMG_TAG);

        // record button
        mRecordBtn = (ImageButton) findViewById(R.id.record_button);
        // download symbol
        mStoredImageSymbol = (ImageView) findViewById(R.id.download_image);
        // Image Timestamp title
        mImgTitleTxtView = (TextView)findViewById(R.id.img_timestamp);

        if( mShowType == SHOW_LIVE) {
            mImgUpdateTimer = new Timer();
            mImgUpdateTimer.schedule(new UpdatePictureTimerTask(), 0);

            mRecordBtn.setTag( RECORD_OFF_TAG);
            mRecordBtn.setVisibility( View.VISIBLE);
            mRecordBtn.setOnClickListener( this);

            HorizontalScrollView archView = (HorizontalScrollView)findViewById(R.id.cam_archive);
            archView.setVisibility( View.INVISIBLE);
        }

        mCloseActivityTimer = new Timer();
        mCloseActivityTimer.schedule( new CloseActivityTimerTask(), mOpenBy == OPEN_MANUELL ? MAN_OPEN_DO_CLOSE_ACTIVITY_AFTER : AUTO_OPEN_DO_CLOSE_ACTIVITY_AFTER);

        mStoreEveryXPicture = STORE_EVERY_X_PICTURE; //TODO: getPrefs().getInt(SonneriePreferenceFragment.STORE_EVERY_X_PICTURE, false))
        mPicCnt = mStoreEveryXPicture; // to store the first pic
        mImgPath = new File(getImgPathDir()).getAbsolutePath();

        if( mShowType == SHOW_LIVE) {
            mImgTitleTxtView.setVisibility( View.INVISIBLE);
        }

        //if( mShowType == SHOW_LIVE && mOpenBy == OPEN_AUTO) {
            File filePath = new File(mImgPath);
            if( ! filePath.exists())
                filePath.mkdirs();

            Log.d(TAG, "CamImgPath: " + mImgPath);
        //}
        /*else*/ if( mShowType == SHOW_ARCHIVE) {
            mDayOfYearToday = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);

            mArchiveScrollView = (HorizontalScrollView) findViewById(R.id.cam_archive);
            LinearLayout thumbImgLayout = (LinearLayout)findViewById(R.id.thumbImgLayout);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.cam);
            ImageView firstThumbImg = null;  // to show the first image on startup activity

            File[] fileArr = getArchiveFiles();

            if( fileArr != null && fileArr.length > 0) {
                Arrays.sort(fileArr, Collections.reverseOrder());

                for (File file: fileArr) {
                    String fileAbsName = file.getAbsolutePath();
                    String filename = file.getName();
                    String fileTimestamp = filename.substring(0, filename.length() -4); // remove .jpg from filename

                    Bitmap bmp = BitmapFactory.decodeFile(fileAbsName);
                    if( bmp != null) {
                        Bitmap bmpThumb = Bitmap.createScaledBitmap(bmp, 100, 100, true);

                        RelativeLayout imgViewLayout = (RelativeLayout) inflater.inflate(R.layout.thumb_view, mainLayout, false);
                        ImageView imgView = (ImageView) imgViewLayout.findViewById(R.id.thumb_img);
                        imgView.setTag(fileTimestamp);
                        imgView.setImageBitmap(bmpThumb);
                        imgView.setRotation(IMAGE_ROTATION);
                        imgView.setOnClickListener(this);
                        thumbImgLayout.addView(imgViewLayout);

                        if( firstThumbImg == null)
                            firstThumbImg = imgView;
                    }
                }
            }

            if(firstThumbImg != null)
                onClick( firstThumbImg);
        }
    }

    private void resetThumbImgSelection() {
        LinearLayout thumbImgLayout = (LinearLayout)findViewById(R.id.thumbImgLayout);
        int childCnt = thumbImgLayout.getChildCount();
        for(int cnt=0; cnt <childCnt; cnt++)
            thumbImgLayout.getChildAt(cnt).setBackgroundResource( R.color.colorDeepBlack);
    }

    private File[] getArchiveFiles() {
        File dir = new File(mImgPath);
        return dir.listFiles();
    }

    public static String getImgPathDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "cam";
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDoKeepUpdateRunning = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        mDoKeepUpdateRunning = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDoKeepUpdateRunning = false;
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();

        if( tag == null || tag.toString().equals(MAIN_CAM_IMG_TAG)) {
            Log.i(TAG, "Closed Cam Activity by user click");
            finish();
        }
        else if( tag != null && (tag.equals(RECORD_ON_TAG) || tag.equals(RECORD_OFF_TAG))) {
            if(tag.equals(RECORD_OFF_TAG)) {
                mRecordBtn.setBackgroundResource(R.drawable.cam_rec);
                mRecordBtn.setTag( RECORD_ON_TAG);
            }
            else if(tag.equals(RECORD_ON_TAG)) {
                mRecordBtn.setBackgroundResource(R.drawable.cam_gray);
                mRecordBtn.setTag( RECORD_OFF_TAG);
            }
        }
        else if( tag != null) { // on thumb img clicked
            Log.d(TAG, "Usery clicked on thumb img: " + tag);
            String fileAbs = getImgPathDir() + File.separator + tag + ".jpg";
            Bitmap bmp = BitmapFactory.decodeFile(fileAbs);
            showMainCamImg( bmp);

            mImgTitleTxtView.setText( getReadableTimestamp( new Date( Long.parseLong((String)tag))));

            resetThumbImgSelection();
            RelativeLayout rl = (RelativeLayout)v.getParent();
            rl.setBackgroundResource( R.color.colorYellow);
        }

    }

    private void showMainCamImg( Bitmap bmpImg) {
        if( bmpImg != null) {
            try {
                //Matrix matrix = new Matrix();
               // Bitmap resizedBitmap = Bitmap.createBitmap(bmpImg, 0, 0, 800, 600, matrix, true);

               // if (resizedBitmap != null) {
                   // BitmapDrawable bmd = new BitmapDrawable(getApplicationContext().getResources(), bmpImg);// resizedBitmap);

                    //if (bmd != null) {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(IMAGE_ROTATION);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(bmpImg , 0, 0, bmpImg.getWidth(), bmpImg.getHeight(), matrix, true);
                        BitmapDrawable bmd = new BitmapDrawable(getApplicationContext().getResources(), rotatedBitmap);// resizedBitmap);
                        mCamLayout.setBackground((Drawable) bmd);
                   // }
                        //mCamImgView.setImageBitmap(bmd.getBitmap());
                //}
            } catch( Exception ex) {
                Log.w(TAG, ex.getMessage());
            }
        } else
            Log.w( TAG, "showMainCamImg is null");
    }

    private String getReadableTimestamp( Date date) {
        GregorianCalendar gcRef = new GregorianCalendar();
        gcRef.setTime(date);
        int dayOfYearRef = gcRef.get(Calendar.DAY_OF_YEAR);

        if( mDayOfYearToday == dayOfYearRef)
            return "Heute " + mTimeFormatter.format(date);
        else if((mDayOfYearToday - 1) == dayOfYearRef)
            return "Gestern " + mTimeFormatter.format(date);
        else
            return mDateTimeFormatter.format(date);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // update intervall task
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class UpdatePictureTimerTask extends TimerTask {

        public void run() {
            Log.d(TAG, "TimerTask Updating DoorCam image");
            new DoorCamTask().execute( DOORCAM_URL);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // cam picture update task
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class DoorCamTask extends AsyncTask<String, String, Bitmap> {
        String url = null;

        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];

            try {
                Log.d(TAG, "DoorCam doInBackground [url: " + url + "]");
                return BitmapFactory.decodeStream( new URL(url).openConnection().getInputStream());
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
                return null;
            }
        }

        // see: https://stackoverflow.com/questions/12837523/android-how-to-cut-some-part-of-image-and-show-it-in-imageview
        @Override
        protected void onPostExecute( Bitmap bmpImg) {
            if (bmpImg != null)
                showMainCamImg( bmpImg);
             else
                Log.w(TAG, "bmpImg is null [" + (url == null ? "null" : url) + "]");

            mPicCnt++;
            if((mOpenBy == OPEN_AUTO || mRecordBtn.getTag().equals( RECORD_ON_TAG)) && mPicCnt >= mStoreEveryXPicture && mStoredPicCnt < MAX_PICTURES_WHEN_BELL_RINGS) {
                mPicCnt = 0;

                if( bmpImg != null) {
                    mStoredPicCnt++;
                    String status = storePicture(bmpImg);
                    Log.d(TAG, "store picture: [" + status + "]");
                }
            } else
                mStoredImageSymbol.setVisibility( View.INVISIBLE);

            if( mDoKeepUpdateRunning)
                mImgUpdateTimer.schedule( new UpdatePictureTimerTask(), UPDATE_INTERVALL);
        }

        private String storePicture( Bitmap bmp) {
            try {
                String filename = new Date().getTime() + ".jpg";
                File currFile = new File( mImgPath + File.separator + filename);

                FileOutputStream fOutputStream = new FileOutputStream(currFile);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fOutputStream);
                fOutputStream.flush();
                fOutputStream.close();

                mStoredImageSymbol.setVisibility( View.VISIBLE);

                return currFile.getName() + " ok (" + readableFileSize(currFile.length()) + ")";
            } catch (Exception e) {
                String msg = e.getMessage();
                Log.w(TAG, msg);
                return msg;
            }
        }

        // thanks to: https://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
        public String readableFileSize(long size) {
            if(size <= 0) return "0";
            final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
            int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
            return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }
    }


}
