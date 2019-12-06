package biz.kindler.rigi.modul;

import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 21.10.16.
 */

public class TwoButtonViewHolder extends RecyclerView.ViewHolder {

    public ImageView    mImg;
    public TextView     mTitle;
    public TextView	    mInfo;
    public TextView	    mInfoButton1;
    public Button       mButton1;
    public Button       mButton11;
    public Button       mButton12;
    public TextView	    mInfoButton2;
    public Button       mButton2;
    public Button       mButton21;
    public Button       mButton22;
    public int          mPos;
    private ViewGroup.LayoutParams  mDefaultParams;
    private ViewGroup.LayoutParams  mForInvisibleParams;

    public TwoButtonViewHolder(View view) {
        super( view);
        view.setClickable(false);
        mImg = (ImageView)view.findViewById( R.id.icon);
        mTitle = (TextView)view.findViewById( R.id.title);
        mInfo = (TextView)view.findViewById( R.id.modul_info);
        mInfoButton1 = (TextView)view.findViewById( R.id.button1_info);
        mButton1 = (Button)view.findViewById( R.id.button1);
        mButton11 = (Button)view.findViewById( R.id.button1_1);
        mButton12 = (Button)view.findViewById( R.id.button1_2);
        mInfoButton2 = (TextView)view.findViewById( R.id.button2_info);
        mButton2 = (Button)view.findViewById( R.id.button2);
        mButton21 = (Button)view.findViewById( R.id.button2_1);
        mButton22 = (Button)view.findViewById( R.id.button2_2);

        mDefaultParams = itemView.getLayoutParams();
        mForInvisibleParams = new RelativeLayout.LayoutParams(0, 0);
    }

    public void updateViewComponent( int resId, String text) {
        View view = itemView.findViewById( resId);
        if( view != null) {
            if (view instanceof TextView)
                ((TextView)view).setText(text);
            else if (view instanceof ImageView && text != null) {
                int imgResId = Integer.parseInt( text);
                if( imgResId > -1)
                    ((ImageView)view).setImageResource( Integer.parseInt( text));
            }
            else if (view instanceof Button) {
                // todo: if text "enabled" or "disabled" do it
                ((Button) view).setText(text);
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

    public void showVisible( boolean status) {
        itemView.setLayoutParams( status ? mDefaultParams : mForInvisibleParams);
    }

    public void setComponentVisibility( int resId, boolean visible) {
        View view = itemView.findViewById( resId);
        if( view != null)
            view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
}
