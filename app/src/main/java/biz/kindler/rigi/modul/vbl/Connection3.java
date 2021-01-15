package biz.kindler.rigi.modul.vbl;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Patrick Kindler - Switzerland
 * 14.01.21
 */
class Connection3 implements Serializable {
    private final static String TAG = Connection3.class.getSimpleName();

    private Date departureDate, arrivalDate, updatedDate;
    private String transportType, transportLine;
    private String destinationText, startText;
    private int delayMinutes = 0;
    private boolean departureDelayed; // departure on time or delayed
    private boolean isDirectConnection; // kein Umsteigen

    public static SimpleDateFormat clientDateFormat = new SimpleDateFormat( "HH:mm", Locale.getDefault());
    public static SimpleDateFormat serviceDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    public Connection3(JSONObject tripJSONObj) throws Exception {

        String startTimeString = tripJSONObj.optString( "trias:StartTime");
        OffsetDateTime odtStart = OffsetDateTime.parse( startTimeString);
        departureDate = java.util.Date.from( odtStart.toInstant()); // Instant is always in UTC.

        String endTimeString = tripJSONObj.optString( "trias:EndTime");
        OffsetDateTime odtEnd = OffsetDateTime.parse( endTimeString);
        arrivalDate = java.util.Date.from( odtEnd.toInstant()); // Instant is always in UTC.

        isDirectConnection = tripJSONObj.optString( "trias:Interchanges").equals("0");

        updatedDate = new Date();

        try {
            JSONObject timedLegObj = tripJSONObj.getJSONObject("trias:TripLeg").getJSONObject("trias:TimedLeg");

            JSONObject serviceObj = timedLegObj.getJSONObject("trias:Service");
            transportType = serviceObj.getJSONObject("trias:Mode").getJSONObject("trias:Name").optString("trias:Text");
            transportLine = serviceObj.getJSONObject("trias:PublishedLineName").optString("trias:Text");
           // destinationText = serviceObj.getJSONObject("trias:DestinationText").getString("trias:Text");

            JSONObject legBoardObj = timedLegObj.getJSONObject("trias:LegBoard");

            startText = legBoardObj.getJSONObject("trias:StopPointName").optString("trias:Text");
            destinationText = timedLegObj.getJSONObject("trias:LegAlight").getJSONObject("trias:StopPointName").optString("trias:Text");

            try {
                JSONObject serviceDepartureObj = legBoardObj.getJSONObject("trias:ServiceDeparture");
                String timetabledTime = serviceDepartureObj.optString("trias:TimetabledTime");
                String estimatedTime = serviceDepartureObj.optString("trias:EstimatedTime");
                departureDelayed = !estimatedTime.equals(timetabledTime);
                if( departureDelayed) {
                    delayMinutes = calculateDepartureDelay(timetabledTime, estimatedTime);
                }
            } catch(Exception ex) {
                Log.w(TAG, "no EstimatedTime or TimetabledTime (" + ex.getMessage() + ")");
            }

        } catch(Exception ex) {
            Log.w(TAG, ex.getMessage());
        }

        Log.d(TAG, "departureDate: " + departureDate.toString() + "," + transportType + "," + transportLine + "," + startText + "," + destinationText + ",departureDelayed:" + departureDelayed);
    }

    private int calculateDepartureDelay(String timetabledTime, String estimatedTime) throws Exception {
        OffsetDateTime timetabledODT = OffsetDateTime.parse( timetabledTime);
        OffsetDateTime estimatedODT = OffsetDateTime.parse( estimatedTime);
        return estimatedODT.getMinute() - timetabledODT.getMinute();
    }

    public String getDelay() {
        return departureDelayed ? "+" + delayMinutes : "";
    }

    public boolean isDepartureDelayed() {
        return departureDelayed;
    }

    public String getTransportType() {
        return transportType;
    }

    public String getTransportLine() {
        return transportLine;
    }

    public boolean isDirectConnection() {
        return isDirectConnection;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public String getDepartureStation() {
        return startText;//replaceFirst(",", "").replaceFirst("Bahnhof","Bhf");
    }

    public String getArrivalStation() {
        return destinationText;//.replaceFirst(",", "").replaceFirst("Bahnhof","Bhf");
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
