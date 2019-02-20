package biz.kindler.rigi.modul.vbl;

import android.content.Context;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.RigiApplication;
import biz.kindler.rigi.modul.ThreeLinesDataHolder;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 10.11.16.
 */

public class VBLDataHolder extends ThreeLinesDataHolder {

    public VBLDataHolder() {
        super( "Fahrplan " + VBLModel2.getFromTitle(RigiApplication.getAppContext()) + " - " + VBLModel2.getToTitle(RigiApplication.getAppContext()), new String[] {"-", "", ""}, new String[] {"-", "", ""}, new String[] {"-", "", ""}, MainActivity.VBL, R.drawable.vbl);
    }
}
