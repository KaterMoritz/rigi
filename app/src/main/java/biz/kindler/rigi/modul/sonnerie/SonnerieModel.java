package biz.kindler.rigi.modul.sonnerie;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.OneButtonDataHolder;
import biz.kindler.rigi.modul.clock.TimeAndDateModel;
import biz.kindler.rigi.modul.system.Log;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 30.06.17.
 */

public class SonnerieModel extends BaseModel {

    private final static String TAG = SonnerieModel.class.getSimpleName();

    // item Names
    private final static String SONNERIE        = "Sonnerie";
    private final static String DOORBELL_NOW    = "Es klingelt";

    private static final int    KEEP_PICTURES_FOR_X_DAYS            = 30; //TODO: getPrefs()

    private OneButtonDataHolder mDataHolder;
    private SimpleDateFormat mTimeFormatter;
    private String           mLastDoorbellTimestamp;


    public SonnerieModel(Context ctx) {
        super(ctx, MainActivity.SONNERIE);

        mDataHolder = new SonnerieDataHolder();

        mTimeFormatter = new SimpleDateFormat("HH:mm");
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TimeAndDateModel.ACTION_DAY_SEGMENT);
        intentFilter.addAction(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.SONNERIE);
        intentFilter.addAction(MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.SONNERIE);
        intentFilter.addAction(SONNERIE);
        ctx.registerReceiver(this, intentFilter);
    }

    public OneButtonDataHolder getDataHolder() {
        return mDataHolder;
    }

    @Override
    protected void initItems() throws Exception {
        sendItemReadBroadcast(SONNERIE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();
        String value = intent.getStringExtra(ItemManager2.VALUE);

        if(action.equals(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.SONNERIE))
            handleRecordsClicked();
        else if (action.equals(MainListAdapter.ACTION_PANEL_CLICK_MODUL + MainActivity.SONNERIE))
            handleCamButtonClick();
        else if (action.equals( SONNERIE))
            handleSonnerie(value);
        else if (action.equals(TimeAndDateModel.ACTION_DAY_SEGMENT)) {
            if( intent.getBooleanExtra(TimeAndDateModel.KEY_MIDNIGHT, false))
                handleMidnight();
        }
    }

    private void handleSonnerie( String value) {
        Log.i(TAG, "Sonnerie: " + value);
        if( value != null && value.equals( "ON")) {
            mLastDoorbellTimestamp = mTimeFormatter.format( new Date());
            mDataHolder.setHighlighted(true);
            mDataHolder.setInfo(DOORBELL_NOW);
            sendUpdateListItemBroadcast( true);
            playNofificationSound(getContext());
            // TODO: if( getPrefs().getBoolean(SonneriePreferenceFragment.SHOW_CAM_WHEN_BELL_RING, false))
            if( true)
                showCam( false, true);
        }
        else if( value != null && value.equals( "OFF")) {
            mDataHolder.setHighlighted(false);
            mDataHolder.setInfo(mLastDoorbellTimestamp == null ? "" : mLastDoorbellTimestamp);
            sendUpdateListItemBroadcast();
        }
    }

    private void handleMidnight() {
        if( isModulInList()) {
            sendUpdateListItemBroadcast( false);
            mLastDoorbellTimestamp = null;
            Log.d(TAG, "Its midnight, Modul Sonnerie remove from list");
        }
        new CleanupOldPicturesTask().execute();
    }

    private void handleCamButtonClick() {
        showCam( true, true);
    }

    private void handleRecordsClicked() {
        showCam( true, false);
    }

    private void showCam( boolean openByButton, boolean showLiveCam) {
        if( ! showLiveCam) {
            Intent camArchIntent = new Intent("CamArchIntent", null, getContext(), DoorCamArchiveActivity.class);
            getContext().startActivity(camArchIntent);
        } else {
            Intent calIntent = new Intent("CamIntent", null, getContext(), DoorCamLiveActivity.class);//DoorCamActivity2.class);
            Bundle b = new Bundle();
            b.putInt(DoorCamLiveActivity.OPEN_BY, openByButton ? DoorCamLiveActivity.OPEN_MANUELL : DoorCamLiveActivity.OPEN_AUTO);
            calIntent.putExtras(b);
            getContext().startActivity(calIntent);
        }
    }

    public static void playNofificationSound( Context ctx) {
        //Define sound URI
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer player = MediaPlayer.create(ctx, notification);
        player.setLooping(false);
        player.start();
        /*
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);  // TYPE_NOTIFICATION
        Ringtone r = RingtoneManager.getRingtone(ctx, soundUri);
        r.play(); */
        Log.d(TAG, "playNofificationSound");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // delete old pictures task
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class CleanupOldPicturesTask extends AsyncTask<Void, Void, Void> {

        SimpleDateFormat mDateTimeFormatter;

        @Override
        protected Void doInBackground(Void... params) {
            mDateTimeFormatter = new SimpleDateFormat("dd.MM.yy HH:mm");

            try {
                long DAY_SECONDS = 86400;
                long todayInMs = new Date().getTime();
                long deleteOlderAs = todayInMs - (DAY_SECONDS * KEEP_PICTURES_FOR_X_DAYS * 1000);

                String camImgDir = DoorCamActivity.getImgPathDir();
                File imgDir = new File(camImgDir);
                File[] fileArr = imgDir.listFiles();
                Log.i(TAG, "cam files found [cnt: " + fileArr.length + "]");

                ArrayList<File> delFileArr = new ArrayList<>();

                for (File file: fileArr) {
                    String filename = file.getName();
                    String fileTimestamp = filename.substring(0, filename.length() - 4); // remove .jpg from filename
                    long timstamp = Long.parseLong(fileTimestamp);
                    Log.d(TAG, "timestamp:     " + mDateTimeFormatter.format(new Date(timstamp)) + "]");
                    Log.d(TAG, "deleteOlderAs: " + mDateTimeFormatter.format(new Date(deleteOlderAs)) + "]");
                    if( timstamp < deleteOlderAs)
                        delFileArr.add( new File( camImgDir + File.separator + timstamp + ".jpg"));
                }
                Log.d(TAG, "delete cam files [older as: " + mDateTimeFormatter.format(new Date(deleteOlderAs)) + "]");

                if( delFileArr.size() == 0)
                    Log.d(TAG, "no cam files for delete");
                else {
                    Log.d(TAG, "delete cam files [cnt: " + delFileArr.size() + "]");
                    int cntDelFiles = deleteFiles(delFileArr);
                    Log.d(TAG, "cam files deleted: " + cntDelFiles);
                }

                return null;
            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
                return null;
            }
        }

        private int deleteFiles( ArrayList<File> fileArr) {
            int okCnt = 0;
            for (File file: fileArr) {
                boolean status = file.delete();
                Log.i(TAG, "delete file: " + file.getName() + " [" + (status ? "OK" : "FAILED") + "]");
                if( status)
                    okCnt++;
            }
            return okCnt;
        }

    }
}