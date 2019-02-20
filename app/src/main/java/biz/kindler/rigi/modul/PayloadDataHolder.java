package biz.kindler.rigi.modul;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 24.10.16.
 */

public class PayloadDataHolder {

    private int      mId;
    private String   mText;

    public PayloadDataHolder( int id, String data) {
        mId = id;
        mText = data;
    }

    public int getId() {
        return mId;
    }

    public String getText() {
        return mText;
    }

}
