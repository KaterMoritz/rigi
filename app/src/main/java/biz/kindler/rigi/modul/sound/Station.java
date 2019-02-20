package biz.kindler.rigi.modul.sound;

import java.io.Serializable;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 25.08.17.
 */

// see: http://wiki.shoutcast.com/wiki/SHOUTcast_Radio_Directory_API
public class Station implements Serializable {

    public static final String  NAME    = "name";
    public static final String  ID      = "id";
    public static final String  GENRE   = "genre";
    public static final String  LOGO    = "logo";

    private String  mName;
    private String  mMt;        // Media Type
    private String  mId;
    private String  mBr;        // Bitrate
    private String  mGenre;
    private String  mGenre2;
    private String  mGenre3;
    private String  mGenre4;
    private String  mGenre5;
    private String  mLogo;
    private String  mCt;
    private String  mLc;
    private boolean mFav;
    private boolean mSelected;

    public void setName( String name) {
        mName = name;
    }

    public void setMt( String mt) {
        mMt = mt;
    }

    public void setId( String id) {
        mId = id;
    }

    public void setBr( String br) {
        mBr = br;
    }

    public void setGenre( String genre) {
        mGenre = genre;
    }

    public void setGenre2( String genre) {
        mGenre2 = genre;
    }

    public void setGenre3( String genre) {
        mGenre3 = genre;
    }

    public void setGenre4( String genre) {
        mGenre4 = genre;
    }

    public void setGenre5( String genre) {
        mGenre5 = genre;
    }

    public void setLogo( String logo) {
        mLogo = logo;
    }

    public void setCt( String ct) {
        mCt = ct;
    }

    public void setLc( String lc) {
        mLc = lc;
    }

    public void setFavorite( boolean status) {
        mFav = status;
    }

    public void setSelected( boolean status) {
        mSelected = status;
    }
    ////////////////////////////////////////////////////////////////////
    public String getName() {
        return mName;
    }

    public String getMt() {
        return mMt;
    }

    public String getId() {
        return mId;
    }

    public long getIdAsLong() {
        return Long.parseLong( mId);
    }

    public String getBr() {
        return mBr;
    }

    public String getGenre() {
        return mGenre;
    }

    public String getGenre2() {
        return mGenre2;
    }

    public String getGenre3() {
        return mGenre3;
    }

    public String getGenre4() {
        return mGenre4;
    }

    public String getGenre5() {
        return mGenre5;
    }

    public String getLogo() {
        return mLogo;
    }

    public String getCt() {
        return mCt;
    }

    public String getLc() {
        return mLc;
    }

    public boolean getFavorite() {
        return mFav;
    }

    public boolean getSelected() {
        return mSelected;
    }
}
