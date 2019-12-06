package biz.kindler.rigi.modul.background;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 27.11.16.
 */

public class BackgroundView implements View.OnTouchListener {

    private DrawerLayout    mDrawer;
    private BackgroundModel mModel;
    private boolean         mAllowClick;

    private androidx.recyclerview.widget.RecyclerView  mListView;

    public BackgroundView( Context ctx) {
        mDrawer = (DrawerLayout)((Activity)ctx).findViewById(R.id.drawer_layout);
        mListView = ((Activity)ctx).findViewById(R.id.main_list_view);
        mListView.setOnTouchListener( this);
        mAllowClick = true;
    }

    public void setModel( BackgroundModel model) {
        mModel = model;
    }


    public void showPicture( Drawable drawImg) {
        mDrawer.setBackground( drawImg);
    }

    private void blockClickForAWhile() {
        // prevent for clicking too much in a short time
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAllowClick = true;
            }
        }, 5000);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && mListView.findChildViewUnder(event.getX(), event.getY()) == null) {
            // Touch outside items here
            if( mAllowClick) {
                mModel.nextWebcamPic();
                mAllowClick = false;
                blockClickForAWhile();
            }
        }
        return false;
    }
}
