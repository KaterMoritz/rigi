package biz.kindler.rigi.modul.sonnerie;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.system.Log;

/**
 * Created by P.Kindler
 * patrick.kindler@schindler.com (kindlepa)
 * TMG PORT Technology
 * 2019-08-14
 *
 * Image Slider, see: https://www.youtube.com/watch?v=Q2FPDI99-as
 */
public class DoorCamArchiveActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = DoorCamArchiveActivity.class.getSimpleName();

    private static final float      IMAGE_ROTATION                      = 90;

    private ViewPager               mViewPager;
    private CustomPagerAdapter      mPagerAdapter;
    private HorizontalScrollView    mArchiveScrollView;
    private TextView                mImgTitleTxtView;
    private int                     mDayOfYearToday;
    private String                  mImgPath;
    private int                     mSizeOfArchiveImages;  // count of files
    private int                     mSelectedScrollIdx;
    private SimpleDateFormat        mDateTimeFormatter;
    private SimpleDateFormat        mTimeFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_cam);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        Bundle b = getIntent().getExtras();


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Haustür Kamera Archiv");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mDateTimeFormatter = new SimpleDateFormat("dd.MM.yy HH:mm");
        mDateTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        mTimeFormatter = new SimpleDateFormat("HH:mm");
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        mDayOfYearToday = new GregorianCalendar().get(Calendar.DAY_OF_YEAR);

        mArchiveScrollView = (HorizontalScrollView)findViewById(R.id.cam_archive_scrollview);
        mArchiveScrollView.setSmoothScrollingEnabled( true);
      //  LinearLayout thumbImgLayout = (LinearLayout)findViewById(R.id.thumbImgLayout);
      //  LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    //    RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.cam);
     //   ImageView firstThumbImg = null;  // to show the first image on startup activity

        // Image Timestamp title
        mImgTitleTxtView = (TextView)findViewById(R.id.img_timestamp);

        mImgPath = new File(getImgPathDir()).getAbsolutePath();
        File[] fileArr = getArchiveFiles();
        mSizeOfArchiveImages = fileArr.length;

        if( fileArr != null && mSizeOfArchiveImages > 0) {
            Arrays.sort(fileArr, Collections.reverseOrder());
            initThumbImgLayout( fileArr);
        }

        mViewPager = (ViewPager)findViewById(R.id.viewpager);
        mPagerAdapter = new CustomPagerAdapter(this, fileArr);
        mViewPager.setAdapter(mPagerAdapter);


       // if(firstThumbImg != null)
        //    onClick( firstThumbImg);
    }

    private void initThumbImgLayout( File[] fileArr) {
        LinearLayout thumbImgLayout = (LinearLayout)findViewById(R.id.thumbImgLayout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.cam);
        ImageView firstThumbImg = null;  // to show the first image on startup activity

        int idx=0;
        for (File file: fileArr) {
            String fileAbsName = file.getAbsolutePath();
            String filename = file.getName();
            String fileTimestamp = filename.substring(0, filename.length() -4); // remove .jpg from filename

            Bitmap bmp = BitmapFactory.decodeFile(fileAbsName);
            if( bmp != null) {
                Bitmap bmpThumb = Bitmap.createScaledBitmap(bmp, 100, 100, true);

                RelativeLayout imgViewLayout = (RelativeLayout) inflater.inflate(R.layout.thumb_view, mainLayout, false);
                ImageView imgView = imgViewLayout.findViewById(R.id.thumb_img);
                imgView.setTag(idx + ":" + fileTimestamp);
                imgView.setImageBitmap(bmpThumb);
                imgView.setRotation(IMAGE_ROTATION);
                imgView.setOnClickListener(this);
                thumbImgLayout.addView(imgViewLayout);

                if( firstThumbImg == null)
                    firstThumbImg = imgView;
            }
            idx++;
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

    private void resetThumbImgSelection() {
        LinearLayout thumbImgLayout = (LinearLayout)findViewById(R.id.thumbImgLayout);
        int childCnt = thumbImgLayout.getChildCount();
        for(int cnt=0; cnt <childCnt; cnt++)
            thumbImgLayout.getChildAt(cnt).setBackgroundResource( R.color.colorDeepBlack);
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();

        if( tag != null) { // on thumb img clicked
            String[] tagData = ((String)tag).split( ":");
            mSelectedScrollIdx = Integer.parseInt( tagData[0]);
            Log.d(TAG, "Usery clicked on thumb img: [scrollIdx: " + mSelectedScrollIdx + ", fileid: " + tagData[1] + "]");
            String fileAbs = null;
            if( tagData.length == 2)
                fileAbs = getImgPathDir() + File.separator + tagData[1] + ".jpg";

            final Bitmap bmp = BitmapFactory.decodeFile(fileAbs);

            new Handler( Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    // showMainCamImg( bmp);
                    mViewPager.setCurrentItem(mSelectedScrollIdx, true);
                }
            });

            mImgTitleTxtView.setText( getReadableTimestamp( new Date( Long.parseLong( tagData[1]))));

            resetThumbImgSelection();
            RelativeLayout rl = (RelativeLayout)v.getParent();
            rl.setBackgroundResource( R.color.colorYellow);
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class CustomPagerAdapter extends PagerAdapter {

        private Context mCtx;
       // private int[] mImgIds = new int[] {R.drawable.hausrigi2, R.drawable.hausrigi, R.drawable.background_settings1};
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
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            String fileAbsName = mFileArr[position].getAbsolutePath();
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
