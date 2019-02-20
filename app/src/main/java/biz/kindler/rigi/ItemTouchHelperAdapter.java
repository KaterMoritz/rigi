package biz.kindler.rigi;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 30.10.16.
 */

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
