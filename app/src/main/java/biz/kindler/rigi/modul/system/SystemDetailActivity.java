package biz.kindler.rigi.modul.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.calendar.CalendarModel;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 02.12.16.
 */

public class SystemDetailActivity extends AppCompatActivity {


    private ListView mListView;
    private ListAdapter mListAdapter;
    private MyBroadcastReceiver mMyBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_detail);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        ActionBar actionBar = getSupportActionBar();
        if( actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("System Ereignisse");
            actionBar.setDisplayShowHomeEnabled(true);
        }

        mListView = (ListView) findViewById(R.id.systemlist);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(CalendarModel.KEY_EVENT_LIST)) {
                ArrayList<Event> eventList = (ArrayList<Event>) extras.getSerializable(SystemModel.KEY_EVENT_LIST);
                mListAdapter = new ListAdapter(eventList);
            }
        }

        if( mListAdapter != null)
            mListView.setAdapter(mListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        mMyBroadcastReceiver = new MyBroadcastReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SystemModel.ACTION_EXCEPTION);
        intentFilter.addAction(SystemModel.ACTION_LOG);
        registerReceiver( mMyBroadcastReceiver, intentFilter);

        Collections.sort( mListAdapter.getData());

        // getSupportActionBar().hide();
       // hideSystemUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        if( mMyBroadcastReceiver != null)
            unregisterReceiver( mMyBroadcastReceiver);
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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
        private ArrayList<Event>    eventList;
        private SimpleDateFormat    mTimeFormatter, mDateFormatter;

        public ListAdapter( ArrayList<Event> eventList) {
            this.eventList = eventList;
            mTimeFormatter = new SimpleDateFormat("HH:mm:ss");
            mTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

            mDateFormatter = new SimpleDateFormat("dd.MM.");
            mDateFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        }

        public ArrayList<Event> getData() {
            return eventList;
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

            Event currEvent = eventList.get(position);
            Date timestamp = currEvent.getTimestamp();
            int timestampDay = timestamp.getDay();
            int todayDay = new Date().getDay();

            if (convertView == null)
                layout = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.system_list_item, parent, false);
            else
                layout = (RelativeLayout) convertView;

            ImageView imgView = (ImageView)layout.findViewById(R.id.typeImg);
            imgView.setImageResource( currEvent.getType() == Event.EXCEPTION ? R.drawable.ic_bug_report_white_36dp : R.drawable.ic_adb_white_36dp);

            String classTxt = currEvent.getClassName() == null ? "" : currEvent.getClassName();
            String objTxt = currEvent.getObjName() == null ? "" : currEvent.getObjName();
            TextView firstLine = (TextView) layout.findViewById(R.id.eventFirstLine);
            firstLine.setText( classTxt.replace( "biz.kindler.rigi", "") + "  |  " + objTxt);

            TextView secondLine = (TextView) layout.findViewById(R.id.eventSecondLine);
            secondLine.setText( currEvent.getMessage());

            TextView eventTime = (TextView) layout.findViewById(R.id.eventTime);
            eventTime.setText( mTimeFormatter.format( timestamp));

            TextView eventDate = (TextView) layout.findViewById(R.id.eventDate);
            eventDate.setText( timestampDay == todayDay ? "Heute" : mDateFormatter.format( timestamp));

            return layout;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    private class MyBroadcastReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(SystemModel.ACTION_EXCEPTION) || action.equals(SystemModel.ACTION_LOG)) {
                mListAdapter.eventList.add(new Event(intent.getStringExtra(SystemModel.KEY_CLASS),
                        intent.getStringExtra(SystemModel.KEY_OBJECT),
                        intent.getStringExtra(SystemModel.KEY_MESSAGE),
                        action.equals(SystemModel.ACTION_EXCEPTION) ? Event.EXCEPTION : Event.LOG));
                Collections.sort( mListAdapter.getData());
                mListAdapter.notifyDataSetChanged();
            }
        }
    }
}

