package biz.kindler.rigi.modul.clock;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 27.11.16.
 */

public class TimeAndDateView {

    private TextView            mTime;
    private TextView            mDate;
    private TextView            mTomorrowDayTxt;
    private TextView            mAfterTomorrowDayTxt;
    private SimpleDateFormat    mTimeFormatter;
    private SimpleDateFormat    mDateFormatter;
    private SimpleDateFormat    mWeekdayFormatter;


    public TimeAndDateView( Context ctx) {
        // Time
        mTime = (TextView)((Activity)ctx).findViewById(R.id.time);
        mTimeFormatter = new SimpleDateFormat("HH:mm");
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        // Date
        mDate = (TextView)((Activity)ctx).findViewById(R.id.date);
        mDateFormatter = new SimpleDateFormat("EEEE d MMMM", Locale.GERMAN);
        mDateFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        // tomorrow day text
        mWeekdayFormatter = new SimpleDateFormat("EE", Locale.GERMAN);
        mTomorrowDayTxt = (TextView)((Activity)ctx).findViewById(R.id.weather_tomorrow_day_text);
        // after tomorrow day text
        mAfterTomorrowDayTxt = (TextView)((Activity)ctx).findViewById(R.id.weather_aftertomorrow_day_text);
    }

    public void setTime( Date now) {
        mTime.setText(mTimeFormatter.format(now));
    }

    public void setDate( Date date) {
        mDate.setText(mDateFormatter.format(date));
    }

    public void setTomorrowDay( Date date) {
        mTomorrowDayTxt.setText(mWeekdayFormatter.format(date).replace(".", ""));
    }

    public void setAfterTomorrowDay( Date date) {
        mAfterTomorrowDayTxt.setText(mWeekdayFormatter.format(date).replace(".", ""));
    }


}
