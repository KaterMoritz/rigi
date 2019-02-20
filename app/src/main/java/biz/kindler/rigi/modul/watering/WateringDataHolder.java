package biz.kindler.rigi.modul.watering;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.OneButtonDataHolder;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 23.06.17.
 */

public class WateringDataHolder extends OneButtonDataHolder {

    public WateringDataHolder() {
        super("Bewässerung", MainActivity.WATERING, R.drawable.geranientopf);
        setAltImgResId( R.drawable.giesskanne);
        setInfo( "-");
        setButtonInfo( "");
        setButtonText( WateringModel.BUTTON_ON_TEXT);
    }
}