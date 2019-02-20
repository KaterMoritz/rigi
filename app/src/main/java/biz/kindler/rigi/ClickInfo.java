package biz.kindler.rigi;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 24.10.16.
 */

public class ClickInfo {

    int     modulId;
    int     buttonNr;
    String  btnTxt;
    int     rowIdx;

    public ClickInfo( int modulId, int buttonNr, String btnTxt, int rowIdx) {
        this.modulId = modulId;
        this.buttonNr = buttonNr;
        this.btnTxt = btnTxt;
        this.rowIdx = rowIdx;
    }

    public int getModulId() {
        return modulId;
    }

    public int getButtonNr() {
        return buttonNr;
    }

    public String getButtonText() {
        return btnTxt;
    }

    public int getRowIdx() {
        return rowIdx;
    }

    public String toString() {
        return "modulId:" + modulId + ",buttonNr:" + buttonNr + ",btnTxt:" + btnTxt + ",rowIdx:" + rowIdx;
    }
}
