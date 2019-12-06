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

public class OneButtonViewHolder extends RecyclerView.ViewHolder {

    public ImageView    mImg;
    public TextView     mTitle;
    public TextView	    mInfo;
    public TextView	    mInfoButton;
    public Button       mButton;
    public int          mPos;
    private ViewGroup.LayoutParams  mDefaultParams;
    private ViewGroup.LayoutParams  mForInvisibleParams;

    public OneButtonViewHolder(final View view) {
        super( view);
        view.setClickable(false);
        mImg = (ImageView)view.findViewById( R.id.icon);
        mTitle = (TextView)view.findViewById( R.id.title);
        mInfo = (TextView)view.findViewById( R.id.modul_info);
        mInfoButton = (TextView)view.findViewById( R.id.button_info);
        mButton = (Button)view.findViewById( R.id.button);
       // mDefaultParams = itemView.getLayoutParams();
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
               // ((ImageView)view).setImageDrawable(ContextCompat.getDrawable(ctx, Integer.parseInt(text)));
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
        if( mDefaultParams == null)
            mDefaultParams = itemView.getLayoutParams();

      //  itemView.setLayoutParams( status ? mDefaultParams : mForInvisibleParams);
        itemView.setVisibility(status ? View.VISIBLE : View.GONE);
        // itemView.getLayoutParams().notify();
    }

    public void showHighlighted( boolean status) {
        itemView.setBackgroundResource( status ? R.drawable.round_border_yellow : R.drawable.round_border_gray);
    }


}
