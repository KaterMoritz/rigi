package biz.kindler.rigi.modul.background;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.RelativeLayout;

import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 27.11.16.
 */

public class BackgroundView implements View.OnClickListener {

    private Context         mCtx;
    private DrawerLayout    mDrawer;
    private RelativeLayout  mMainLayout;
    private int             mClickCnt;
    private int             mDayOfMonth;
    private BackgroundModel mModel;


    public BackgroundView( Context ctx) {
        mCtx = ctx;
        mDrawer = (DrawerLayout)((Activity)ctx).findViewById(R.id.drawer_layout);

        CoordinatorLayout cLayout = ((CoordinatorLayout)((Activity)ctx).findViewById(R.id.coordinator_layout));
//        mMainLayout = (RelativeLayout)((Activity)ctx).findViewById(R.id.content_main);
  //      mMainLayout.setOnClickListener( this);
        cLayout.setOnClickListener( this);
    }

    public void setModel( BackgroundModel model) {
        mModel = model;
    }

    public void setPicture( int dayOfMonth) {
        mDayOfMonth = dayOfMonth;
        String mode = getBackgroundMode();

        if(mode.equals( "standard") || mode.equals( "set1"))
            mDrawer.setBackground(ContextCompat.getDrawable(mCtx, getResIdForDay(mDayOfMonth, mode.equals( "standard"))));
        else if(mode.equals( "set2"))
            mDrawer.setBackground(ContextCompat.getDrawable(mCtx, getResIdForDay(dayOfMonth)));
    }

    public void showPicture( Drawable drawImg) {
        mDrawer.setBackground( drawImg);
    }

    private int getResIdForDay(int day, boolean defaultSerie) {
        switch (day) {
            case 0:
                return defaultSerie ? R.drawable.background1 : R.drawable.background_p1;
            case 1:
                return defaultSerie ? R.drawable.background2 : R.drawable.background_p2;
            case 2:
                return defaultSerie ? R.drawable.background3 : R.drawable.background_p3;
            case 3:
                return defaultSerie ? R.drawable.background4 : R.drawable.background_p4;
            case 4:
                return defaultSerie ? R.drawable.background5 : R.drawable.background_p5;
            case 5:
                return defaultSerie ? R.drawable.background6 : R.drawable.background_p6;
            case 6:
                return defaultSerie ? R.drawable.background7 : R.drawable.background_p7;
            case 7:
                return defaultSerie ? R.drawable.background8 : R.drawable.background_p8;
            case 8:
                return defaultSerie ? R.drawable.background9 : R.drawable.background_p9;
            case 9:
                return defaultSerie ? R.drawable.background10 : R.drawable.background_p10;
            case 10:
                return defaultSerie ? R.drawable.background11 : R.drawable.background_p11;
            case 11:
                return defaultSerie ? R.drawable.background12 : R.drawable.background_p12;
            case 12:
                return defaultSerie ? R.drawable.background13 : R.drawable.background_p13;
            case 13:
                return defaultSerie ? R.drawable.background14 : R.drawable.background_p14;
            case 14:
                return defaultSerie ? R.drawable.background15 : R.drawable.background_p15;
            case 15:
                return defaultSerie ? R.drawable.background16 : R.drawable.background_p16;
            case 16:
                return defaultSerie ? R.drawable.background17 : R.drawable.background_p17;
            case 17:
                return defaultSerie ? R.drawable.background18 : R.drawable.background_p18;
            case 18:
                return defaultSerie ? R.drawable.background19 : R.drawable.background_p19;
            case 19:
                return defaultSerie ? R.drawable.background20 : R.drawable.background_p20;
            case 20:
                return defaultSerie ? R.drawable.background21 : R.drawable.background_p21;
            case 21:
                return defaultSerie ? R.drawable.background22 : R.drawable.background_p22;
            case 22:
                return defaultSerie ? R.drawable.background23 : R.drawable.background_p23;
            case 23:
                return defaultSerie ? R.drawable.background24 : R.drawable.background_p24;
            case 24:
                return defaultSerie ? R.drawable.background25 : R.drawable.background_p25;
            case 25:
                return defaultSerie ? R.drawable.background26 : R.drawable.background_p26;
            case 26:
                return defaultSerie ? R.drawable.background27 : R.drawable.background_p27;
            case 27:
                return defaultSerie ? R.drawable.background28 : R.drawable.background_p28;
            case 28:
                return defaultSerie ? R.drawable.background29 : R.drawable.background_p29;
            case 29:
                return defaultSerie ? R.drawable.background30 : R.drawable.background_p30;
            case 30:
                return defaultSerie ? R.drawable.background31 : R.drawable.background_p31;
            default:
                return defaultSerie ? R.drawable.background1 : R.drawable.background_p1;
        }
    }

    private int getResIdForDay(int day) {
        if( mClickCnt > 2)
            mClickCnt = 0;

        switch (mClickCnt) {
            case 0 : return R.drawable.background_katy1;
            case 1 : return R.drawable.background_katy2;
            case 2 : return R.drawable.background_katy3;
            default : return R.drawable.background_katy1;
        }
    }

    @Override
    public void onClick(View v) {
        String bgMode = getBackgroundMode();

        if( bgMode.equals( BackgroundModel.WEBCAM))
            mModel.nextWebcamPic();
        else if( bgMode.equals( BackgroundModel.WEBRANDOM))
            mModel.nextWebrandomPic();
        else {
            mClickCnt++;
            if (mClickCnt > 30)
                mClickCnt = 0;

            setPicture(mClickCnt);
        }
    }

    private String getBackgroundMode() {
        return PreferenceManager.getDefaultSharedPreferences(mCtx).getString(BackgroundModel.BACKGROUND_MODE, BackgroundModel.STANDARD);
    }

}
