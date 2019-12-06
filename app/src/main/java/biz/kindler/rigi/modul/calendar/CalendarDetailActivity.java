package biz.kindler.rigi.modul.calendar;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;

/*
* Created by patrick kindler (katermoritz100@gmail.com)
*/

public class CalendarDetailActivity extends AppCompatActivity {


    private static final String KEY_EVENT_SUMMARY           = "summary";
    private static final String KEY_EVENT_START             = "start";
    private static final String KEY_EVENT_ONLY_DATE         = "date";
    private static final String KEY_EVENT_DATE_AND_TIME     = "dateTime";

    private static final String ONLY_DATE_FORMAT            = "EEE d MMMM";
    private static final String DATE_AND_TIME_FORMAT        = "HH:mm  EEE d MMMM";

    private ListView            mListView;
    private ListAdapter         mListAdapter;
    private SimpleDateFormat    mOnlyDateFormat;
    private SimpleDateFormat    mDateAndTimeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_detail);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle( "Rigi Kalender");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        mListView = (ListView) findViewById(R.id.calendarlist);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(CalendarModel.KEY_EVENT_LIST)) {
                ArrayList<HashMap> eventList = (ArrayList<HashMap>) extras.getSerializable(CalendarModel.KEY_EVENT_LIST);
                mListAdapter = new ListAdapter(eventList);
            }
        }

        if( mListAdapter != null)
            mListView.setAdapter(mListAdapter);

        mOnlyDateFormat = new SimpleDateFormat(ONLY_DATE_FORMAT, Locale.GERMAN);
        mDateAndTimeFormat = new SimpleDateFormat(DATE_AND_TIME_FORMAT, Locale.GERMAN);
    }

    private Context getContext() {
        return this;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    private class ListAdapter extends BaseAdapter {
        private ArrayList<HashMap> eventList;

        public ListAdapter( ArrayList<HashMap> eventList) {
            this.eventList = eventList;
        }

        @Override
        public int getCount() {
            return eventList.size();
        }

        @Override
        public Object getItem(int position) {
            return eventList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout layout;

            String dateTimeString = "";
            HashMap currHMap = eventList.get(position);
            Object summary = currHMap.get( KEY_EVENT_SUMMARY);
            HashMap startHMap = (HashMap)currHMap.get( KEY_EVENT_START);

            Object onlyDateObj = startHMap.get( KEY_EVENT_ONLY_DATE); // Only Date
            if( onlyDateObj != null && onlyDateObj instanceof DateTime) {
                DateTime onlyDate = (DateTime)onlyDateObj;
                dateTimeString = mOnlyDateFormat.format(new Date(onlyDate.getValue()));
            }
            Object dateAndTimeObj = startHMap.get( KEY_EVENT_DATE_AND_TIME); // Date and time
            if( dateAndTimeObj != null && dateAndTimeObj instanceof DateTime) {
                DateTime dateAndTime = (DateTime)dateAndTimeObj;
                dateTimeString = mDateAndTimeFormat.format(new Date(dateAndTime.getValue()));
            }

            if (convertView == null)
                layout = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.calendar_list_item, parent, false);
            else
                layout = (RelativeLayout) convertView;

            TextView eventSummary = (TextView) layout.findViewById(R.id.eventSummary);
            eventSummary.setText( summary.toString());

            TextView eventDateTime = (TextView) layout.findViewById(R.id.eventDateTime);
            eventDateTime.setText( dateTimeString);


            return layout;
        }
    }
}
