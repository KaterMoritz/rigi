package biz.kindler.rigi.modul.weather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import biz.kindler.rigi.ItemManager2;
import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.BaseModel;
import biz.kindler.rigi.modul.system.Log;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 27.11.16.
 */

public class WeatherModel extends BaseModel {

    private final static String     TAG = WeatherModel.class.getSimpleName();

    private WeatherView             mView;

    // Item names
    private static final String     CONDITION_ID_TODAY          = "CommonIdToday";
    private static final String     TEMPERATUR_TODAY            = "TemperatureToday";
    private static final String     TEMPERATUR_TOMORROW         = "TemperatureTomorrow";
    private static final String     CONDITION_ID_TOMORROW       = "CommonIdTomorrow";
    private static final String     TEMPERATUR_AFTER_TOMORROW   = "TemperatureAfterTomorrow";
    private static final String     CONDITION_ID_AFTER_TOMORROW = "CommonIdAfterTomorrow";


    public WeatherModel(Context ctx) {
        super(ctx, MainActivity.WEATHER);

        mView = new WeatherView( ctx);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONDITION_ID_TODAY);
        intentFilter.addAction(TEMPERATUR_TODAY);
        intentFilter.addAction(TEMPERATUR_TOMORROW);
        intentFilter.addAction(CONDITION_ID_TOMORROW);
        intentFilter.addAction(TEMPERATUR_AFTER_TOMORROW);
        intentFilter.addAction(CONDITION_ID_AFTER_TOMORROW);
        ctx.registerReceiver(this, intentFilter);
    }

    @Override
    protected void initItems() {
        sendItemReadBroadcast( CONDITION_ID_TODAY);
        sendItemReadBroadcast( TEMPERATUR_TODAY);
        sendItemReadBroadcast( TEMPERATUR_TOMORROW);
        sendItemReadBroadcast( CONDITION_ID_TOMORROW);
        sendItemReadBroadcast( TEMPERATUR_AFTER_TOMORROW);
        sendItemReadBroadcast( CONDITION_ID_AFTER_TOMORROW);
    }

    private void handleItemActions( String itemName, String itemState) {
        if (itemName.equals(CONDITION_ID_TODAY)) {
            mView.setImage(WeatherView.TODAY, getImageFromItemState(itemState));
            mView.setConditionText(WeatherView.TODAY, getConditionTranslatedFromItemState( itemState));
        }
        else if (itemName.equals(TEMPERATUR_TODAY))
            mView.setTemperatur(WeatherView.TODAY, getTemperaturFromItemState( itemState));
        else if (itemName.equals(TEMPERATUR_TOMORROW))
            mView.setTemperatur(WeatherView.TOMORROW, getTemperaturFromItemState( itemState));
        else if (itemName.equals(CONDITION_ID_TOMORROW))
            mView.setImage(WeatherView.TOMORROW, getImageFromItemState(itemState));
        else if (itemName.equals(TEMPERATUR_AFTER_TOMORROW))
            mView.setTemperatur(WeatherView.AFTER_TOMORROW, getTemperaturFromItemState( itemState));
        else if (itemName.equals(CONDITION_ID_AFTER_TOMORROW))
            mView.setImage(WeatherView.AFTER_TOMORROW, getImageFromItemState(itemState));
    }

    private int getImageFromItemState( String itemState) {
        return getPrefs().getInt( WeatherView.PREFS_PREFIX_WEATHER_IMG + itemState, R.drawable.unknown);
    }

    private String getTemperaturFromItemState( String itemState) {
        return itemState == null || itemState.length() == 0 ? "-°C" : getTempRound(itemState) + "°C";
    }

    private String getConditionTranslatedFromItemState( String itemState) {
        return itemState == null || itemState.length() == 0 ? "-" : getPrefs().getString( WeatherView.PREFS_PREFIX_WEATHER_TXT + itemState, "?");
    }

    private String getTempRound(String temp) {
        try {
            Double d = Double.parseDouble(temp);
            int rounded = (int) Math.round(d);
            return String.valueOf(rounded);
        } catch (Exception ex) {
            Log.w(TAG, "getTempRound (" + temp + ") exc: " + ex.getMessage());
            return "";
        }
    }

    protected SharedPreferences getPrefs() {
        return getContext().getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String itemName = intent.getAction();
        String itemState = intent.getStringExtra(ItemManager2.VALUE);

        Log.d(TAG, "action: " + itemName + ", value: " + (itemState == null ? "null" : itemState));

        handleItemActions( itemName, itemState);
    }
}
