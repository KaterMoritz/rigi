package biz.kindler.rigi.modul.sonnerie;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.OneButtonDataHolder;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 29.06.17.
 */

public class SonnerieDataHolder extends OneButtonDataHolder {

    public SonnerieDataHolder() {
        super( "Haustür", MainActivity.SONNERIE, R.drawable.sonnerie);
        setInfo( "");
        setButtonInfo( "");
        setButtonText( "Aufnahmen");
    }
}
