package biz.kindler.rigi.modul.sound;

import android.content.Context;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;

import java.util.List;

import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 08.01.19
 */
public class CastOptionsProvider implements OptionsProvider {
    @Override
    public CastOptions getCastOptions(Context context) {
       // CastOptions castOptions = new CastOptions.Builder().setReceiverApplicationId(context.getString(R.string.app_id)).build();
        CastOptions castOptions = new CastOptions.Builder().setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID).build();
        // see: https://developers.google.com/cast/v2/receiver_apps#default
        // see: https://stackoverflow.com/questions/43533703/how-do-you-find-your-google-cast-app-id-app-id-in-the-2017-google-play-develop
        return castOptions;
    }
    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}