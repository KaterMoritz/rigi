package biz.kindler.rigi.modul.system;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 02.12.16.
 */

public class Event implements Serializable, Comparable<Event> {

    public static final int     EXCEPTION   = 1;
    public static final int     LOG         = 2;

    private Date                mTimestamp;
    private String              mClassName;
    private String              mObjName;
    private String              mMessage;
    private int                 mType;


    public Event() {
        mTimestamp = new Date();
    }

    public Event( String className, String objName, String message, int type) {
        this();
        mClassName = className;
        mObjName = objName;
        mMessage = message;
        mType = type;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getClassName() {
        return mClassName;
    }

    public String getObjName() {
        return mObjName;
    }

    public int getType() {
        return mType;
    }

    public Date getTimestamp() {
        return mTimestamp;
    }

    @Override
    public int compareTo(Event another) {
        // return getTimestamp().compareTo(another.getTimestamp());
        return another.getTimestamp().compareTo(getTimestamp());
    }
}
