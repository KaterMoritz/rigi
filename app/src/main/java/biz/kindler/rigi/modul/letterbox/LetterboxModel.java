package biz.kindler.rigi.modul.letterbox;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.MainListAdapter;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.OneButtonDataHolder;
import biz.kindler.rigi.modul.system.Log;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 29.11.16.
 */

public class LetterboxModel extends BaseModel {

    private final static String TAG = LetterboxModel.class.getSimpleName();

    private static String       DEFAULT_TITLE               = "Briefkasten";
    private static String       DEFAULT_INFO                = "Keine Postmeldung";
    private static String       DEFAULT_BUTTON_INFO         = "Briefkasten";
    private static String       DEFAULT_BUTTON_TEXT         = "öffnen";

    private static String       LETTERBOX_IS_OPEN_INFO      = "Briefkasten ist offen";
    private static String       MAIL_NOTIFICATION_TITLE     = "Post ist da";
    private static String       CLOSE_BUTTON_TEXT           = "schliessen";
    private static String       TIMER_BUTTON_INFO           = "schliesst automatisch";

    // item Names
    private static String       LETTERBOX                   = "Briefkasten";
    private static String       MAIL_NOTIFICATION           = "PostMeldung";

    private SimpleDateFormat    mTimeFormatter;
    private OneButtonDataHolder mDataHolder;
    private String              mMailNotiTimestamps;


    public LetterboxModel( Context ctx) {
        super(ctx, MainActivity.LETTERBOX);

        mDataHolder = new LetterboxDataHolder();

        mTimeFormatter = new SimpleDateFormat("HH:mm");
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.LETTERBOX);
        intentFilter.addAction(MAIL_NOTIFICATION);
        intentFilter.addAction(LETTERBOX);
        ctx.registerReceiver(this, intentFilter);
    }


    public OneButtonDataHolder getDataHolder( ) {
        return mDataHolder;
    }

    protected void initItems() {

        sendItemReadBroadcast(MAIL_NOTIFICATION);
        sendItemReadBroadcast(LETTERBOX);
        /*
        // Letterbox
        mLetterboxHABItem = getItemManager().getItem( LETTERBOX);
        mLetterboxHABItem.setContext( getContext());
        // Mail Notification
        OpenHABItem mailNoti = getItemManager().getItem( MAIL_NOTIFICATION);
        mailNoti.setContext( getContext());
        mailNoti.setHttpConn(HttpItem.REQUEST_FOR_ON_OFF, 1);
        */
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive( context, intent);

        String action = intent.getAction();
        String value = intent.getStringExtra(ItemManager2.VALUE);

        if (action.equals(MAIL_NOTIFICATION))
            handlePostNotification(value);
        else if( action.equals(LETTERBOX))
            handleLetterboxLockState(value);
        else if (action.equals(MainListAdapter.ACTION_BUTTON_CLICK_MODUL + MainActivity.LETTERBOX)) {
            Log.d(TAG, "Letterbox: button klicked");
            if( mDataHolder.getButtonText().equals( DEFAULT_BUTTON_TEXT)) { // öffnen
                sendItemCmdBroadcast( LETTERBOX, "ON");
                Log.d(TAG, "Letterbox: send OPEN (button klicked)");
            }
            else if( mDataHolder.getButtonText().equals( CLOSE_BUTTON_TEXT)) {
                sendItemCmdBroadcast( LETTERBOX, "OFF");
                Log.d(TAG, "Letterbox: send CLOSE (button klicked)");
            }
        }
    }

    private void handlePostNotification( String value) {
        if( mMailNotiTimestamps == null && value.equals( "ON")) {
            mMailNotiTimestamps = mTimeFormatter.format( new Date());
            handleNewNofificationNow();
            Log.i(TAG, "PostNotification ON");
        }
        else if( mMailNotiTimestamps != null && value.equals( "OFF")) {
            mMailNotiTimestamps = null;  // reset timestamp
            handleNofificationOff();  // switched of the "Post notification"
            Log.d(TAG, "PostNotification OFF");
        }
    }

    private void handleNewNofificationNow() {
        mDataHolder.setTitle( MAIL_NOTIFICATION_TITLE);
        mDataHolder.setInfo( mMailNotiTimestamps);
        mDataHolder.setButtonInfo( DEFAULT_BUTTON_INFO);
        mDataHolder.setHighlighted( true);
        mDataHolder.setImgResId( R.drawable.mail_notification);
       // mDataHolder.setUpdateAll(true);
        sendUpdateListItemBroadcast( true);
        // play sound
        //playNofificationSound( mCtx);
        playRawNofificationSound( getContext());
    }

    private void handleNofificationOff() {
        mDataHolder.setTitle( DEFAULT_TITLE);
        mDataHolder.setInfo( DEFAULT_INFO);
        mDataHolder.setButtonInfo( "");
        mDataHolder.setHighlighted( false);
        mDataHolder.setImgResId( R.drawable.letterbox);
        //mDataHolder.setUpdateAll(true);
        sendUpdateListItemBroadcast();
        startTimerForHideModul( 60);
    }

    public static void playDefaultNofificationSound( Context ctx) {
        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(ctx, soundUri);
        r.play();
    }

    public static void playRawNofificationSound( Context ctx) {
        AudioManager manager = (AudioManager)ctx.getSystemService(Context.AUDIO_SERVICE);
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0);
        //Define sound URI
        MediaPlayer mPlayer = MediaPlayer.create(ctx, R.raw.samsunggal_v7h8lenf);
        mPlayer.start();
    }

    private void handleLetterboxLockState( String value) {
        if( value != null) {
            Log.d(TAG, "handleLetterboxLockState: statusOpen=" + value);
            if (value.equals("ON")) {
                mDataHolder.setTitle(mMailNotiTimestamps == null ? DEFAULT_TITLE : MAIL_NOTIFICATION_TITLE);
                mDataHolder.setInfo(LETTERBOX_IS_OPEN_INFO);
                mDataHolder.setButtonInfo(TIMER_BUTTON_INFO);
                mDataHolder.setButtonText(CLOSE_BUTTON_TEXT);
                sendUpdateListItemBroadcast();
                Log.d(TAG, "handleLetterboxLockState: status open");
            } else if (value.equals("OFF") && mMailNotiTimestamps == null) {
                mDataHolder.setTitle(DEFAULT_TITLE);
                mDataHolder.setInfo(DEFAULT_INFO);
                mDataHolder.setButtonInfo(DEFAULT_BUTTON_INFO);
                mDataHolder.setButtonText(DEFAULT_BUTTON_TEXT);
                sendUpdateListItemBroadcast();
                Log.d(TAG, "handleLetterboxLockState: status closed, mMailNotiTimestamps is null");
            } else if (value.equals("OFF") && mMailNotiTimestamps != null) {
                mDataHolder.setInfo(mMailNotiTimestamps);
                mDataHolder.setButtonInfo(DEFAULT_BUTTON_INFO);
                mDataHolder.setButtonText(DEFAULT_BUTTON_TEXT);
                sendUpdateListItemBroadcast();
                Log.d(TAG, "handleLetterboxLockState: status closed, mMailNotiTimestamps: " + mMailNotiTimestamps);
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
