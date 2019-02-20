package biz.kindler.rigi.modul;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 31.10.16.
 */

public class ThreeLinesViewHolder extends RecyclerView.ViewHolder {

    public ImageView    mImg;
    public TextView     mTitle;
    public TextView     mRow1Col1, mRow1Col2, mRow1Col3;
    public TextView	    mRow2Col1, mRow2Col2, mRow2Col3;
    public TextView	    mRow3Col1, mRow3Col2, mRow3Col3;
    public int          mPos;
    private ViewGroup.LayoutParams  mDefaultParams;
    private ViewGroup.LayoutParams  mForInvisibleParams;

    public ThreeLinesViewHolder(View view) {
        super(view);
        view.setClickable(false);
        mImg = (ImageView)view.findViewById( R.id.icon);
        // title
        mTitle = (TextView)view.findViewById( R.id.title);
        // Row 1
        mRow1Col1 = (TextView)view.findViewById( R.id.row1col1);
        mRow1Col2 = (TextView)view.findViewById( R.id.row1col2);
        mRow1Col3 = (TextView)view.findViewById( R.id.row1col3);
        // Row 2
        mRow2Col1 = (TextView)view.findViewById( R.id.row2col1);
        mRow2Col2 = (TextView)view.findViewById( R.id.row2col2);
        mRow2Col3 = (TextView)view.findViewById( R.id.row2col3);
        // Row 3
        mRow3Col1 = (TextView)view.findViewById( R.id.row3col1);
        mRow3Col2 = (TextView)view.findViewById( R.id.row3col2);
        mRow3Col3 = (TextView)view.findViewById( R.id.row3col3);

        mDefaultParams = itemView.getLayoutParams();
        mForInvisibleParams = new RelativeLayout.LayoutParams(0, 0);
    }

    public void showVisible( boolean status) {
        itemView.setLayoutParams( status ? mDefaultParams : mForInvisibleParams);
    }

    public void updateViewComponent( int resId, String text) {
        View view = itemView.findViewById( resId);
        if( view != null) {
            if (view instanceof TextView)
                ((TextView) view).setText(text);
            else if (view instanceof ImageView && text != null) {
                int imgResId = Integer.parseInt(text);
                if (imgResId > -1)
                    ((ImageView) view).setImageResource(Integer.parseInt(text));
            }
        }
    }

    public void updateViewComponent( int resId, Drawable drawable) {
        if( drawable != null) {
            View view = itemView.findViewById(resId);
            if (view != null) {
                if (view instanceof ImageView)
                    ((ImageView) view).setImageDrawable(drawable);
            }
        }
    }
}
