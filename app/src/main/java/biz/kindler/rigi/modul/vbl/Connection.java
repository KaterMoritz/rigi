package biz.kindler.rigi.modul.vbl;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 11.11.16.
 */
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.JSONObject;


public class Connection implements Serializable {

    private Date departureDate, arrivalDate, updatedDate;
    public static SimpleDateFormat 	serverDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ");
    public static SimpleDateFormat 	clientDateFormat = new SimpleDateFormat( "HH:mm");

    public Connection( JSONObject jsonObj) throws Exception {
        String departureString = jsonObj.getJSONObject( "from").getString( "departure");
        String arrivalString = jsonObj.getJSONObject( "to").getString( "arrival");
        departureDate = serverDateFormat.parse( departureString);
        arrivalDate = serverDateFormat.parse( arrivalString);
        updatedDate = new Date();
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
