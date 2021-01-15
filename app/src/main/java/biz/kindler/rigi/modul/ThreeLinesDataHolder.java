package biz.kindler.rigi.modul;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.Date;

import biz.kindler.rigi.DataHolder;
import biz.kindler.rigi.MainActivity;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 31.10.16.
 */

public class ThreeLinesDataHolder implements DataHolder, Serializable {


    private String      title;
    private String[]    line1;
    private String[]    line2;
    private String[]    line3;
    private int         modulId;
    private int         imgResId;
    private int         altImgResId = -1;
    private Drawable    imgDrawable;
    private Date        sortDate;
    private int         mPos;
    private boolean     mVisible;
    private boolean     mHighlight;
    private boolean     mUpdateAll;

    public ThreeLinesDataHolder( String title, String[] line1, String[] line2, String[] line3, int modulId, int imgResId) {
        this.title = title;
        this.line1 = line1;
        this.line2 = line2;
        this.line3 = line3;
        this.modulId = modulId;
        this.imgResId = imgResId;
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

    @Override
    public int getType() {
        return MainActivity.THREE_LINES_ITEM;
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

    //@Override
   // public int getPos() {
    //    return mPos;
   // }

    @Override
    public void setUpdateAll(boolean status) {
        mUpdateAll = status;
    }

    @Override
    public boolean getUpdateAll() {
        return mUpdateAll;
    }

    public void setImgResId( int imgResId) {
        this.imgResId = imgResId;
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

    public String getTitle() {
        return title;
    }

    public String[] getLine1() {
        return line1;
    }

    public String[] getLine2() {
        return line2;
    }

    public String[] getLine3() {
        return line3;
    }

    public void setTitle( String txt) {
        title = txt;
    }

    public void setLine1( String[] txt) {
        line1 = txt;
    }

    public void setLine2( String[] txt) {
        line2 = txt;
    }

    public void setLine3( String[] txt) {
        line3 = txt;
    }

    @Override
    public int compareTo(Object obj) {
        return -1;// (((DataHolder)obj).getSortDate().compareTo(sortDate));
    }
}
