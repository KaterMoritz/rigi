package biz.kindler.rigi.modul;

import android.graphics.drawable.Drawable;

import java.util.Date;

import biz.kindler.rigi.DataHolder;
import biz.kindler.rigi.MainActivity;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 21.10.16.
 */

public class TwoButtonDataHolder implements DataHolder {

    public static int       B1      = 0; // Doppelbutton links
    public static int       B11     = 1; //
    public static int       B12     = 2;
    public static int       B2      = 3; // Doppelbutton rechts
    public static int       B21     = 4; //
    public static int       B22     = 5;


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

    private String      info;
    private String      button1Info;
    private String      button2Info;
    private boolean[]   buttonVisible = new boolean[6];
    private String[]    buttonText = new String[6];

    public TwoButtonDataHolder( String title, int modulId, int imgResId) {
        this.title = title;
        this.modulId = modulId;
        this.imgResId = imgResId;
        info = "";
        button1Info = "";
        button2Info = "";
        buttonText[B1] = "";
        buttonText[B2] = "";
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

    public void setButtonVisible( int idx, boolean status) {
        buttonVisible[idx] = status;
    }

    public boolean getButtonVisible( int idx) {
        return buttonVisible[idx];
    }

    @Override
    public int getType() {
        return MainActivity.TWO_BUTTON_ITEM;
    }

    @Override
    public int getModulId() {
        return modulId;
    }

    @Override
    public void setPos(int pos) {
        mPos = pos;
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


    public void setTitle( String text) {
        title = text;
    }

    public void setInfo( String text) {
        info = text;
    }

    public void setButton1Info( String text) {
        button1Info = text;
    }

    public void setButtonText( int idx, String text) {
        buttonText[idx] = text;
    }

    public void setButton2Info( String text) {
        button2Info = text;
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

    public String getButton1Info() {
        return button1Info;
    }

    public String getButtonText( int idx) {
        return buttonText[idx];
    }

    public String getButton2Info() {
        return button2Info;
    }

    @Override
    public int compareTo(Object obj) {
        return -1;// (((DataHolder)obj).getSortDate().compareTo(sortDate));
    }
}
