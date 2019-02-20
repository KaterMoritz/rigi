package biz.kindler.rigi.modul.calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.TwoLinesDataHolder;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 29.11.16.
 */

public class CalendarModel extends BaseModel {

    public static final String          KEY_EVENT_LIST              = "event-list";

    private static final String[]       SCOPES = { CalendarScopes.CALENDAR_READONLY };
    public static final String          PREF_ACCOUNT_NAME           = "accountName";
    private static String               NOTCONFIGURED_INFO_LINE1    = "Kalender nicht konfiguriert";
    private static String               NOTCONFIGURED_INFO_LINE2    = "Click auf Panel zum einrichen";

    private TwoLinesDataHolder          mDataHolder;
    private GoogleAccountCredential     mCredential;
    private SimpleDateFormat            mTimeFormat = new SimpleDateFormat( "HH:mm");
    private List<Event>                 mAllEventsList;
    private int                         mTickCnt;

    public CalendarModel( Context ctx) {
        super(ctx, MainActivity.CALENDAR);

        mDataHolder = new CalendarDataHolder();
        //mDataHolder.setVisible( true);

        readCredentials();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction( MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.CALENDAR);
        intentFilter.addAction(MainActivity.ACTION_ACTIVITY_RESULT + MainActivity.CALENDAR);
        intentFilter.addAction(MainListAdapter.ACTION_CHANGED_IN_LIST_MODUL + MainActivity.CALENDAR);
        ctx.registerReceiver(this, intentFilter);
    }

    @Override
    protected void initItems() throws Exception {}

    public TwoLinesDataHolder getDataHolder() {
        return mDataHolder;
    }

    private void readCredentials() {
        mCredential = GoogleAccountCredential.usingOAuth2(getContext(), Arrays.asList(SCOPES)).setBackOff(new ExponentialBackOff());
        if( mCredential != null && mCredential.getSelectedAccountName() == null) {
            String accountName = getContext().getSharedPreferences( MainActivity.PREFS_ID, Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null)
                mCredential.setSelectedAccountName(accountName);
        }
    }

    public static boolean hasConfigured( Context ctx) {
        return ctx.getSharedPreferences( MainActivity.PREFS_ID, Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null) != null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIME_TICK)) {

            if (mTickCnt == 0) {
                if (hasConfigured(getContext()))
                    new MakeRequestTask(mCredential, false).execute();

            } else if (mTickCnt >= 15)
                mTickCnt = -1;

            if ((mCredential == null || mCredential.getSelectedAccountName() == null) && isModulInList()) {
                mDataHolder.setLine1(NOTCONFIGURED_INFO_LINE1);
                mDataHolder.setLine2(NOTCONFIGURED_INFO_LINE2);
                sendUpdateDataInListItemBroadcast();
            }

            mTickCnt++;
        }
        else if(action.equals( MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.CALENDAR)) {
            if( hasConfigured( getContext()))
                new MakeRequestTask(mCredential, true).execute();
            else{
                Intent calSetupIntent = new Intent("CalendarSetupIntent", null, getContext(), CalendarSetupActivity.class);
                ((Activity)getContext()).startActivityForResult(calSetupIntent, CalendarSetupActivity.RESULT_SUCCESFULL_SETUP);
            }
        }
        else if( action.equals(MainActivity.ACTION_ACTIVITY_RESULT + MainActivity.CALENDAR)) {
            mDataHolder.setLine2("aktualisiere...");
            sendUpdateDataInListItemBroadcast();
            readCredentials();
            new MakeRequestTask(mCredential, false).execute();
        }
        else if( action.equals( MainListAdapter.ACTION_CHANGED_IN_LIST_MODUL + MainActivity.CALENDAR)) {
           // if( hasConfigured( getContext()))
           //     new MakeRequestTask(mCredential, false).execute();
        }
    }

    private void showDetailActivity() {
        Intent calIntent = new Intent("CalendarDetailIntent", null, getContext(), CalendarDetailActivity.class);
        calIntent.putExtra( KEY_EVENT_LIST, (Serializable)mAllEventsList);
        getContext().startActivity(calIntent);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void sendUpdateDataInListItemBroadcast() {
        Intent bc = new Intent();
        bc.setAction(  MainListAdapter.ACTION_UPDATE_LISTITEM);
      //  bc.putExtra( MainListAdapter.KEY_POSITION, mDataHolder.getPos());
        bc.putExtra( MainListAdapter.KEY_MODUL_ID, MainActivity.CALENDAR);
        getContext().sendBroadcast(bc);
    }

    private void sendShowOrHideListItemBroadcast( boolean show) {
        Intent bc = new Intent();
        bc.setAction(  MainListAdapter.ACTION_UPDATE_LISTITEM);
        bc.putExtra( MainListAdapter.KEY_SHOW_IN_LIST, show);
        bc.putExtra( MainListAdapter.KEY_MODUL_ID, MainActivity.CALENDAR);
        getContext().sendBroadcast(bc);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<Event>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        private boolean mLoadLongList;

        public MakeRequestTask(GoogleAccountCredential credential , boolean loadLongList) {
            mLoadLongList = loadLongList;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential).setApplicationName("Rigi Google Calendar").build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<Event> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = new Exception(e.getCause());
                cancel(true);
                String cause = e.getCause().getMessage();
                if(cause.contains( "UNREGISTERED_ON_API_CONSOLE")) {  // UNREGISTERED_ON_API_CONSOLE
                    //storePrefsString(PREF_ACCOUNT_NAME, null);
                    SharedPreferences settings = getContext().getSharedPreferences(MainActivity.PREFS_ID, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(PREF_ACCOUNT_NAME, null);
                    editor.apply();
                }
                return null;
            }
        }

        /**
         * Fetch a list of the next 5 or 15 events from the primary calendar.
         * @return List of Events describing returned events.
         * @throws IOException
         */
        private List<Event> getDataFromApi() throws IOException {
            DateTime now = new DateTime(System.currentTimeMillis());

            try {
                Events events = mService.events().list("primary")
                        .setMaxResults(mLoadLongList ? 15 : 5)          // List the next 5 or 15 events from the primary calendar.
                        .setTimeMin(now)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute();
                return events.getItems();
            } catch (UserRecoverableAuthIOException recoverableException) {
                Intent calendarAuthIntent = recoverableException.getIntent();
                ((Activity)getContext()).stopLockTask();
                getContext().startActivity( calendarAuthIntent);
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            if( mLoadLongList) {
                mDataHolder.setLine2("laden...");
                sendUpdateDataInListItemBroadcast();
            }
        }

        @Override
        protected void onPostExecute(List<Event> eventList) {
            mAllEventsList = eventList;

            if (eventList == null || eventList.size() == 0) {
                mDataHolder.setLine1("Kalender");
                mDataHolder.setLine2("keine Einträge");
                sendUpdateDataInListItemBroadcast();
            } else {
                List<Event> listForToday = new ArrayList<Event>();
                List<Event> listForTomorrow = new ArrayList<Event>();
                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime( new Date());
                int dayOfYearToday = gc.get(Calendar.DAY_OF_YEAR);

                for (Event event : eventList) {
                    if( isEventForToday( event, dayOfYearToday))
                        listForToday.add( event);
                    if( isEventForTomorrow( event, (dayOfYearToday + 1)))
                        listForTomorrow.add( event);
                }
                updateDataForGUI( listForToday, listForTomorrow);
                if( mLoadLongList)
                    showDetailActivity();
            }
        }

        private boolean isEventForToday( Event event, int dayOfYearToday) {

            if( event.getStart() == null) // All-day events don't have start times, so just use the start date.
                return true;
            else {
                EventDateTime eventDateTime = event.getStart();
                DateTime start = eventDateTime.getDateTime() == null ? eventDateTime.getDate() : eventDateTime.getDateTime();
                if( start != null) {
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime(new Date(start.getValue()));
                    int eventDayOfYear = gc.get(Calendar.DAY_OF_YEAR);
                    return eventDayOfYear == dayOfYearToday;
                } else
                    return false;
            }
        }

        private boolean isEventForTomorrow( Event event, int dayOfYearTomorrow) {
            if( event.getStart() != null) {
                EventDateTime eventDateTime = event.getStart();
                DateTime start = eventDateTime.getDateTime() == null ? eventDateTime.getDate() : eventDateTime.getDateTime();
                if( start != null) {
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime(new Date(start.getValue()));
                    int eventDayOfYear = gc.get(Calendar.DAY_OF_YEAR);
                    return eventDayOfYear == dayOfYearTomorrow;
                } else
                    return false;
            }
            return false;
        }

        private void updateDataForGUI( List<Event> listForToday, List<Event> listForTomorrow) {
            boolean hasEntriesForToday = listForToday.size() > 0;
            boolean hasEntriesForTomorrow = listForTomorrow.size() > 0;

            if( ! hasEntriesForToday) {
                mDataHolder.setLine1("Keine Einträge für heute");
                mDataHolder.setImgResId( R.drawable.calendar);
            }
            else {
                mDataHolder.setLine1( getEventTextForAll( listForToday));
                int imgId = getImageId( listForToday);
                mDataHolder.setImgResId( imgId);
            }
            if( ! hasEntriesForTomorrow)
                mDataHolder.setLine2("Keine Einträge für morgen");
            else
                mDataHolder.setLine2( "Morgen: " + getEventTextForAll( listForTomorrow));

            sendShowOrHideListItemBroadcast( hasEntriesForToday || hasEntriesForTomorrow);

            // only sort when entries for today. when no entries for today and tomorrow then remove modul from list
            //doSendUpdateModul( hasEntriesForToday, ! hasEntriesForToday && hasEntriesForTomorrow);
            sendUpdateDataInListItemBroadcast();
        }

        private String getEventTextForAll( List<Event> eventList) {
            StringBuffer buff = new StringBuffer();
            for (Event event : eventList) {
                buff.append( getEventText( event));
                buff.append( ", ");
            }
            String text = buff.toString();
            return text.substring( 0, text.length() -2);
        }

        private String getEventText( Event event) {
            DateTime dt = event.getStart().getDateTime();
            String time = null;
            if( dt != null)
                time = mTimeFormat.format( new Date( dt.getValue()));
            return event.getSummary() + (time == null ? "" : " " + time);
        }

        private int getImageId( List<Event> eventList) {
            for (Event event : eventList) {
                String txt = event.getSummary().toLowerCase();
                if(txt.startsWith( "abfall"))
                    return R.drawable.abfall;
                else if(txt.contains( "grünabfall"))
                    return R.drawable.gruenabfall;
                else if(txt.contains( "papier"))
                    return R.drawable.papier;
                else if(txt.contains( "karton"))
                    return R.drawable.karton;
                else if(txt.contains( "geburtstag"))
                    return R.drawable.geburtstag;
                else if(txt.contains( "feiertag"))
                    return R.drawable.feiertag;
                else if(txt.contains( "zahnarzt"))
                    return R.drawable.zahnarzt;
                else if(txt.contains( "tierarzt"))
                    return R.drawable.tierarzt;
            }
            return R.drawable.calendar;
        }

        /*
        @Override
        protected void onPostExecute(List<String> output) {

            if (output == null || output.size() == 0) {
                mDataHolder.setLine2("No results returned.");
            } else {
                //output.add(0, "Data retrieved using the Google Calendar API:");
                //mOutputText.setText(TextUtils.join("\n", output));
                mDataHolder.setLine2(output.get(0));
            }
            doSendUpdateModul();
        } */

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    int connStatusCode = ((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode();
                    mDataHolder.setLine2("GooglePlayServices not available [connection error:" + connStatusCode + "]");
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    mDataHolder.setLine1(NOTCONFIGURED_INFO_LINE1);
                    mDataHolder.setLine2(NOTCONFIGURED_INFO_LINE2);
                } else {
                    mDataHolder.setLine2( "Fehler: " + mLastError.getMessage());
                }
            } else {
                mDataHolder.setLine2( "Request cancelled.");
            }
            //doSendUpdateModul(false, true);
            sendUpdateDataInListItemBroadcast();
        }
    }


}
