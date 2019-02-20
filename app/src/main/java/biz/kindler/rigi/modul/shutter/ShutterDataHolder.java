package biz.kindler.rigi.modul.shutter;


import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.TwoButtonDataHolder;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 10.11.16.
 */

public class ShutterDataHolder extends TwoButtonDataHolder {

    public ShutterDataHolder() {
        super( "Storen", MainActivity.SHUTTER, R.drawable.blinds);
        setInfo( "-");
        setButton1Info( "Ost + Süd");
        setButton2Info( "Garten");

        setButtonVisible(B1, false);
        setButtonVisible(B11, true);
        setButtonVisible(B12, true);
        setButtonVisible(B2, false);
        setButtonVisible(B21, true);
        setButtonVisible(B22, true);

        setButtonText(B11, ShutterModel.BUTTON_UP_TEXT);
        setButtonText(B12, ShutterModel.BUTTON_DOWN_TEXT);
        setButtonText(B21, ShutterModel.BUTTON_UP_TEXT);
        setButtonText(B22, ShutterModel.BUTTON_DOWN_TEXT);
    }
}
