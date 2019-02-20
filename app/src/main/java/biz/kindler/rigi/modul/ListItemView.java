package biz.kindler.rigi.modul;

import biz.kindler.rigi.R;

/**
 * Created by patrick kindler (katermoritz100@gmail.com)
 */

public class ListItemView {

    private String title;
    private int modul;
    private int type;
    private int imgResId;

    public ListItemView( String title, int modul, int type, int imgResId) {
        this.title = title;
        this.modul = modul;
        this.type = type;
        this.imgResId = imgResId;
    }

    public String getTitle() {
        return title;
    }

    public int getModul() {
        return modul;
    }

    public int getType() {
        return type;
    }

    public int getImageResId() {
        return imgResId;
    }
}
