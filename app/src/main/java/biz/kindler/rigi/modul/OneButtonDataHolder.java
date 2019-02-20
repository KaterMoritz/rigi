package biz.kindler.rigi.modul;

import android.graphics.drawable.Drawable;

import java.io.Serializable;
import java.util.Date;

import biz.kindler.rigi.DataHolder;
import biz.kindler.rigi.MainActivity;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 21.10.16.
 */

public class OneButtonDataHolder implements DataHolder, Serializable {

    private String      title;
    private int         modulId;
    private int         imgResId;
    private int         altImgResId = -1;
    private Drawable    imgDrawable;
    private Date        sortDate;
    private int         mPos;
    private boolean     mVisible;
    private boolean     mHighlight;
    private boolean     mUpdateAll;

    private String  info;
    private String  buttonInfo;
    private String  buttonText;


    public OneButtonDataHolder( String title, int modulId, int imgResId) {
        this.title = title;
        this.modulId = modulId;
        this.imgResId = imgResId;
        info = "";
        buttonInfo = "";
        buttonText = "";
        sortDate = new Date();
    }

    @Override
    public int getType() {
        return MainActivity.ONE_BUTTON_ITEM;
    }

    @Override
    public int getModulId() {
        return modulId;
    }

    @Override
    public void setPos(int pos) {
        mPos = pos;
    }

    @Override
    public void setVisible(boolean status) {
        mVisible = status;
    }

    @Override
    public boolean getVisible() {
        return mVisible;
    }

    //@Override
    //public int getPos() {
      //  return mPos;
   // }

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

    public void setImgResId( int imgResId) {
        this.imgResId = imgResId;
    }

    public void setImgDrawable( Drawable img) {
        this.imgDrawable = img;
    }

    public void setAltImgResId( int resId) {
        altImgResId = resId;
    }

    public boolean getHighlighted() {
        return mHighlight;
    }

    public int getAltImgResId() {
        return altImgResId;
    }

    public void setTitle( String text) {
        title = text;
    }

    public void setInfo( String text) {
        info = text;
    }

    public void setButtonInfo( String text) {
        buttonInfo = text;
    }

    public void setButtonText( String text) {
        buttonText = text;
    }

    public int getImgResId() {
        return imgResId;
    }

    public Drawable getImgDrawable() {
        return imgDrawable;
    }

    public String getTitle() {
        return title;
    }

    public String getInfo() {
        return info;
    }

    public String getButtonInfo() {
        return buttonInfo;
    }

    public String getButtonText() {
        return buttonText;
    }

    @Override
    public int compareTo(Object obj) {
        //return (((DataHolder)obj).getSortDate().compareTo(sortDate));
        return -1;
    }
}
