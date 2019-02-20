package biz.kindler.rigi;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 21.10.16.
 */

public interface DataHolder extends Comparable {

    int getType();

    int getModulId();

   // Date getSortDate();

   // void setSortDateNow();

    void setPos( int pos);

    //int getPos();

    void setVisible( boolean status);

    boolean getVisible();

    void setUpdateAll( boolean status);

    boolean getUpdateAll();
}
