package biz.kindler.rigi.modul.weather;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ImageView;
import android.widget.TextView;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 27.11.16.
 */

public class WeatherView {

    public static final String      PREFS_PREFIX_WEATHER_TXT        = "weatherTxt-";
    public static final String      PREFS_PREFIX_WEATHER_IMG        = "weatherImg-";

    public static final int         TODAY           = 0;
    public static final int         TOMORROW        = 1;
    public static final int         AFTER_TOMORROW  = 2;

    private Context                 mCtx;
    private SharedPreferences       mPrefs;
    private TextView                mWeatherTodayTemp;
    private TextView                mWeatherTomorrowTemp;
    private TextView                mWeatherAfterTomorrowTemp;
    private TextView                mWeatherTodayText;
    private ImageView               mWeatherTodayImage;
    private ImageView               mWeatherTomorrowImage;
    private ImageView               mWeatherAfterTomorrowImage;

    public WeatherView( Context ctx) {
        mCtx = ctx;
        Activity activity = (Activity)ctx;

        putWeatherIDsToProperties();
        putWeatherImagesToProperties();

        // Weather today
        mWeatherTodayText = (TextView) activity.findViewById(R.id.weather_today_text);
        mWeatherTodayTemp = (TextView) activity.findViewById(R.id.weather_today_temperatur);
        mWeatherTodayImage = (ImageView) activity.findViewById(R.id.weather_today_image);
        // Weather tomorrow
        mWeatherTomorrowTemp = (TextView) activity.findViewById(R.id.weather_tomorrow_temperatur);
        mWeatherTomorrowImage = (ImageView) activity.findViewById(R.id.weather_tomorrow_image);
        // Weather after tomorrow
        mWeatherAfterTomorrowTemp = (TextView) activity.findViewById(R.id.weather_aftertomorrow_temperatur);
        mWeatherAfterTomorrowImage = (ImageView) activity.findViewById(R.id.weather_aftertomorrow_image);
    }

    public void setImage( final int when, int resId) {
        switch( when) {
            case TODAY : mWeatherTodayImage.setImageResource(resId); break;
            case TOMORROW : mWeatherTomorrowImage.setImageResource(resId); break;
            case AFTER_TOMORROW : mWeatherAfterTomorrowImage.setImageResource(resId); break;
        }
    }

    public void setTemperatur( int when, String temperatur) {
        switch( when) {
            case TODAY : mWeatherTodayTemp.setText(temperatur); break;
            case TOMORROW : mWeatherTomorrowTemp.setText(temperatur); break;
            case AFTER_TOMORROW : mWeatherAfterTomorrowTemp.setText(temperatur); break;
        }
    }

    public void setConditionText( int when, String conditionText) {
        if( when == TODAY)
            mWeatherTodayText.setText( conditionText);
    }

    protected SharedPreferences getPrefs() {
        if( mPrefs == null)
            mPrefs = mCtx.getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE);
        return mPrefs;
    }

    private void putWeatherIDsToProperties() {
        SharedPreferences.Editor editor = getPrefs().edit();

        editor.putString( PREFS_PREFIX_WEATHER_TXT + "thunder", "Gewitter");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "storm", "Sturm");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "rain-and-snow", "Regen und Schnee");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "rain-and-sleet", "Regen und Schneeregen");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "snow-and-sleet", "Schnee und Schneeregen");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "freezing-drizzle", "Gefrierender Nieselregen");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "few-showers", "Nieselregen");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "freezing-rain", "Gefrierender Regen");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "rain", "Regen");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "snow-flurries", "Schneegestöber");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "light-snow", "Leichter Schneeschauer");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "blowing-snow", "Schneetreiben");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "snow", "Starker Schneefall");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "sleet", "Schneeregen");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "dust", "Staub");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "fog", "Neblig");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "wind", "Stürmisch");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "cold", "Kalt");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "cloudy", "Bewölkt");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "mostly-cloudy-night", "überwiegend bewölkt");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "mostly-cloudy-day", "überwiegend bewölkt");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "partly-cloudy-night", "teilweise bewölkt");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "partly-cloudy-day", "teilweise bewölkt");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "clear-night", "Klare Nacht");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "sunny", "Sonnig");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "hot", "Heiss");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "scattered-thunder", "Vereinzelte Gewitter");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "scattered-showers", "Vereinzelt Niederschlag");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "thundershowers", "Gewitterregen");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "snow-showers", "Schneefall");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "scattered-thundershowers", "Vereinzelter Gewitterregen");
        editor.putString( PREFS_PREFIX_WEATHER_TXT + "unknown", "Unbekannt");
        editor.commit();
    }

    private void putWeatherImagesToProperties() {
        SharedPreferences.Editor editor = getPrefs().edit();

        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "thunder", R.drawable.thunder);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "storm",  R.drawable.storm);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "rain-and-snow", R.drawable.rain_and_snow);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "rain-and-sleet", R.drawable.rain_and_sleet);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "snow-and-sleet", R.drawable.snow_and_sleet);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "freezing-drizzle", R.drawable.freezing_drizzle);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "few-showers", R.drawable.few_showers);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "freezing-rain", R.drawable.freezing_rain);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "rain", R.drawable.rain);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "snow-flurries", R.drawable.snow_flurries);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "light-snow", R.drawable.light_snow);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "blowing-snow", R.drawable.blowing_snow);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "snow", R.drawable.snow);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "sleet", R.drawable.sleet);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "dust", R.drawable.dust);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "fog", R.drawable.fog);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "wind", R.drawable.wind);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "cold", R.drawable.cold);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "cloudy", R.drawable.cloudy);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "mostly-cloudy-night", R.drawable.mostly_cloudy_night);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "mostly-cloudy-day", R.drawable.mostly_cloudy_day);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "partly-cloudy-night", R.drawable.partly_cloudy_night);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "partly-cloudy-day", R.drawable.partly_cloudy_day);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "clear-night", R.drawable.clear_night);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "sunny", R.drawable.sunny);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "hot", R.drawable.hot);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "scattered-thunder", R.drawable.scattered_thunder);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "scattered-showers", R.drawable.scattered_showers);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "thundershowers", R.drawable.thundershower);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "snow-showers", R.drawable.snow_showers);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "scattered-thundershowers", R.drawable.scattered_thundershowers);
        editor.putInt( PREFS_PREFIX_WEATHER_IMG + "unknown", R.drawable.unknown);
        editor.commit();
    }
}
