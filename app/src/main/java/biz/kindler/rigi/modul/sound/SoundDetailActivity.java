package biz.kindler.rigi.modul.sound;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import biz.kindler.rigi.R;
import biz.kindler.rigi.Util;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.settings.SoundPreferenceFragment2;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 25.08.17.
 */

public class SoundDetailActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static final String STATION                      = "station";
    public static final String MY_FAV_STATION_IDS           = "myfavs";
    public static final String VOLUME_LOCAL                 = "volume-local";
    public static final String VOLUME_REMOTE                = "volume-remote";

    private final static String TAG = SoundDetailActivity.class.getSimpleName();
    private static final String STATION_LIST_URL            = "http://api.shoutcast.com/legacy/Top500?k=[APIKEY]";
    private static final String LIST_FAVORITES              = "list-favorites";
    private static final String LIST_ALL                    = "list-all";
    private static final String STATION_LIST_RAW_DATA       = "station-list-raw-data";
    private static final String STATION_LIST_UPDATED_DAY    = "station-list-udated-dayofyear";


    private ListView            mStationListView;
    private TextView            mInfoView;
    private StationListAdapter  mStationListAdapter;
    private String              mFavStationIds;
    private Button              mFavBtn;
    private Button              mAllBtn;
    private SeekBar             mVolumeSeekBar;
    private ProgressBar         mProgressBar;
    private AudioManager        mAudioMgr;
    private String              mSelectedStationId;
    private String              mInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_detail);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle( "Radio Stationen");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mVolumeSeekBar = findViewById(R.id.volumeSeekBar);
        mVolumeSeekBar.setOnSeekBarChangeListener(this);

        if( isPlayLocal()) {
            mAudioMgr = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mVolumeSeekBar.setMax(mAudioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            mVolumeSeekBar.setProgress(Integer.parseInt(getPrefs().getString(VOLUME_LOCAL, "50")));
        } else
            mVolumeSeekBar.setProgress(Integer.parseInt((getPrefs().getString(VOLUME_REMOTE, "50"))));

        mFavBtn = findViewById(R.id.favorites_btn);
        mFavBtn.setBackgroundResource( R.drawable.round_border_yellow);
        mFavBtn.setTag( LIST_FAVORITES);
        mFavBtn.setOnClickListener( this);

        mAllBtn = findViewById(R.id.all_btn);
        mAllBtn.setBackgroundResource( R.drawable.round_border_dark);
        mAllBtn.setTag( LIST_ALL);
        mAllBtn.setOnClickListener( this);

        mStationListView = findViewById(R.id.stationlist);
        mStationListAdapter = new StationListAdapter();
        mStationListView.setAdapter(mStationListAdapter);
        mStationListView.setOnItemClickListener(this);

        mInfoView = findViewById( R.id.infotext);

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility( View.INVISIBLE);

        mFavStationIds = getPrefs().getString( MY_FAV_STATION_IDS, "1702206,489878,1272062,19209,");

        mSelectedStationId = getPrefs().getString(Station.ID, "1702206");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private Context getContext() {
        return this;
    }

    protected SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences( getContext());
    }

    private boolean isPlayLocal() {
        return ! getPrefs().getBoolean(SoundPreferenceFragment2.SOUNDSWITCH, false);
    }

    protected void storePrefsString(String prefId, String value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString( prefId, value);
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        mInfoView.setVisibility( View.INVISIBLE);

        String apiKey = getPrefs().getString( SoundPreferenceFragment2.SHOUTCAST_API_KEY, "");
        if( apiKey == null || apiKey.trim().length() == 0) {
            mInfoView.setVisibility( View.VISIBLE);
            mInfoView.setText( "no api key");
        }
        else {
            mProgressBar.setVisibility( View.VISIBLE);
            String stationListURLWithApiKey = STATION_LIST_URL.replace("[APIKEY]", apiKey);
            new GetStationListTask().execute(stationListURLWithApiKey);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        System.out.println( "i:" + i + ",l:" + l);
        if( view instanceof ImageView) {
            System.out.println ("ImageView");
        } else {
            Intent returnIntent = new Intent();
            Station selectedStation = (Station) mStationListAdapter.getItem(i);
            returnIntent.putExtra(Station.NAME, selectedStation.getName());
            returnIntent.putExtra(Station.ID, selectedStation.getId());
            returnIntent.putExtra(Station.GENRE, selectedStation.getGenre());
            returnIntent.putExtra(Station.LOGO, selectedStation.getLogo());
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        Object tagObj = view.getTag();

        if( tagObj != null) {
            if(tagObj.equals( LIST_FAVORITES)) {
                mFavBtn.setBackgroundResource(R.drawable.round_border_yellow);
                mAllBtn.setBackgroundResource(R.drawable.round_border_dark);
                mStationListAdapter.showFavoritesOnly( true);
            }
            else if(tagObj.equals( LIST_ALL)) {
                mFavBtn.setBackgroundResource(R.drawable.round_border_dark);
                mAllBtn.setBackgroundResource(R.drawable.round_border_yellow);
                mStationListAdapter.showFavoritesOnly( false);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if( isPlayLocal()) {
            Log.i(TAG, "set local volume to: " + i);
            storePrefsString(VOLUME_LOCAL, String.valueOf(i));
            mAudioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
        } else {
            Log.i(TAG, "set remote volume to: " + i);
            storePrefsString(VOLUME_REMOTE, String.valueOf(i));
            double newVal = (double)i/100;
            try {
                CastContext castContext = CastContext.getSharedInstance(getContext());
                SessionManager sessionMgr = castContext.getSessionManager();
                CastSession castSession = sessionMgr.getCurrentCastSession();
                if (castSession != null && castSession.isConnected())
                    castSession.setVolume(newVal);
            } catch(Exception ex) {
                Log.w(TAG, "setVolume to: " + i + " exc: " + ex.getMessage());
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private class GetStationListTask extends AsyncTask<String, Void, List<Station>> {

       // <station name="1.FM - Amsterdam Trance (www.1.fm)" mt="audio/mpeg" id="1620383" br="128" genre="Top 40" genre2="Dance" genre3="Trance" genre4="Top 40" logo="http://i.radionomy.com/document/radios/4/4884/48849868-2fd0-4c71-8b05-78ea74e6fc9e.jpg" lc="922"/>

        public static final String STATION = "station";
        public static final String NAME = "name";
        public static final String MT = "mt";
        public static final String ID = "id";
        public static final String BR = "br";
        public static final String GENRE = "genre";
        public static final String GENRE2 = "genre2";
        public static final String GENRE3 = "genre3";
        public static final String GENRE4 = "genre4";
        public static final String GENRE5 = "genre5";
        public static final String LOGO = "logo";
        public static final String CT = "ct";
        public static final String LC = "lc";

        @Override
        protected List<Station> doInBackground(String... param) {
            String url = param[0];

            List<Station> stationList = new ArrayList<Station>();
            Station station = null;
            mInfoText = null;

            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();

                if( needUpdateListFromWeb()) {
                    InputStream streamFromUrl = new URL(url).openConnection().getInputStream();
                    try {
                        storePrefsString(STATION_LIST_RAW_DATA, Util.inputStreamToString(streamFromUrl));
                        storePrefsString(STATION_LIST_UPDATED_DAY, String.valueOf( Util.getDayOfYearToday()));
                    } catch(Exception ex) {
                        Log.w(TAG, "failed to store station list to cache");
                        mInfoText = "Station list not up to date";
                    }
                }

                try {
                    String stationListRawString = getPrefs().getString(STATION_LIST_RAW_DATA, null);
                    InputStream streamFromSettings = Util.stringToInputStream( stationListRawString);
                    parser.setInput( streamFromSettings, null);
                } catch(Exception ex) {
                    Log.w(TAG, "failed to load station list from cache");
                    mInfoText = "Station list not up to date";
                }

                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagname = parser.getName();
                    switch (eventType) {
                        case XmlPullParser.START_TAG:
                            if (tagname.equalsIgnoreCase(STATION)) {
                                station = new Station();
                                station.setName(parser.getAttributeValue(null, NAME));
                                station.setMt(parser.getAttributeValue(null, MT));
                                station.setId(parser.getAttributeValue(null, ID));
                                station.setBr(parser.getAttributeValue(null, BR));
                                station.setGenre(parser.getAttributeValue(null, GENRE));
                                station.setGenre2(parser.getAttributeValue(null, GENRE2));
                                station.setGenre3(parser.getAttributeValue(null, GENRE3));
                                station.setGenre4(parser.getAttributeValue(null, GENRE4));
                                station.setGenre5(parser.getAttributeValue(null, GENRE5));
                                station.setLogo(parser.getAttributeValue(null, LOGO));
                                station.setCt(parser.getAttributeValue(null, CT));
                                station.setLc(parser.getAttributeValue(null, LC));
                                if( mFavStationIds.contains( station.getId()))
                                    station.setFavorite(true);
                                if( station.getId().equals( mSelectedStationId))
                                    station.setSelected( true);

                                break;
                            }
                        case XmlPullParser.END_TAG:
                            if (tagname.equalsIgnoreCase(STATION))
                                stationList.add(station);
                            break;

                        default:
                            break;
                    }
                    eventType = parser.next();
                }

            } catch (Exception e) {
                Log.w(TAG, e.getMessage());
                mInfoText = "Station list loading failed";
            }

            return stationList;
        }

        @Override
        protected void onPostExecute( List<Station> list) {
            mStationListAdapter.setAllStations(list);
            mStationListAdapter.showFavoritesOnly( true);
            mProgressBar.setVisibility( View.INVISIBLE);
            if( mInfoText != null && mInfoText.length() > 0) {
                mInfoView.setVisibility( View.VISIBLE);
                mInfoView.setText( mInfoText);
            }
            else if(list.size() == 0) {
                mInfoView.setVisibility( View.VISIBLE);
                mInfoView.setText( "no stations");
            }
        }

        private boolean needUpdateListFromWeb() {
            int dayStored = Integer.parseInt(getPrefs().getString(STATION_LIST_UPDATED_DAY, "0"));
            int today = Util.getDayOfYearToday();
            return dayStored != today;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    private class StationListAdapter extends BaseAdapter implements View.OnClickListener {
        private List<Station> stationList;
        private List<Station> favList;
        private boolean showOnlyFavorites;

        public StationListAdapter() {
            favList = new ArrayList<>();
        }

        public void setAllStations(List<Station> stationList) {
            this.stationList = stationList;
        }

        @Override
        public int getCount() {
            if( showOnlyFavorites)
                return favList == null ? 0 : favList.size();
            else
                return stationList == null ? 0 : stationList.size();
        }

        @Override
        public Object getItem(int position) {
            if( showOnlyFavorites)
                return favList == null ? null : favList.get(position);
            else
                return stationList == null ? null : stationList.get(position);
        }

        @Override
        public long getItemId(int position) {
            if( showOnlyFavorites)
                return favList == null ? -1 : favList.get(position).getIdAsLong();
            else
                return stationList == null ? -1 : stationList.get(position).getIdAsLong();
        }

        public void showFavoritesOnly( boolean status) {
            showOnlyFavorites = status;
            if( status)
                buildFavoritesList();

            notifyDataSetChanged();
        }

        public boolean showFavoritesOnly() {
            return showOnlyFavorites;
        }

        private void buildFavoritesList() {
            favList.clear();
            if( stationList != null) {
                Iterator<Station> iter = stationList.iterator();
                while (iter.hasNext()) {
                    Station station = iter.next();
                    if (station.getFavorite())
                        favList.add(station);
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout layout;
            Station station = showOnlyFavorites ? favList.get(position) : stationList.get(position);

            if (convertView == null)
                layout = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.station_list_item, parent, false);
            else
                layout = (RelativeLayout) convertView;

            TextView stationName = layout.findViewById(R.id.stationName);
            stationName.setText( station.getName());

            TextView stationGenre = layout.findViewById(R.id.stationGenre);
            stationGenre.setText( station.getGenre());

            ImageView favorite = layout.findViewById(R.id.favorite);
            favorite.setVisibility( station.getFavorite() ? View.VISIBLE : View.INVISIBLE);
            favorite.setOnClickListener( this);
            favorite.setTag( station);

            ImageView noFavorite = layout.findViewById(R.id.not_favorite);
            noFavorite.setVisibility( station.getFavorite() ? View.INVISIBLE : View.VISIBLE);
            noFavorite.setOnClickListener( this);
            noFavorite.setTag( station);

            layout.setBackgroundResource( station.getSelected() ? R.drawable.round_border_yellow : R.drawable.round_border_dark);

            return layout;
        }

        @Override
        public void onClick(View view) {
            Object tagObj = view.getTag();
            if( tagObj != null && tagObj instanceof Station) {
                Station currStation = (Station)tagObj;
                boolean addFav = view.getId() == R.id.not_favorite;
                currStation.setFavorite( addFav);
                notifyDataSetChanged();
                if( addFav)  // add favorite
                    mFavStationIds = mFavStationIds + currStation.getId() + ",";
                else // remove favorite
                    mFavStationIds = mFavStationIds.replace( currStation.getId() + ",", "");

                // store in settings
                storePrefsString( MY_FAV_STATION_IDS, mFavStationIds);

                // if showing favorites list only
                if( mStationListAdapter.showFavoritesOnly()) {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            mStationListAdapter.showFavoritesOnly(true); // rebuild favorites list
                        }
                    }, 1000);
                }
            }
        }
    }

}
