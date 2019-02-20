package biz.kindler.rigi.modul;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.ItemManager;
import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 21.10.16.
 */

public abstract class BaseModel extends BroadcastReceiver {

    private final static String TAG = BaseModel.class.getSimpleName();

    private SharedPreferences   mPrefs;
    private Context             mCtx;
    private SimpleDateFormat    mDateTimeFormatter;
    private Timer               mHideModulTimer;
    private boolean             mHideModulTimerScheduled;
    private int                 mModulId;

    public BaseModel( Context ctx, int modulId) {
        mCtx = ctx;
        mModulId = modulId;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ItemManager.ACTION_HABITEMS_LOADED);
        ctx.registerReceiver(this, intentFilter);

        Log.d(TAG, "BaseModel created [ModulId:" + mModulId + "," + MainActivity.getModulName(mModulId) + "]");
    }

    protected SimpleDateFormat getDateTimeFormatter() {
        if( mDateTimeFormatter == null) {
            mDateTimeFormatter = new SimpleDateFormat("HH:mm dd.MM.yy");
            mDateTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        }
        return mDateTimeFormatter;
    }

    protected Context getContext() {
        return mCtx;
    }

    protected SharedPreferences getPrefs() {
        if( mPrefs == null)
            mPrefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
        // mPrefs = mCtx.getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE);
        return mPrefs;
    }

    protected static SharedPreferences getPrefs( Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    protected void storePrefsString(String prefId, String value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString( prefId, value);
        editor.commit();
    }

    protected void storePrefsBoolean(String prefId, boolean value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putBoolean( prefId, value);
        editor.commit();
    }
/*
    protected boolean isModulInList( int modulId) {
        return mCtx.getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE).getBoolean( String.valueOf(modulId), false);
    } */

    protected boolean isModulInList() {
        return mCtx.getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE).getBoolean( String.valueOf(mModulId), false);
    }

    protected abstract void initItems() throws Exception;

    protected void handleHabitemsLoaded() {
        try {
            initItems();
        } catch( Exception ex) {
            sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "initItems", ex.getMessage());
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ItemManager.ACTION_HABITEMS_LOADED))
            handleHabitemsLoaded();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected boolean isAM() {
        return (new GregorianCalendar().get(Calendar.AM_PM)) == Calendar.AM;
    }

    protected boolean isPM() {
        return (new GregorianCalendar().get(Calendar.AM_PM)) == Calendar.PM;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void sendUpdateListItemBroadcast() {
        Intent bc = new Intent();
        bc.setAction( MainListAdapter.ACTION_UPDATE_LISTITEM);
        //bc.putExtra( MainListAdapter.KEY_POSITION, mDataHolder.getPos());
        bc.putExtra( MainListAdapter.KEY_MODUL_ID, mModulId);
        mCtx.sendBroadcast(bc);
    }

    protected void sendUpdateListItemBroadcast( boolean showInlist) {
        sendUpdateListItemBroadcast( mModulId, showInlist);
    }

    protected void sendUpdateListItemBroadcast( int modulId, boolean showInlist) {
        Intent bc = new Intent();
        bc.setAction( MainListAdapter.ACTION_UPDATE_LISTITEM);
        bc.putExtra( MainListAdapter.KEY_SHOW_IN_LIST, showInlist);
        bc.putExtra( MainListAdapter.KEY_MODUL_ID, modulId);
        mCtx.sendBroadcast(bc);
    }

    protected void sendSystemBroadcast( String action, String className, String objectName, String message) {
        Intent bc = new Intent();
        bc.setAction( action);
        bc.putExtra( SystemModel.KEY_CLASS, className);
        bc.putExtra( SystemModel.KEY_OBJECT, objectName);
        bc.putExtra( SystemModel.KEY_MESSAGE, message);
        mCtx.sendBroadcast(bc);
    }

    protected void sendItemReadBroadcast( String itemName) {
        Intent bc = new Intent();
        bc.setAction( ItemManager2.ACTION_ITEM_READ);
        bc.putExtra( ItemManager2.ITEM_NAME, itemName);
        mCtx.sendBroadcast(bc);
    }

    protected void sendItemCmdBroadcast( String itemName, String cmd) {
        Intent bc = new Intent();
        bc.setAction( ItemManager2.ACTION_ITEM_CMD);
        bc.putExtra( ItemManager2.ITEM_NAME, itemName);
        bc.putExtra( ItemManager2.ITEM_CMD, cmd);
        mCtx.sendBroadcast(bc);
    }

    protected void startTimerForHideModul( int delaySec) {
        startTimerForHideModul( mModulId, delaySec);
    }

    protected void startTimerForHideModul( int modulId, int delaySec) {
        mHideModulTimer = new Timer();
        mHideModulTimer.schedule(new HideModulTimerTask(modulId), delaySec * 1000);
        mHideModulTimerScheduled = true;
        Log.d(TAG, "startTimerForHideModul [ModulId:" + modulId + "," + MainActivity.getModulName(modulId) + "] mModulId:" + mModulId);
    }

    protected boolean isTimerForHideModulScheduled() {
        return mHideModulTimer != null && mHideModulTimerScheduled;
    }

    protected void cancelTimerForHideModul() {
        if( mHideModulTimer != null && mHideModulTimerScheduled) {
            mHideModulTimer.cancel();
            mHideModulTimerScheduled = false;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Timertask
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class HideModulTimerTask extends TimerTask {

        private int     mRemoveModulId;

        public HideModulTimerTask( int modulId) {
            mRemoveModulId = modulId;
        }

        public void run() {
            Log.d(TAG, "Modul: " + mModulId + ", AUTO remove from list");
            sendUpdateListItemBroadcast(mModulId, false);
            Log.d(TAG, "remove modul by timer [ModulId:" + mRemoveModulId + "," + MainActivity.getModulName(mRemoveModulId) + "] mModulId:" + mModulId);
            mHideModulTimerScheduled = false;
        }
    }

}
