package biz.kindler.rigi;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastButtonFactory;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import biz.kindler.rigi.modul.background.BackgroundModel;
import biz.kindler.rigi.modul.calendar.CalendarModel;
import biz.kindler.rigi.modul.calendar.CalendarSetupActivity;
import biz.kindler.rigi.modul.clock.TimeAndDateModel;
import biz.kindler.rigi.modul.entree.EntreeModel;
import biz.kindler.rigi.modul.entree.EntreeView;
import biz.kindler.rigi.modul.forecourt.ForecourtModel;
import biz.kindler.rigi.modul.garage.GarageModel;
import biz.kindler.rigi.modul.garden.GardenModel;
import biz.kindler.rigi.modul.letterbox.LetterboxModel;
import biz.kindler.rigi.modul.lock.LockModel;
import biz.kindler.rigi.modul.misc.MiscModel;
import biz.kindler.rigi.modul.shutter.ShutterModel;
import biz.kindler.rigi.modul.sonnerie.SonnerieModel;
import biz.kindler.rigi.modul.sound.SoundModel;
import biz.kindler.rigi.modul.sound.SoundModel3;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import biz.kindler.rigi.modul.vbl.VBLModel3;
import biz.kindler.rigi.modul.watering.WateringModel;
import biz.kindler.rigi.modul.weather.WeatherModel;
import biz.kindler.rigi.modul.weatherstation.WeatherstationModel;
import biz.kindler.rigi.settings.LogPreferenceFragment;
import biz.kindler.rigi.settings.SettingsActivity;
import biz.kindler.rigi.settings.SoundPreferenceFragment2;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;


/*
*  Created by patrick kindler (katermoritz100@gmail.com)
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private final static String 	TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS = 1;
    private static String[] PERMISSIONS_STORAGE = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static String[] REQUIRED_PERMISSIONS = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_SETTINGS };

    public static final String      PREFS_ID                        = "rigi";
    public static final String      ACTION_ACTIVITY_RESULT          = "action-activityresult-modulid-";

    // not in modul list
    public static final int         WEATHER = -13;
    public static final int         MISC = -12;
    public static final int         GARDEN = -11;
    public static final int         TIMEANDDATE = -10;
    // in modul list
    public static final int         LETTERBOX = 0;
    public static final int         GARAGE = 1;
    public static final int         ENTREE = 2;
    public static final int         SHUTTER = 3;
    public static final int         CALENDAR = 4;
    public static final int         VBL = 5;
    public static final int         SYSTEM = 6;
    public static final int         WEATHERSTATION = 7;
    public static final int         FORECOURT = 8;
    public static final int         WATERING = 9;
    public static final int         SONNERIE = 10;
    public static final int         SOUND = 11;
    public static final int         LOCK = 12;


    public static final int         TEXT_ITEM = 0;
    public static final int         ONE_BUTTON_ITEM = 1;
    public static final int         TWO_BUTTON_ITEM = 2;
    public static final int         FOUR_BUTTON_ITEM = 3;
    public static final int         TWO_LINES_ITEM = 4;
    public static final int         THREE_LINES_ITEM = 5;

    // buttons on Main Panel TAG
    public static final String      MENU = "menu";
    public static final String      CAM  = "cam";
    public static final String      LOCK_AND_GO  = "lockandgo";

    private static final String     MOTION_SENSOR_GARAGE = "Motion_Sensor_Garage";
    private static final String     MOTION_SENSOR_GARDEN = "Motion_Sensor_Garden";
   // public static final String      LOCK_STATE           = "Lock_State";

    private RecyclerView                            mMainListView;
    private MainListAdapter                         mMainListAdapter;
    private LinearLayoutManager                     mLinearLayoutManager;
    private DrawerLayout                            mDrawer;
    private ItemTouchHelper                         mItemTouchHelper;
    private SharedPreferences                       mPrefs;
    private ItemManager2                            mItemManager;
    private ComponentName                           mAdminComponentName;
    private DevicePolicyManager                     mDevicePolicyManager;
    private PackageManager                          mPackageManager;
    private ImageView                               mMotionImageView;
    private LottieAnimationView                     mLockImageAniView;
    private ImageView                               mLockAndGoImageView;
    private ProgressBar                             mLockProgress;
    private ProgressBar                             mLockAndGoProgress;
    private MyBroadcastReceiver                     mMyBroadcastReceiver;
    private ArrayList                               mAllListDataArr;
    private androidx.mediarouter.app.MediaRouteButton mMediaRouteButton;

    private static final String         Battery_PLUGGED_ANY = Integer.toString(BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB | BatteryManager.BATTERY_PLUGGED_WIRELESS);
    private static final String         DONT_STAY_ON = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));
        if (getIntent().getBooleanExtra("crash", false))
            Toast.makeText(this, "App restarted after crash", Toast.LENGTH_SHORT).show();

        verifyPermissions(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        // test commit

/*
        // for testing and optimizing
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads()
				.detectDiskWrites()
				.detectNetwork()   // or .detectAll() for all detectable problems
                //.detectAll()
				.penaltyLog()
				.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects()
				.detectLeakedClosableObjects()
				.penaltyLog()
				.penaltyDeath()
				.build());
*/

        // COSU
       // mAdminComponentName = DeviceAdminReceiver().getComponentName(this);
       // mAdminComponentName = new DeviceAdminReceiver().getWho(this);
        mAdminComponentName = new ComponentName(this, DeviceAdminReceiver.class);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mPackageManager = getPackageManager();

        //mDevicePolicyManager.setLockTaskPackages(mAdminComponentName,new String[]{getPackageName()});

       // setDefaultCosuPolicies(true);
        try {
            if (mDevicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                Log.d(TAG, "App is Device Owner");
                try {
                    // Settings.Global.putInt(getContentResolver(),Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0);

                    enableStayOnWhilePluggedIn(true);
                    Log.d(TAG, "enableStayOnWhilePluggedIn OK");
                } catch( Exception ex) {
                    Log.e(TAG, "enableStayOnWhilePluggedIn exc: " + ex.getMessage());
                }
                setDefaultCosuPolicies(true);
                Log.d(TAG, "DefaultCosuPolicies set");
                sendSystemBroadcast( SystemModel.ACTION_LOG, getClass().getName(), "App is Device Owner", "DefaultCosuPolicies set");
            } else {
                Log.e(TAG, "App is NOT Device Owner");
                sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "App is NOT Device Owner", "");
                Toast.makeText(getApplicationContext(), "Not Device Owner", Toast.LENGTH_LONG).show();
            }
        } catch( Exception ex) {
            Log.e(TAG, "setDefaultCosuPolicies exc:" + ex.getMessage());
            sendSystemBroadcast( SystemModel.ACTION_EXCEPTION, getClass().getName(), "setDefaultCosuPolicies", ex.getMessage());
        }
            // end cosu


        // open site menu button
        ImageButton menuBtn = (ImageButton) findViewById(R.id.open_menu_button);
        menuBtn.setTag(MENU);
        menuBtn.setOnClickListener(this);

        mMotionImageView = (ImageView) findViewById( R.id.motion_image);
        mMotionImageView.setBackgroundResource( R.drawable.motion_garage);
        mMotionImageView.setVisibility( View.INVISIBLE);

        mLockImageAniView = (LottieAnimationView)findViewById( R.id.lock_image_ani);
        mLockImageAniView.setVisibility( View.INVISIBLE);
        mLockImageAniView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tagObj = v.getTag();
                if (tagObj != null && tagObj instanceof Integer) {
                    int currLockState = ((Integer)mLockImageAniView.getTag()).intValue();
                    Intent bc = new Intent();
                    bc.setAction(LockModel.ACTION_LOCK);
                    if (currLockState == LockModel.LOCK_LOCKED)
                        bc.putExtra("lockActionCmd", LockModel.CMD_UNLOCK);
                    else if (currLockState == LockModel.LOCK_UNLOCKED)
                        bc.putExtra("lockActionCmd", LockModel.CMD_LOCK);
                    getContext().sendBroadcast(bc);
                    mLockProgress.setVisibility(View.VISIBLE);
                }
            }
        });

        mLockProgress = (ProgressBar)findViewById( R.id.lock_progress);

        mLockAndGoImageView = (ImageView) findViewById( R.id.lock_n_go_image);
        mLockAndGoImageView.setTag(LOCK_AND_GO);
        mLockAndGoImageView.setBackgroundResource( R.drawable.lockngo);
        mLockAndGoImageView.setVisibility( View.INVISIBLE);
        mLockAndGoImageView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bc = new Intent();
                bc.setAction(LockModel.ACTION_LOCK);
                bc.putExtra("lockActionCmd", LockModel.CMD_LOCK_N_GO); // LockModel.CMD_LOCK_N_GO_WITH_UNLATCH);
                getContext().sendBroadcast(bc);
                mLockAndGoProgress.setVisibility(View.VISIBLE);
            }
        });

        mLockAndGoProgress = (ProgressBar)findViewById( R.id.lock_n_go_progress);

        ImageButton camBtn = (ImageButton) findViewById(R.id.cam_button);
        camBtn.setTag(CAM);
        camBtn.setOnClickListener(this);


        mItemManager = new ItemManager2( getContext());

        // set all modul prefs, not visible in list
        SharedPreferences.Editor prefsEditor = getPrefs().edit();
        prefsEditor.putBoolean(String.valueOf(LETTERBOX), false);
        prefsEditor.putBoolean(String.valueOf(GARAGE), false);
        prefsEditor.putBoolean(String.valueOf(ENTREE), false);
        prefsEditor.putBoolean(String.valueOf(SHUTTER), false);
        prefsEditor.putBoolean(String.valueOf(CALENDAR), false);
        prefsEditor.putBoolean(String.valueOf(VBL), false);
        prefsEditor.putBoolean(String.valueOf(SYSTEM), false);
        prefsEditor.putBoolean(String.valueOf(WEATHERSTATION), false);
        prefsEditor.putBoolean(String.valueOf(FORECOURT), false);
        prefsEditor.putBoolean(String.valueOf(WATERING), false);
        prefsEditor.putBoolean(String.valueOf(SONNERIE), false);
        prefsEditor.putBoolean(String.valueOf(SOUND), false);
        prefsEditor.apply();

        // System messages
        SystemModel systemModel = new SystemModel( this);
        // Date and Time
        new TimeAndDateModel(this);
        // Background
        new BackgroundModel(this);
        // Weather
        new WeatherModel(this);
        // Entree
        EntreeModel entreeModel = new EntreeModel(this);
        // VBL
        VBLModel3 vblModel = new VBLModel3( this);
        // KClass<VBLModel3> vblModel = JvmClassMappingKt.getKotlinClass(VBLModel3.class);
        // Calendar
        CalendarModel calendarModel = new CalendarModel( this);
        // Letterbox
        LetterboxModel letterboxModel = new LetterboxModel(this);
        // Garage
        GarageModel garageModel = new GarageModel( this);
        // Shutter
        ShutterModel shutterModel = new ShutterModel( this);
        // Weatherstation
        WeatherstationModel weatherstationModel = new WeatherstationModel( this);
        // Watering
        WateringModel wateringModel = new WateringModel( this);
        // Aussenlicht bei Entree
        ForecourtModel forecourtModel = new ForecourtModel( this);
        // Garden Plug
        new GardenModel(this);
        // Misc
        new MiscModel(this);
        // Sonnerie
        SonnerieModel sonnerieModel = new SonnerieModel(this);
        // Sound
        SoundModel3 soundModel = new SoundModel3(this);
        // Lock
        LockModel lockModel = new LockModel(this);

        mAllListDataArr = new ArrayList<>();
        mAllListDataArr.add(letterboxModel.getDataHolder());
        mAllListDataArr.add(garageModel.getDataHolder());
        mAllListDataArr.add(entreeModel.getDataHolder());
        mAllListDataArr.add(shutterModel.getDataHolder());
        mAllListDataArr.add(calendarModel.getDataHolder());
        mAllListDataArr.add(vblModel.getDataHolder());
        mAllListDataArr.add(systemModel.getDataHolder());
        mAllListDataArr.add(weatherstationModel.getDataHolder());
        mAllListDataArr.add(wateringModel.getDataHolder());
        mAllListDataArr.add(forecourtModel.getDataHolder());
        mAllListDataArr.add(sonnerieModel.getDataHolder());
        mAllListDataArr.add(soundModel.getDataHolder());

        /*
        mLinearLayoutManager = new LinearLayoutManager(this);

        mMainListView = (RecyclerView) findViewById(R.id.main_list_view);
        mMainListView.setLayoutManager(mLinearLayoutManager);
        mMainListView.setHasFixedSize(true);
        // mMainListView.addItemDecoration( new DividerItemDecoration());
        //mMainListView.setItemAnimator( animator);
        mMainListAdapter = new MainListAdapter(getContext(), mMainListView);
        mMainListAdapter.setAllData(allListDataArr);
       // mMainListAdapter.setClickListener(this);
        mMainListView.setAdapter(mMainListAdapter);

        // Swipe to dismiss: see https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.i1dw67z85
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mMainListAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mMainListView);
*/

        initMainList();

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, null/*toolbar*/, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.setDrawerIndicatorEnabled(false);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // mItemManager.loadItemsFromServer();

        //mMainListAdapter.checkModulVisibility(false);
   //     sendUpdateListitemBroadcast( WEATHERSTATION);  // to show in list at startup
    //    sendUpdateListitemBroadcast( SYSTEM);  // will auto hide when connected with OpenHAB2 SSE

        mMyBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainListAdapter.ACTION_CHANGED_IN_LIST_MODUL + MainActivity.SOUND);
        intentFilter.addAction(MOTION_SENSOR_GARAGE);
        intentFilter.addAction(MOTION_SENSOR_GARDEN);
        intentFilter.addAction(LockModel.ACTION_LOCK);
        intentFilter.addAction(LogPreferenceFragment.SYSTEMSERVICE);
        registerReceiver(mMyBroadcastReceiver, intentFilter);

/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor prefEditor = prefs.edit();
            prefEditor.putBoolean("stop_lock_task_switch", false);
            prefEditor.commit();
        } */


       // verifyPermissions(this);


        mMediaRouteButton = (MediaRouteButton)findViewById(R.id.media_route_button);
        CastButtonFactory.setUpMediaRouteButton(this, mMediaRouteButton);
        mMediaRouteButton.setVisibility(View.INVISIBLE);
    }

    private void initMainList() {
        mLinearLayoutManager = new LinearLayoutManager(this);

        mMainListView = (RecyclerView) findViewById(R.id.main_list_view);
        mMainListView.setLayoutManager(mLinearLayoutManager);
        mMainListView.setHasFixedSize(true);
        // mMainListView.addItemDecoration( new DividerItemDecoration());
        //mMainListView.setItemAnimator( animator);
        mMainListAdapter = new MainListAdapter(getContext(), mMainListView);
        mMainListAdapter.setAllData(mAllListDataArr);
        // mMainListAdapter.setClickListener(this);
        mMainListView.setAdapter(mMainListAdapter);

        // Swipe to dismiss: see https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.i1dw67z85
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mMainListAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mMainListView);

        System.out.println( "isShown:" + mMainListView.isShown());
        mMainListView.setVisibility(View.VISIBLE);
        System.out.println( "isShown:" + mMainListView.isShown());
    }

    public static String getModulName( int modulId) {
        switch (modulId) {
            case( WEATHER) : return "WEATHER";
            case( MISC) : return "MISC";
            case( GARDEN) : return "GARDEN";
            case( TIMEANDDATE) : return "TIMEANDDATE";
            case( LETTERBOX) : return "LETTERBOX";
            case( GARAGE) : return "GARAGE";
            case( ENTREE) : return "ENTREE";
            case( SHUTTER) : return "SHUTTER";
            case( CALENDAR) : return "CALENDAR";
            case( VBL) : return "VBL";
            case( SYSTEM) : return "SYSTEM";
            case( WEATHERSTATION) : return "WEATHERSTATION";
            case( FORECOURT) : return "FORECOURT";
            case( WATERING) : return "WATERING";
            case( SONNERIE) : return "SONNERIE";
            case( SOUND) : return "SOUND";
            default : return "UNKNOWN";
        }
    }

    /////////////////////////////////////////////////////////////////////
    //persmission method.
    public static void verifyPermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeSettingsPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_SETTINGS);
      //  boolean hasPermForWriteSettings = Settings.System.canWrite(activity); // required for sound play

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED || writeSettingsPermission != PackageManager.PERMISSION_GRANTED) { //! hasPermForWriteSettings) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    REQUIRED_PERMISSIONS,
                    REQUEST_PERMISSIONS
            );
        }
    }

    private void showFragment(Fragment fragment) {
        // getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    @TargetApi(Build.VERSION_CODES.M)
    //@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "Rigi app onStart");

       // Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);

       // Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);


        // start lock task mode if its not already active
        if(mDevicePolicyManager.isLockTaskPermitted(this.getPackageName())){
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if(am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
                startLockTask();
            }
        }

/*
        if( Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            // start lock task mode if it's not already active
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            // ActivityManager.getLockTaskModeState api is not available in pre-M.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (!am.isInLockTaskMode()) {
                    startLockTask();
                }
            } else {
                if (am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_NONE) {
                    startLockTask();
                }
            }
        } */
    }

    public Context getContext() {
        return this;
    }

    protected SharedPreferences getPrefs() {
        if( mPrefs == null)
            //mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            mPrefs = getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE);
        return mPrefs;
    }

    @Override
    public void onResume() {
        super.onResume();

       // hideSystemUI();

       // initMainList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMyBroadcastReceiver);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        setDisplayBrightness( EntreeView.DISPLAY_BRIGHT);
        return super.dispatchTouchEvent(event);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CalendarSetupActivity.RESULT_SUCCESFULL_SETUP && resultCode == Activity.RESULT_OK) {
            Intent bc = new Intent();
            bc.setAction( ACTION_ACTIVITY_RESULT + CALENDAR);
            getContext().sendBroadcast(bc);
        }

        else if (requestCode == 199) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean stoplocktask = prefs.getBoolean("stop_lock_task_switch", false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (stoplocktask)
                    stopLockTask();
            }
        }

        else if( requestCode == SoundModel.DETAIL_ACTIVITY_RESULT_CODE  && resultCode == Activity.RESULT_OK) {
            data.setAction( ACTION_ACTIVITY_RESULT + SOUND);  // send Intent again to SoundModel
            getContext().sendBroadcast(data);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_garage)
            sendUpdateListitemBroadcast( GARAGE);
        else if (id == R.id.nav_blinds)
            sendUpdateListitemBroadcast( SHUTTER);
        else if (id == R.id.nav_letterbox)
            sendUpdateListitemBroadcast( LETTERBOX);
        else if (id == R.id.nav_entree)
            sendUpdateListitemBroadcast( ENTREE);
        else if (id == R.id.nav_sonnerie)
            sendUpdateListitemBroadcast( SONNERIE);
        else if (id == R.id.nav_calendar)
            sendUpdateListitemBroadcast( CALENDAR);
        else if (id == R.id.nav_vbl)
            sendUpdateListitemBroadcast( VBL);
        else if (id == R.id.nav_system)
            sendUpdateListitemBroadcast( SYSTEM);
        else if (id == R.id.nav_weatherstation)
            sendUpdateListitemBroadcast( WEATHERSTATION);
        else if (id == R.id.nav_light_avenue)
            sendUpdateListitemBroadcast( FORECOURT);
        else if (id == R.id.nav_watering)
            sendUpdateListitemBroadcast( WATERING);
        else if (id == R.id.nav_music)
            sendUpdateListitemBroadcast( SOUND);
        else if (id == R.id.nav_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult( settingsIntent, 199);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendUpdateListitemBroadcast( int modulId) {
        Intent bc = new Intent();
        bc.setAction( MainListAdapter.ACTION_UPDATE_LISTITEM);
        bc.putExtra( MainListAdapter.KEY_SHOW_IN_LIST, true);
        bc.putExtra( MainListAdapter.KEY_MODUL_ID, modulId);
        bc.putExtra( MainListAdapter.KEY_DO_ANIMATE, true);
        getContext().sendBroadcast(bc);
    }

    @Override
    public void onClick(View view) {
        Vibrator vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        if( vib != null && vib.hasVibrator())
            vib.vibrate(50);

       // view.playSoundEffect(android.view.SoundEffectConstants.CLICK);

        if (view.getTag() != null && view.getTag().equals(MENU))
            mDrawer.openDrawer(Gravity.LEFT);
        else if( view.getTag() != null && view.getTag().equals(CAM))
            sendButtonKlickedEvent( SONNERIE, 1);
        /*
        else if( view.getTag() != null && view.getTag().equals(LOCK_AND_GO)) {
            sendButtonKlickedEvent(LOCK, 3);
            mLockAndGoProgress.setVisibility(View.VISIBLE);
        } */
    }

    private void sendButtonKlickedEvent( int modulId, int buttonId) {
        Intent bc = new Intent();
        bc.setAction(MainListAdapter.ACTION_PANEL_CLICK_MODUL + modulId);
        bc.putExtra(MainListAdapter.KEY_BUTTON_NR, buttonId);
        getContext().sendBroadcast(bc);
    }

    public static void playNofificationSoundWithVibrator( Context ctx, String title) {
        //Define Notification Manager
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        androidx.core.app.NotificationCompat.Builder mBuilder = new androidx.core.app.NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.icon_02_09)
                .setContentTitle(title)
                .setContentText(title)
                .setSound(soundUri); //This sets the sound to play
        //Display notification
        notificationManager.notify(0, mBuilder.build());
    }

    private void setDisplayBrightness( final float value) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                try {
                    WindowManager.LayoutParams layout = getWindow().getAttributes();
                    layout.screenBrightness = value; // bright ? 0.7f : 0f;   // 1.0f
                    getWindow().setAttributes(layout);;
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COSU
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @TargetApi(Build.VERSION_CODES.M)
    private void setDefaultCosuPolicies(boolean active) throws Exception {
        // set user restrictions
        //setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);
        //setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);
        //setUserRestriction(UserManager.DISALLOW_ADD_USER, active);
      //  setUserRestriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, active);
     //   setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);

        // disable keyguard and status bar
        mDevicePolicyManager.setKeyguardDisabled(mAdminComponentName, active);
        mDevicePolicyManager.setStatusBarDisabled(mAdminComponentName, active);

        // enable STAY_ON_WHILE_PLUGGED_IN
        enableStayOnWhilePluggedIn(active);

        // set System Update policy
        if (active){
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, SystemUpdatePolicy.createWindowedInstallPolicy(60,120));
        } else {
            mDevicePolicyManager.setSystemUpdatePolicy(mAdminComponentName, null);
        }

        // set this Activity as a lock task package

        mDevicePolicyManager.setLockTaskPackages(mAdminComponentName,
                active ? new String[]{getPackageName()} : new String[]{});

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MAIN);
        intentFilter.addCategory(Intent.CATEGORY_HOME);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        if (active) {
            // set Cosu activity as home intent receiver so that it is started
            // on reboot
            mDevicePolicyManager.addPersistentPreferredActivity(mAdminComponentName, intentFilter, new ComponentName( getPackageName(), MainActivity.class.getName()));
        } else {
            mDevicePolicyManager.clearPackagePersistentPreferredActivities( mAdminComponentName, getPackageName());
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            mDevicePolicyManager.addUserRestriction(mAdminComponentName, restriction);
        } else {
            mDevicePolicyManager.clearUserRestriction(mAdminComponentName, restriction);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void enableStayOnWhilePluggedIn(boolean enabled) throws Exception {
        if (enabled) {
            mDevicePolicyManager.setGlobalSetting( mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, Battery_PLUGGED_ANY);
        } else {
            mDevicePolicyManager.setGlobalSetting(mAdminComponentName, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, DONT_STAY_ON);
        }
    }

    protected void sendSystemBroadcast( String action, String className, String objectName, String message) {
        Intent bc = new Intent();
        bc.setAction( action);
        bc.putExtra( SystemModel.KEY_CLASS, className);
        bc.putExtra( SystemModel.KEY_OBJECT, objectName);
        bc.putExtra( SystemModel.KEY_MESSAGE, message);
        sendBroadcast(bc);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Bradcast Receiver for Motion Sensor
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private Boolean mLockNgoActive = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String value = intent.getStringExtra(ItemManager2.VALUE);

            Log.d(TAG, "action: " + action + ", value: " + (value == null ? "null" : value));

            if (action.equals(MOTION_SENSOR_GARAGE))
                handleMotionSensorGarage(value);
            else if (action.equals(MOTION_SENSOR_GARDEN))
                handleMotionSensorGarden(value);
            else if (action.equals(LockModel.ACTION_LOCK))
                handleLockAction(intent.getExtras());
            else if( action.equals( LogPreferenceFragment.SYSTEMSERVICE))
                handleSystemServiceCmd( intent.getStringExtra( SystemModel.KEY_MESSAGE));
            else if( action.startsWith(MainListAdapter.ACTION_CHANGED_IN_LIST_MODUL + MainActivity.SOUND))
                handleOnShowOrHideOnModulSoundInList( intent.getBooleanExtra( MainListAdapter.KEY_SHOW_IN_LIST, false));
        }

        private void handleOnShowOrHideOnModulSoundInList( boolean showInList) {
            boolean playRemote = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(SoundPreferenceFragment2.SOUNDSWITCH, false);
            mMediaRouteButton.setVisibility( playRemote && showInList ? View.VISIBLE : View.INVISIBLE);
        }

        private void handleMotionSensorGarage(String value) {
            if (value != null) {
                if (value.equals("ON")) {
                    Log.d(TAG, "handleMotionSensorGarage ON");
                    mMotionImageView.setBackgroundResource(R.drawable.motion_garage);
                    mMotionImageView.setVisibility(View.VISIBLE);
                } else if (value.equals("OFF")) {
                    Log.d(TAG, "handleMotionSensorGarage OFF");
                    mMotionImageView.setVisibility(View.INVISIBLE);
                }
            }
        }

        private void handleMotionSensorGarden(String value) {
            if (value != null) {
                if (value.equals("ON")) {
                    Log.d(TAG, "handleMotionSensorGarden ON");
                    mMotionImageView.setBackgroundResource(R.drawable.motion_garden2);
                    mMotionImageView.setVisibility(View.VISIBLE);
                } else if (value.equals("OFF")) {
                    Log.d(TAG, "handleMotionSensorGarden OFF");
                    mMotionImageView.setVisibility(View.INVISIBLE);
                }
            }
        }

        private void handleLockAction(Bundle extras) {
            System.out.println ( "handleLockAction: " + extras.toString());

            if( extras.containsKey("bridgeConnected")) {
                mLockAndGoImageView.setVisibility( extras.getBoolean( "bridgeConnected", false) ? View.VISIBLE : View.INVISIBLE);
                if(extras.containsKey("info"))
                    Toast.makeText(getApplicationContext(),  extras.getString("info"), Toast.LENGTH_LONG).show();
            }

            if( extras.containsKey("lockActionId")) {
                int lockActionId = extras.getInt("lockActionId", -1);
                if( lockActionId == LockModel.CMD_UNLOCK || lockActionId == LockModel.CMD_LOCK) {
                    mLockProgress.setVisibility(View.INVISIBLE);
                } else if( lockActionId == LockModel.CMD_LOCK_N_GO || lockActionId == LockModel.CMD_LOCK_N_GO_WITH_UNLATCH) {
                    mLockAndGoProgress.setVisibility(View.INVISIBLE);
                    boolean activated = extras.getBoolean("success", false);
                    String notiTxt =  activated ? "Lock and go aktiviert" : extras.getString("failure", "unknown error");
                    Toast.makeText(getApplicationContext(), notiTxt, Toast.LENGTH_LONG).show();
                    if( activated) {
                        blinkLockNgoImage(true);
                    }
                }
            }

            if( extras.containsKey("success")) {
                if( ! extras.getBoolean("success")) {
                    if( extras.getBoolean("showFailure", false)) {
                        Toast.makeText(getApplicationContext(), extras.getString("shortText"), Toast.LENGTH_LONG).show();
                    }
                }
            }

            if( extras.containsKey("lockState")) {
                int lockState = extras.getInt("lockState");
                String lockStateText = extras.getString("lockStateText");
                int doorState = extras.getInt("doorState");
                String doorStateText = extras.getString("doorStateText");
             //   boolean someoneMoving = extras.getBoolean("someoneMoving");

                handleLockState(lockState);

                if( lockState == LockModel.LOCK_UNCALIBRATED || lockState >= 5) {
                    Toast.makeText(getApplicationContext(), lockStateText, Toast.LENGTH_LONG).show();
                }

              //  mLockImageView.setVisibility(someoneMoving ? View.VISIBLE : View.INVISIBLE);

                // door is open
                /*
                if( lockState == LockModel.LOCK_UNLOCKED && doorState == LockModel.DOOR_OPENED) {
                    //mLockImageView.setBackgroundResource( R.drawable.door_state_open);
                    showDoorOpen();
                    mLockImageView.setTag( new Integer(LockModel.LOCK_UNLOCKED)); // workaround when door state is wrong
                    mLockImageAniView.setTag(new Integer(LockModel.LOCK_UNLOCKED)); // workaround when door state is wrong
                } */
            }

            if( extras.containsKey("someoneMoving")) {
                mLockImageAniView.setVisibility(extras.getBoolean("someoneMoving") ? View.VISIBLE : View.INVISIBLE);
            }
        }

        private void handleLockState( int newState) {
            int currIconState = mLockImageAniView.getTag() == null ? -1 : ((Integer)mLockImageAniView.getTag()).intValue();  // icon states: -1 = Invisible, LockModel.LOCK_UNLOCKED, LockModel.LOCK_LOCKED
            int newIconState = getSimpleLockState(newState);

            System.out.println( "handleLockAction State: currIconState: " + currIconState + " newIconState: " + newIconState);
            if( currIconState != newIconState) {
                mLockImageAniView.setTag( new Integer(newIconState));
                if( newIconState == LockModel.LOCK_UNLOCKED) {
                    mLockImageAniView.setVisibility(View.VISIBLE);
                    mLockImageAniView.setSpeed(1);
                    mLockImageAniView.playAnimation();
                } else if( newIconState == LockModel.LOCK_LOCKED) {
                    mLockImageAniView.setVisibility(View.VISIBLE);
                    mLockImageAniView.setSpeed(-1);  // show locked
                    mLockImageAniView.playAnimation();
                } else {
                    mLockImageAniView.setVisibility(View.INVISIBLE);
                }
            }
        }

        private int getSimpleLockState(int lockState) {
            if( lockState == LockModel.LOCK_UNLOCKED || lockState == LockModel.LOCK_UNLOCKING || lockState == LockModel.LOCK_UNLOCKED_LOCKNGO || lockState == LockModel.LOCK_UNLATCHED || lockState == LockModel.LOCK_UNLATCHING) {
                return LockModel.LOCK_UNLOCKED;
            } else if( lockState == LockModel.LOCK_LOCKED || lockState == LockModel.LOCK_LOCKING) {
                return LockModel.LOCK_LOCKED;
            } else
                return -1;
        }

        private void blinkLockNgoImage( boolean status) {

            if( ! mLockNgoActive) {
                Timer blinkLockNgoTimer = new Timer();
                mLockAndGoImageView.setVisibility( View.VISIBLE);
                blinkLockNgoTimer.schedule( new TimerTask() {
                    int cnt = 0;
                    public void run() {
                        try {
                            mLockAndGoImageView.setBackgroundResource(cnt % 2 == 0 ? R.drawable.lockngo_toggle1 : R.drawable.lockngo_toggle2);
                            cnt++;
                            if(cnt >= 60) {
                                mLockAndGoImageView.setBackgroundResource(R.drawable.lockngo);
                                mLockNgoActive = false;
                                cancel();
                            }
                        } catch (Exception ex) {
                            System.out.println("error running thread " + ex.getMessage());
                            mLockNgoActive = false;
                        }
                    }
                }, 0,500);
            }
            mLockNgoActive = status;
        }

        private void handleSystemServiceCmd( String cmd) {
            Log.w(TAG, "received sys cmd: " + cmd);
            if(cmd.equals( "1")) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initMainList();
                       // mMainListView.setAdapter(mMainListAdapter);
                        Log.w(TAG, "run sys cmd with delay: initMainList();");
                    }
                }, 5000);
            }
        }
    }

}