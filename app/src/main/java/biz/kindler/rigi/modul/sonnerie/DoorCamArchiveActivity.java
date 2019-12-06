package biz.kindler.rigi.modul.sonnerie;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import biz.kindler.rigi.R;
import biz.kindler.rigi.Util;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.settings.CamPreferenceFragment;

/**
 * Created by P.Kindler
 * patrick.kindler@schindler.com (kindlepa)
 * TMG PORT Technology
 * 2019-08-14
 *
 * Image Slider, see: https://www.youtube.com/watch?v=Q2FPDI99-as
 */
public class DoorCamArchiveActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener, View.OnLongClickListener {

    private final static String TAG = DoorCamArchiveActivity.class.getSimpleName();

    private ViewPager               mViewPager;
    private CustomPagerAdapter      mPagerAdapter;
    private HorizontalScrollView    mArchiveScrollView;
    private LinearLayout            mThumbImgLayout;
    private RelativeLayout          mTimestampLayout;
    private ProgressBar             mProgressBar;
    private TextView                mImgTitleTxtView;
    private int                     mDayOfYearToday;
    private String                  mImgPath;
    private int                     mSizeOfArchiveImages;  // count of files
    private int                     mSelectedScrollIdx;
    private int                     mLastSelectedScrollIdx;
    private SimpleDateFormat        mDateTimeFormatter;
    private SimpleDateFormat        mTimeFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_cam);

        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Haustür Kamera Archiv");
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Bundle b = getIntent().getExtras();

        mDateTimeFormatter = new SimpleDateFormat("dd.MM.yy HH:mm");
        mDateTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        mTimeFormatter = new SimpleDateFormat("HH:mm:ss");
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        mDayOfYearToday = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);

        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility( View.VISIBLE);

        mArchiveScrollView = (HorizontalScrollView)findViewById(R.id.cam_archive_scrollview);
        mArchiveScrollView.setSmoothScrollingEnabled( true);

        // Image Timestamp title
        mImgTitleTxtView = (TextView)findViewById(R.id.img_timestamp);
        mTimestampLayout = (RelativeLayout)findViewById(R.id.layout_timestamp);
        mTimestampLayout.setVisibility( View.INVISIBLE);
        mImgPath = new File(getImgPathDir()).getAbsolutePath();
        mViewPager = (ViewPager)findViewById(R.id.viewpager);
        mViewPager.addOnPageChangeListener( this);

       // deleteCamFile( "1566400384959.jpg"); // /storage/emulated/0/cam/1566400384959.jpg
    }

    @Override
    public void onResume() {
        super.onResume();

        loadImages();
    }

    private void loadImages() {
        mProgressBar.setVisibility( View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                File[] fileArr = getArchiveFiles();
                mSizeOfArchiveImages = fileArr.length;

                Log.i(TAG, "SizeOfArchiveImages: " + mSizeOfArchiveImages + " pictures");
                Arrays.sort(fileArr, Collections.reverseOrder());

                if( mSizeOfArchiveImages > 100) {
                    fileArr = Arrays.copyOfRange(fileArr, 0, 100);
                    mSizeOfArchiveImages = fileArr.length;
                    Log.i(TAG, "NEW SizeOfArchiveImages: " + mSizeOfArchiveImages + " pictures");
                }

                if( fileArr != null && mSizeOfArchiveImages > 0) {
                    initThumbImgLayout( fileArr);
                }

                mPagerAdapter = new CustomPagerAdapter(getApplicationContext(), fileArr);
                mViewPager.setAdapter(mPagerAdapter);

                mProgressBar.setVisibility(View.INVISIBLE);
                mTimestampLayout.setVisibility( View.VISIBLE);
                if( fileArr.length > 0) {
                    selectThumb(0);
                }
            }
        }, 200);
    }

    private void initThumbImgLayout( File[] fileArr) {
        mThumbImgLayout = (LinearLayout)findViewById(R.id.thumbImgLayout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.cam);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int rotation = Integer.parseInt(prefs.getString(CamPreferenceFragment.CAM_ROTATION, "0"));

        for(int cnt=0; cnt <fileArr.length; cnt++) {
            File currFile = fileArr[cnt];
            String fileAbsName = currFile.getAbsolutePath();
            String filename = currFile.getName();
            String fileTimestamp = filename.substring(0, filename.length() -4); // remove .jpg from filename

            Bitmap bmp = BitmapFactory.decodeFile(fileAbsName);
            Bitmap bmpThumb = null;
            if( bmp != null) {
                bmpThumb = Bitmap.createScaledBitmap(bmp, 100, 100, false);
            }
            RelativeLayout imgViewLayout = (RelativeLayout) inflater.inflate(R.layout.thumb_view, mainLayout, false);
            imgViewLayout.setTag(cnt + ":" + fileTimestamp);

            ImageView imgView = imgViewLayout.findViewById(R.id.thumb_img);
            imgView.setTag(cnt + ":" + fileTimestamp);
            if( bmpThumb != null) {
                imgView.setImageBitmap(bmpThumb);
            }
            imgView.setRotation(rotation);
            imgView.setOnClickListener(this);
            imgView.setOnLongClickListener(this);
            mThumbImgLayout.addView(imgViewLayout);
        }
    }

    public static String getImgPathDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "cam";
    }

    private File[] getArchiveFiles() {
        File dir = new File(mImgPath);
        return dir.listFiles();
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

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();

        if( tag != null) { // on thumb img clicked
            String[] tagData = ((String)tag).split( ":");
            mSelectedScrollIdx = Integer.parseInt( tagData[0]);

            Log.d(TAG, "Usery clicked on thumb img: [scrollIdx: " + mSelectedScrollIdx + ", fileid: " + tagData[1] + "]");

            new Handler( Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mViewPager.setCurrentItem(mSelectedScrollIdx, true);
                }
            });

            selectThumb( mSelectedScrollIdx);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        Object tag = v.getTag();

        if( tag != null) { // on thumb img clicked
            final String[] tagData = ((String) tag).split(":");

            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("Bild (" + getReadableTimestamp( new Date( Long.parseLong( tagData[1]))) + ") löschen?");
            adb.setIcon(android.R.drawable.ic_dialog_alert);
            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            boolean status = deleteCamFile( tagData[1] + ".jpg");
                            Util.showToastInUiThread( getCtx(), status ? "gelöscht" : "fehler", Toast.LENGTH_LONG);
                            finish();
                            startActivity(getIntent());
                        }
                    }, 200);
                }
            });
            adb.setCancelable(true);
            adb.show();
        }

        return false;
    }

    private Context getCtx() {
        return this;
    }

    private void selectThumb( int arrIdx) {

        int childCnt = mThumbImgLayout.getChildCount();
        for(int cnt=0; cnt <childCnt; cnt++) {
            View view = mThumbImgLayout.getChildAt(cnt);
            view.setBackgroundResource( cnt == arrIdx ? R.color.colorYellow : R.color.colorDeepBlack);
            if( cnt == arrIdx) {
                String[] tagData = ((String)view.getTag()).split( ":");
                mImgTitleTxtView.setText( getReadableTimestamp( new Date( Long.parseLong( tagData[1]))));

                final Rect scrollBounds = new Rect();
                mArchiveScrollView.getHitRect(scrollBounds);
                if (view.getLocalVisibleRect(scrollBounds)) {
                    // imageView is within the visible window
                    Log.d(TAG, "TAG:" + view.getTag() + " is visible");
                } else {
                    // imageView is not within the visible window
                    Log.d(TAG, "TAG:" + view.getTag() + " is NOT visible");
                    mArchiveScrollView.pageScroll( arrIdx > mLastSelectedScrollIdx ? View.FOCUS_RIGHT : View.FOCUS_LEFT);
                }
            }
        }
        mLastSelectedScrollIdx = arrIdx;
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {
     //  Log.d(TAG, "onPageScrolled: i:" + i + ",v:" + v + ",i1:" + i1);
    }

    @Override
    public void onPageSelected(int i) {
        Log.d(TAG, "onPageSelected: i:" + i);
        selectThumb( i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    //    Log.d(TAG, "onPageScrollStateChanged: i:" + i);
    }

    private boolean deleteCamFile( String filenameAndSuffix) {
        String path = getImgPathDir() + File.separator + filenameAndSuffix;
        File file = new File( path);
        boolean status = file.delete();
        Log.d(TAG, "deleteCamFile: " + path + " status: " + status);
        return status;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class CustomPagerAdapter extends PagerAdapter {

        private Context mCtx;
        private File[] mFileArr;

        public CustomPagerAdapter( Context ctx, File[] fileArr) {
            mCtx = ctx;
            mFileArr = fileArr;
        }

        @Override
        public int getCount() {
            return mFileArr.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
            return view == obj;
        }

        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, int position) {
            ImageView imgView = new ImageView(mCtx);
            imgView.setTag( position);
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            String fileAbsName = mFileArr[position].getAbsolutePath();
            Log.d(TAG, "filename: " + fileAbsName + ", position: " + position);
            final Bitmap bmp = BitmapFactory.decodeFile(fileAbsName);
            if( bmp != null) {
                imgView.setImageBitmap(bmp);
            }

            collection.addView(imgView, 0);
            return imgView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup collection, int position, @NonNull Object view) {
            collection.removeView((View)view);
        }
    }
}
