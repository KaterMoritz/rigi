package biz.kindler.rigi.modul.system;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.acra.ACRA;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Timer;

import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.TwoLinesDataHolder;
import biz.kindler.rigi.modul.clock.TimeAndDateModel;
import biz.kindler.rigi.settings.LogPreferenceFragment;


/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 01.12.16.
 */

public class SystemModel extends BaseModel {

    private final static String TAG            = SystemModel.class.getSimpleName();


    public static final String ACTION_EXCEPTION = "action-exception";
    public static final String ACTION_LOG = "action-log";
    public static final String ACTION_DAY_OR_NIGHT = "action-day-or-night";
    public static final String KEY_CLASS = "system-classname";
    public static final String KEY_OBJECT = "system-objectname";
    public static final String KEY_MESSAGE = "system-message";
    public static final String KEY_EVENT_LIST = "event-list";

    private static final int   KEEP_LOGFILES_CNT = 10;

    private TwoLinesDataHolder mDataHolder;
    private ArrayList<Event> mEventList;
    private Timer mWaitBeforeSendMailTimer;
    private String mSSEInfo;
    private boolean    mHasInit;

    public SystemModel(Context ctx) {
        super(ctx, MainActivity.SYSTEM);

        mDataHolder = new SystemDataHolder();

        mEventList = new ArrayList<>();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_EXCEPTION);
        intentFilter.addAction(ACTION_LOG);
        intentFilter.addAction(MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.SYSTEM);
        intentFilter.addAction(TimeAndDateModel.ACTION_NEW_DAY);
        intentFilter.addAction(LogPreferenceFragment.SEND_LOG_NOW);
        intentFilter.addAction(ItemManager2.ACTION_SYSTEM);
        ctx.registerReceiver(this, intentFilter);
    }

    @Override
    protected void initItems() throws Exception {
        mHasInit = true;
    }

    public TwoLinesDataHolder getDataHolder() {
        return mDataHolder;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ACTION_EXCEPTION) || action.equals(ACTION_LOG))
            handleExceptionOrLog(intent, action.equals(ACTION_EXCEPTION) ? Event.EXCEPTION : Event.LOG);
        else if (action.equals(MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.SYSTEM))
            handleOnSystemPanelClicked();
        else if (action.equals(TimeAndDateModel.ACTION_NEW_DAY)) {
            if( ! intent.getBooleanExtra(TimeAndDateModel.KEY_APP_START_TODAY, false))
                handleNewDay();
        }
        else if(action.equals(LogPreferenceFragment.SEND_LOG_NOW)) {
            if( intent.getStringExtra(KEY_MESSAGE).contains("tracepot"))
                handleSendTestException();
            else
                handleSendLogFileNow( intent.getStringExtra(KEY_OBJECT));
        }
        else if(action.equals(ItemManager2.ACTION_SYSTEM)) {
            String state = intent.getStringExtra( ItemManager2.SSE_STATE);
            String info = intent.getStringExtra( ItemManager2.SSE_INFO);
            handleItemManagerAction( state, info);
        }
    }

    // Exception or log
    private void handleExceptionOrLog(Intent intent, int eventType) {
        if (mEventList.size() > 100)
            mEventList.remove(0);

        mEventList.add(new Event(intent.getStringExtra(KEY_CLASS),
                intent.getStringExtra(KEY_OBJECT),
                intent.getStringExtra(KEY_MESSAGE),
                eventType));

        if( mHasInit)
            updateInfoText(false);
    }

    private void handleOnSystemPanelClicked() {
        Intent systemDetailIntent = new Intent("SystemDetailActivity", null, getContext(), SystemDetailActivity.class);
        systemDetailIntent.putExtra(KEY_EVENT_LIST, mEventList);
        getContext().startActivity(systemDetailIntent);
    }

    private void handleNewDay() {
        int dayOfYearToday = new GregorianCalendar().get(java.util.Calendar.DAY_OF_YEAR);
        int dayOfYearYesterday = dayOfYearToday -1;
        biz.kindler.rigi.modul.system.Log.setDayOfYear( dayOfYearToday);

        boolean sendDailyLog = getContext().getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE).getBoolean(LogPreferenceFragment.SEND_LOG_DAILY, false);
        if( sendDailyLog)
            new SendMailTask(getContext()).execute( Log.getFileName(dayOfYearYesterday));

        try {
            String status = Log.deleteOldLogfiles( KEEP_LOGFILES_CNT);
            Log.i(TAG, "deleteOldLogfiles [" + KEEP_LOGFILES_CNT + "] status: " + status);
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }
    }

    private void handleSendTestException() {
        try {
            Exception ex = new Exception("send test exception to cloud");
            ACRA.getErrorReporter().handleSilentException(ex);
            mEventList.add(new Event(getClass().getSimpleName(), "ACRA", "test exception, check tracepot.com", Event.LOG));
        } catch (Exception ex2) {
            mEventList.add(new Event(getClass().getSimpleName(), "ACRA", "test exception:" + ex2.getMessage(), Event.EXCEPTION));
            Log.w(TAG, ex2.getMessage());
        }
        sendUpdateListItemBroadcast();
    }

    private void handleSendLogFileNow( String filename) {
        new SendMailTask(getContext()).execute( filename);
    }

    private void updateInfoText( boolean showInList) {
        mDataHolder.setLine2( mSSEInfo == null ? "" : mSSEInfo + " [" + mEventList.size() + " Einträge]");
        sendUpdateListItemBroadcast(showInList);
    }

    private void handleItemManagerAction( String state, String info) {
        if( state.equals(ItemManager2.SSE_CONNECTED)) {
            if( isModulInList())
                startTimerForHideModul( 15);
            mDataHolder.setHighlighted(false);
            mSSEInfo = "SSE connected";
        }
        else if( state.equals(ItemManager2.SSE_DISCONNECTED)) {
            mDataHolder.setHighlighted(true);
            mSSEInfo = "SSE disconnected [" + info + "]";
        }
        updateInfoText( true);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

}