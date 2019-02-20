package biz.kindler.rigi.modul.entree;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.OneButtonDataHolder;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 10.11.16.
 */

public class EntreeDataHolder extends OneButtonDataHolder {

    public EntreeDataHolder() {
        super("Ankleide", MainActivity.ENTREE, R.drawable.ankleide);
        setInfo( "automatik");
        setButtonInfo( EntreeModel.BUTTON_DEFAULT_TEXT);
        setButtonText( EntreeModel.BUTTON_ON_TEXT);
    }
}
