package biz.kindler.rigi.modul;

import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 31.10.16.
 */

public class TwoLinesViewHolder extends RecyclerView.ViewHolder {


    public ImageView    mImg;
    public TextView     mTitle;
    public TextView	    mLine2;
    public TextView	    mLine2Center;
    public TextView	    mLine2Right;
    public ImageView    mIcon1;
    public ImageView    mIcon2;
    public int          mPos;
    private ViewGroup.LayoutParams  mDefaultParams;
    private ViewGroup.LayoutParams  mForInvisibleParams;


    public TwoLinesViewHolder(View view) {
        super(view);
        view.setClickable(false);
        mImg = (ImageView)view.findViewById( R.id.icon);
        mTitle = (TextView)view.findViewById( R.id.title);
        mLine2 = (TextView)view.findViewById( R.id.line2);
        mLine2Center = (TextView)view.findViewById( R.id.line2_center);
        mLine2Right = (TextView)view.findViewById( R.id.line2_right);
        mIcon1 = (ImageView)view.findViewById( R.id.icon1);
        mIcon2 = (ImageView)view.findViewById( R.id.icon2);

        mDefaultParams = itemView.getLayoutParams();
        mForInvisibleParams = new RelativeLayout.LayoutParams(0, 0);
    }

    public void updateViewComponent( int resId, String text) {
        View view = itemView.findViewById( resId);
        if( view != null) {
            if (view instanceof TextView)
                ((TextView) view).setText(text);
            else if (view instanceof ImageView && text != null) {
                int imgResId = Integer.parseInt( text);
                if( imgResId > -1)
                    ((ImageView)view).setImageResource( Integer.parseInt( text));
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

    public void setComponentVisibility( int resId, boolean visible) {
        View view = itemView.findViewById( resId);
        if( view != null)
            view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    public void showVisible( boolean status) {
        itemView.setLayoutParams( status ? mDefaultParams : mForInvisibleParams);
    }

    public void showHighlighted( boolean status) {
        itemView.setBackgroundResource( status ? R.drawable.round_border_yellow : R.drawable.round_border_gray);
    }
}
