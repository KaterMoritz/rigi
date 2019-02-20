package biz.kindler.rigi.modul.vbl;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 11.11.16.
 */

import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class Connection2 implements Serializable {

    public static final int     BUS = 1;
    public static final int     TRAIN = 2;

    private Date departureDate, arrivalDate, updatedDate;
    private int transportType;
    private String delay;


    public static SimpleDateFormat 	clientDateFormat = new SimpleDateFormat( "HH:mm");

    public Connection2(JSONObject jsonObj) throws Exception {
        String departureTimeMS = jsonObj.getJSONObject( "from").getString( "time");
        String departureLocationName = jsonObj.getJSONObject( "from").getJSONObject( "location").getString( "name");
        String arrivalTimeMs = jsonObj.getJSONObject( "to").getString( "time");
        delay = jsonObj.getJSONObject( "from").getString( "delay");
        departureDate = new Date(Long.parseLong( departureTimeMS));
        arrivalDate = new Date(Long.parseLong( arrivalTimeMs));
        updatedDate = new Date();

        // todo better solution or read name from properties
        if( departureLocationName.equals( "Buchrain, Dorf"))
            transportType = BUS;
        else
            transportType = TRAIN;
    }

    public String getDelay( String prefix, boolean hideZeroDelay) {
        if( hideZeroDelay)
            return delay.equals( "0") ? "" : prefix + delay;
        else
            return prefix + delay;
    }

    public int getTransportType() {
        return transportType;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public String getDepartureClientFormat() {
        return clientDateFormat.format( departureDate);
    }

    public String getArrivalClientFormat() {
        return clientDateFormat.format( arrivalDate);
    }

    public String getUpdatedClientFormat() {
        return clientDateFormat.format( updatedDate);
    }

    public boolean isDepartureLessThan( int minutes) {
        GregorianCalendar gCalDep = new GregorianCalendar();
        gCalDep.setTime( departureDate);

        GregorianCalendar gCalNow = new GregorianCalendar();
        gCalNow.add( Calendar.MINUTE, minutes);

        return gCalNow.after( gCalDep);
    }

    public int getDepartureInMinutes() {
        GregorianCalendar gCal = new GregorianCalendar();
        if( gCal.get( Calendar.SECOND) > 30)
            gCal.add( Calendar.MINUTE, 1);

       // long difference = departureDate.getTime() - Calendar.getInstance().getTimeInMillis();
        long difference = departureDate.getTime() - gCal.getTimeInMillis();
       // Long l = new Long( difference/60000);
       // return l.intValue();
        //
        Long l = roundUp( difference, 60000);
        return l.intValue();
        // return l.intValue();
    }

    public static long roundUp(long num, long divisor) {
        return (num + divisor - 1) / divisor;
    }

    public String toString() {
        return "departure:" + getDepartureClientFormat() + ", arrival:" + getArrivalClientFormat() + ", departure in:" + getDepartureInMinutes() + ", updated:" + getUpdatedClientFormat();
    }

}
