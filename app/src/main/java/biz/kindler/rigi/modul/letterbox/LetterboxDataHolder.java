package biz.kindler.rigi.modul.letterbox;

import biz.kindler.rigi.MainActivity;
import biz.kindler.rigi.R;
import biz.kindler.rigi.modul.OneButtonDataHolder;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 10.11.16.
 */

public class LetterboxDataHolder extends OneButtonDataHolder {

    public LetterboxDataHolder() {
        super( "Briefkasten", MainActivity.LETTERBOX, R.drawable.letterbox);
        setAltImgResId( R.drawable.mail_notification);
        setInfo( "keine Postmeldung");
        setButtonInfo( "");
        setButtonText( "öffnen");
    }
}
