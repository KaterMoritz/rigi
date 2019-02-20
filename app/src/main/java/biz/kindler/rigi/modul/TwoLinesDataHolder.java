package biz.kindler.rigi.modul;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.Date;

import biz.kindler.rigi.DataHolder;
import biz.kindler.rigi.MainActivity;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 31.10.16.
 */

public class TwoLinesDataHolder implements DataHolder, Serializable {

    private String      title;
    private int         modulId;
    private int         imgResId;
    private int         altImgResId = -1;
    private Drawable    imgDrawable;
    private Date        sortDate;
    private String      line2;
    private String      line2center;
    private String      line2right;
    private int         mPos;
    private boolean     mIcon1Visible;
    private boolean     mIcon2Visible;
    private boolean     mVisible;
    private boolean     mHighlight;
    private boolean     mUpdateAll;

    public TwoLinesDataHolder( String title, String line2, int modulId, int imgResId) {
        this.title = title;
        this.modulId = modulId;
        this.imgResId = imgResId;
        this.line2 = line2;
        this.line2center = "";
        this.line2right = "";
        sortDate = new Date();
    }

    @Override
    public void setVisible(boolean status) {
        mVisible = status;
    }

    @Override
    public boolean getVisible() {
        return mVisible;
    }

    public void setLine1( String text) {
        this.title = text;
    }

    public void setLine2( String text) {
        this.line2 = text;
    }

    public String getLine1() {
        return title;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2Center( String text) {
        line2center = text;
    }

    public String getLine2Center() {
        return line2center;
    }

    public void setLine2Right(String text) {
        line2right = text;
    }

    public String getLine2Right() {
        return line2right;
    }

    public void setIcon1Visible(boolean status) {
        mIcon1Visible = status;
    }

    public boolean getIcon1Visible() {
        return mIcon1Visible;
    }

    public void setIcon2Visible(boolean status) {
        mIcon2Visible = status;
    }

    public boolean getIcon2Visible() {
        return mIcon2Visible;
    }

    @Override
    public int getType() {
        return MainActivity.TWO_LINES_ITEM;
    }

    @Override
    public int getModulId() {
        return modulId;
    }

    public int getImgResId() {
        return imgResId;
    }

    @Override
    public void setPos(int pos) {
        mPos = pos;
    }

    public void setImgResId( int imgResId) {
        this.imgResId = imgResId;
    }

    //@Override
    //public int getPos() {
   //     return mPos;
    //}

    @Override
    public void setUpdateAll(boolean status) {
        mUpdateAll = status;
    }

    @Override
    public boolean getUpdateAll() {
        return mUpdateAll;
    }

    public void setHighlighted( boolean status) {
        mHighlight = status;
    }

    public void setAltImgResId( int resId) {
        altImgResId = resId;
    }

    public void setImgDrawable( Drawable img) {
        this.imgDrawable = img;
    }

    public boolean getHighlighted() {
        return mHighlight;
    }

    public int getAltImgResId() {
        return altImgResId;
    }

    public Drawable getImgDrawable() {
        return imgDrawable;
    }

    @Override
    public int compareTo(Object obj) {
        return -1;// (((DataHolder)obj).getSortDate().compareTo(sortDate));
    }
}
