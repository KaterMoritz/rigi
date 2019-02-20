package biz.kindler.rigi.modul.garage;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.OneButtonDataHolder;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 10.11.16.
 */

public class GarageDataHolder extends OneButtonDataHolder {

    public GarageDataHolder() {
        super( "Garage", MainActivity.GARAGE, R.drawable.garage);
        setInfo( "-");
        setButtonInfo( "");
        setButtonText( "öffnen");
    }
}
