package biz.kindler.rigi.modul.sound;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.OneButtonDataHolder;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 15.08.17.
 */

public class SoundDataHolder extends OneButtonDataHolder {

    public SoundDataHolder() {
        super( "Musik", MainActivity.SOUND, R.drawable.music);
        setInfo( "");
        setButtonInfo( "");
        setButtonText( "Play");
    }
}