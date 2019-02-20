package biz.kindler.rigi.modul.forecourt;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.OneButtonDataHolder;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 21.12.16.
 */

public class ForecourtDataHolder extends OneButtonDataHolder {

    public ForecourtDataHolder() {
        super("Aussenlicht", MainActivity.FORECOURT, R.drawable.aussenlicht);
        setInfo( "-");
        setButtonInfo( ForecourtModel.BUTTON_DEFAULT_TEXT);
        setButtonText( ForecourtModel.BUTTON_ON_TEXT);
    }
}

