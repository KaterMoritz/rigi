package biz.kindler.rigi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biz.kindler.rigi.modul.OneButtonDataHolder;
import biz.kindler.rigi.modul.OneButtonViewHolder;
import biz.kindler.rigi.modul.ThreeLinesDataHolder;
import biz.kindler.rigi.modul.ThreeLinesViewHolder;
import biz.kindler.rigi.modul.TwoButtonDataHolder;
import biz.kindler.rigi.modul.TwoButtonViewHolder;
import biz.kindler.rigi.modul.TwoLinesDataHolder;
import biz.kindler.rigi.modul.TwoLinesViewHolder;
import biz.kindler.rigi.modul.system.Log;

/**
 *
 * Created by patrick kindler (katermoritz100@gmail.com)
 */
public class _MainListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    private final static String TAG = _MainListAdapter.class.getSimpleName();

    public static final String ACTION_UPDATE_LISTITEM = "action-update-listitem";
    public static final String ACTION_BUTTON_CLICK_MODUL = "action-button-touch-modulid-";
    public static final String ACTION_PANEL_CLICK_MODUL = "action-modul-touch-modulid-";
    //public static final String      KEY_POSITION                = "adapter-position";
    public static final String KEY_SHOW_IN_LIST = "show-in-list";
    public static final String KEY_SORT = "sort";
    public static final String KEY_MODUL_ID = "modul-id";
    public static final String KEY_BUTTON_NR = "button-nr";

    public static final String SORT = "sort";
    public static final String LIST = "list";  // show in main list


    private int lastPosition = -1;
    private Context mContext;
    private RecyclerView mView;
    private ArrayList<DataHolder> mInListDataHolderArr;
    private ArrayList<DataHolder> mNOTinListDataHolderArr;
    private View.OnClickListener mClickListener;
    private boolean mDoAnimateFirstRow;

    public _MainListAdapter(Context ctx, RecyclerView listView) {
        mContext = ctx;
        mView = listView;
        mInListDataHolderArr = new ArrayList<DataHolder>();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_LISTITEM);
        ctx.registerReceiver(new MyBroadcastReceicer(), intentFilter);
    }

    public void setAllData(ArrayList<DataHolder> dataHolder) {
        mNOTinListDataHolderArr = dataHolder;
    }

    public void setModulVisible(int modulId, boolean status, boolean setSortToNow) {
        for (DataHolder dataHolder : mNOTinListDataHolderArr) {
            if (dataHolder.getModulId() == modulId) {
                //dataHolder.setVisible(status);
                if (setSortToNow)
                    ;//dataHolder.setSortDateNow();

                checkModulVisibility(setSortToNow);
                return;
            }
        }

        for (DataHolder dataHolder : mInListDataHolderArr) {
            if (dataHolder.getModulId() == modulId) {
                //dataHolder.setVisible(status);
                if (setSortToNow)
                    ;//dataHolder.setSortDateNow();
            }
        }

        if (setSortToNow) {
            sort();
            notifyDataSetChanged();
        }
    }

    public void setModulSortNow(int modulId) {
        for (DataHolder dataHolder : mInListDataHolderArr) {
            if (dataHolder.getModulId() == modulId) {
                //dataHolder.setSortDateNow();
                notifyDataSetChanged();
                break;
            }
        }
        for (DataHolder dataHolder : mNOTinListDataHolderArr) {
            if (dataHolder.getModulId() == modulId) {
                //dataHolder.setSortDateNow();
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void checkModulVisibility(boolean sort) {
        /*
        for (DataHolder notInListDataHolder : mNOTinListDataHolderArr) {
            if (notInListDataHolder.getVisible() && !mInListDataHolderArr.contains(notInListDataHolder)) {
                mDoAnimateFirstRow = true;
                mInListDataHolderArr.add(notInListDataHolder);
            } else if (!notInListDataHolder.getVisible() && mInListDataHolderArr.contains(notInListDataHolder))
                mInListDataHolderArr.remove(notInListDataHolder);
        }

        for (DataHolder inListDataHolder : mInListDataHolderArr) {
            if (!inListDataHolder.getVisible() && !mNOTinListDataHolderArr.contains(inListDataHolder))
                mNOTinListDataHolderArr.add(inListDataHolder);
            else if (inListDataHolder.getVisible() && mNOTinListDataHolderArr.contains(inListDataHolder))
                mNOTinListDataHolderArr.remove(inListDataHolder);
        } */
        if (sort)
            sort();

        notifyDataSetChanged();

        /*
        if( doAnimateFirstRow) {
            RecyclerView.ViewHolder viewHolder = mView.findViewHolderForLayoutPosition( 0);
            if (viewHolder != null) {
                Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
                viewHolder.itemView.startAnimation(animation);
            }
        } */
    }

    public DataHolder getDataHolderAtPos(int pos) {
        return pos < mInListDataHolderArr.size() ? mInListDataHolderArr.get(pos) : null;
    }

    private void addDataHolderToList(DataHolder dataHolder) {
        mInListDataHolderArr.add(dataHolder);
        mNOTinListDataHolderArr.remove(dataHolder);
    }

    private void removeDataHolderFromList(DataHolder dataHolder) {
        mInListDataHolderArr.remove(dataHolder);
        mNOTinListDataHolderArr.add(dataHolder);
    }

    private DataHolder findModul(int modulId) {
        for (DataHolder dataHolder : mInListDataHolderArr) {
            if (dataHolder.getModulId() == modulId) {
                return dataHolder;
            }
        }
        for (DataHolder dataHolder : mNOTinListDataHolderArr) {
            if (dataHolder.getModulId() == modulId) {
                return dataHolder;
            }
        }
        return null;
    }

    public boolean updateDataHolder(DataHolder newDataHolder) {
        for (int cnt = 0; cnt < mInListDataHolderArr.size(); cnt++) {
            DataHolder currDataHolder = mInListDataHolderArr.get(cnt);
            if (currDataHolder.getModulId() == newDataHolder.getModulId()) {
                mInListDataHolderArr.set(cnt, newDataHolder);
                return true;
            }
        }
        for (int cnt = 0; cnt < mNOTinListDataHolderArr.size(); cnt++) {
            DataHolder currDataHolder = mNOTinListDataHolderArr.get(cnt);
            if (currDataHolder.getModulId() == newDataHolder.getModulId()) {
                mNOTinListDataHolderArr.set(cnt, newDataHolder);
                return true;
            }
        }
        return false;
    }

    public void sort() {
        Collections.sort(mInListDataHolderArr);
        // Collections.reverse(mInListDataHolderArr);
    }

    public void setClickListener(View.OnClickListener clickListener) {
        mClickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mInListDataHolderArr.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int type) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder view = null;

        switch (type) {
            case MainActivity.ONE_BUTTON_ITEM:
                view = new OneButtonViewHolder(layoutInflater.inflate(R.layout.one_button_item, parent, false));
                break;
            case MainActivity.TWO_BUTTON_ITEM:
                view = new TwoButtonViewHolder(layoutInflater.inflate(R.layout.two_button_item, parent, false));
                break;
            case MainActivity.TWO_LINES_ITEM:
                view = new TwoLinesViewHolder(layoutInflater.inflate(R.layout.two_lines_item, parent, false));
                break;
            case MainActivity.THREE_LINES_ITEM:
                view = new ThreeLinesViewHolder(layoutInflater.inflate(R.layout.three_lines_item, parent, false));
                break;
            case MainActivity.FOUR_BUTTON_ITEM:
                return null;// TODO new OneButtonViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.one_button_item, parent, false));
            case MainActivity.TEXT_ITEM:
                return null;// TODO new OneButtonViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.one_button_item, parent, false));
            default:
                return null;
        }
        if (view != null) {
            view.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object tagObj = v.getTag();
                    if (tagObj != null && tagObj instanceof Integer)
                        sendPanelTouchBroadcast((Integer) tagObj);
                }
            });
        }

        return view;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int pos, List<Object> payload) {
        if (payload != null && payload.size() > 0) {
            // DataHolder dataHolder = mInListDataHolderArr.get(pos);
            Object obj = payload.get(0);

            if (obj instanceof TwoLinesDataHolder) {
                TwoLinesDataHolder twoLinesDataHolder = (TwoLinesDataHolder) obj;
                TwoLinesViewHolder twoLinesViewHolder = (TwoLinesViewHolder) viewHolder;
                twoLinesViewHolder.updateViewComponent(R.id.title, twoLinesDataHolder.getLine1());
                twoLinesViewHolder.updateViewComponent(R.id.line2, twoLinesDataHolder.getLine2());
                twoLinesViewHolder.updateViewComponent(R.id.line2_center, twoLinesDataHolder.getLine2Center());
                twoLinesViewHolder.updateViewComponent(R.id.line2_right, twoLinesDataHolder.getLine2Right());
                twoLinesViewHolder.updateViewComponent(R.id.icon, String.valueOf(twoLinesDataHolder.getImgResId()));
                twoLinesViewHolder.setComponentVisibility(R.id.line2_center, twoLinesDataHolder.getLine2Center().length() > 0);
                twoLinesViewHolder.setComponentVisibility(R.id.line2_right, twoLinesDataHolder.getLine2Right().length() > 0);
                twoLinesViewHolder.setComponentVisibility(R.id.icon1, twoLinesDataHolder.getIcon1Visible());
                twoLinesViewHolder.setComponentVisibility(R.id.icon2, twoLinesDataHolder.getIcon2Visible());
                updateDataHolder(twoLinesDataHolder);

            } else if (obj instanceof ThreeLinesDataHolder) {
                ThreeLinesDataHolder threeLinesDataHolder = (ThreeLinesDataHolder) obj;
                ThreeLinesViewHolder threeLinesViewHolder = (ThreeLinesViewHolder) viewHolder;
                threeLinesViewHolder.updateViewComponent(R.id.title, threeLinesDataHolder.getTitle());
                threeLinesViewHolder.updateViewComponent(R.id.row1col1, threeLinesDataHolder.getLine1()[0]);
                threeLinesViewHolder.updateViewComponent(R.id.row1col2, threeLinesDataHolder.getLine1()[1]);
                threeLinesViewHolder.updateViewComponent(R.id.row1col3, threeLinesDataHolder.getLine1()[2]);
                threeLinesViewHolder.updateViewComponent(R.id.row2col1, threeLinesDataHolder.getLine2()[0]);
                threeLinesViewHolder.updateViewComponent(R.id.row2col2, threeLinesDataHolder.getLine2()[1]);
                threeLinesViewHolder.updateViewComponent(R.id.row2col3, threeLinesDataHolder.getLine2()[2]);
                threeLinesViewHolder.updateViewComponent(R.id.row3col1, threeLinesDataHolder.getLine3()[0]);
                threeLinesViewHolder.updateViewComponent(R.id.row3col2, threeLinesDataHolder.getLine3()[1]);
                threeLinesViewHolder.updateViewComponent(R.id.row3col3, threeLinesDataHolder.getLine3()[2]);
                threeLinesViewHolder.updateViewComponent(R.id.icon, String.valueOf(threeLinesDataHolder.getImgResId()));
                updateDataHolder(threeLinesDataHolder);

            } else if (obj instanceof OneButtonDataHolder) {
                OneButtonDataHolder oneButtonDataHolder = (OneButtonDataHolder) obj;
                OneButtonViewHolder oneButtonViewHolder = (OneButtonViewHolder) viewHolder;
                oneButtonViewHolder.showHighlighted(oneButtonDataHolder.getHighlighted());
                oneButtonViewHolder.updateViewComponent(R.id.title, oneButtonDataHolder.getTitle());
                oneButtonViewHolder.updateViewComponent(R.id.modul_info, oneButtonDataHolder.getInfo());
                oneButtonViewHolder.updateViewComponent(R.id.button_info, oneButtonDataHolder.getButtonInfo());
                oneButtonViewHolder.updateViewComponent(R.id.button, oneButtonDataHolder.getButtonText());
                oneButtonViewHolder.updateViewComponent(R.id.icon, String.valueOf(oneButtonDataHolder.getImgResId()));
                updateDataHolder(oneButtonDataHolder);

            } else if (obj instanceof TwoButtonDataHolder) {
                TwoButtonDataHolder twoButtonDataHolder = (TwoButtonDataHolder) obj;
                TwoButtonViewHolder twoButtonViewHolder = (TwoButtonViewHolder) viewHolder;
                twoButtonViewHolder.updateViewComponent(R.id.title, twoButtonDataHolder.getTitle());
                twoButtonViewHolder.updateViewComponent(R.id.modul_info, twoButtonDataHolder.getInfo());
                twoButtonViewHolder.updateViewComponent(R.id.button1_info, twoButtonDataHolder.getButton1Info());
                twoButtonViewHolder.updateViewComponent(R.id.button1, twoButtonDataHolder.getButtonText(TwoButtonDataHolder.B1));
                twoButtonViewHolder.updateViewComponent(R.id.button1_1, twoButtonDataHolder.getButtonText(TwoButtonDataHolder.B11));
                twoButtonViewHolder.updateViewComponent(R.id.button1_2, twoButtonDataHolder.getButtonText(TwoButtonDataHolder.B12));
                twoButtonViewHolder.updateViewComponent(R.id.button2_info, twoButtonDataHolder.getButton2Info());
                twoButtonViewHolder.updateViewComponent(R.id.button2, twoButtonDataHolder.getButtonText(TwoButtonDataHolder.B2));
                twoButtonViewHolder.updateViewComponent(R.id.button2_1, twoButtonDataHolder.getButtonText(TwoButtonDataHolder.B21));
                twoButtonViewHolder.updateViewComponent(R.id.button2_2, twoButtonDataHolder.getButtonText(TwoButtonDataHolder.B22));
                twoButtonViewHolder.updateViewComponent(R.id.icon, String.valueOf(twoButtonDataHolder.getImgResId()));
                twoButtonViewHolder.setComponentVisibility(R.id.button1, twoButtonDataHolder.getButtonVisible(TwoButtonDataHolder.B1));
                twoButtonViewHolder.setComponentVisibility(R.id.button1_1, twoButtonDataHolder.getButtonVisible(TwoButtonDataHolder.B11));
                twoButtonViewHolder.setComponentVisibility(R.id.button1_2, twoButtonDataHolder.getButtonVisible(TwoButtonDataHolder.B12));
                twoButtonViewHolder.setComponentVisibility(R.id.button2, twoButtonDataHolder.getButtonVisible(TwoButtonDataHolder.B2));
                twoButtonViewHolder.setComponentVisibility(R.id.button2_1, twoButtonDataHolder.getButtonVisible(TwoButtonDataHolder.B21));
                twoButtonViewHolder.setComponentVisibility(R.id.button2_2, twoButtonDataHolder.getButtonVisible(TwoButtonDataHolder.B22));
                updateDataHolder(twoButtonDataHolder);
            }



/*

            int viewType = dataHolder.getType();
            if (viewType == MainActivity.ONE_BUTTON_ITEM) {
                OneButtonViewHolder oneButtonViewHolder = (OneButtonViewHolder) viewHolder;

                if (obj instanceof ArrayList) {
                    for (PayloadDataHolder data : (ArrayList<PayloadDataHolder>) obj) {
                        oneButtonViewHolder.updateViewComponent(data.getId(), data.getText());
                    }
                }
            } else if (viewType == MainActivity.TWO_LINES_ITEM) {
                TwoLinesViewHolder twoLinesViewHolder = (TwoLinesViewHolder) viewHolder;
                Object obj2 = payload.get(0);
                if (obj2 instanceof TwoLinesDataHolder) {
                    TwoLinesDataHolder twoLinesdDataHolder = (TwoLinesDataHolder) obj;
                    twoLinesViewHolder.updateViewComponent(R.id.title, twoLinesdDataHolder.getLine1());
                    twoLinesViewHolder.updateViewComponent(R.id.line2, twoLinesdDataHolder.getLine2());
                    twoLinesViewHolder.updateViewComponent(R.id.icon, String.valueOf(twoLinesdDataHolder.getImgResId()));

                    updateDataHolder(twoLinesdDataHolder);
                }
            } */

            /* else if (viewType == MainActivity.THREE_LINES_ITEM) {
                ThreeLinesViewHolder threeLinesViewHolder = (ThreeLinesViewHolder) viewHolder;

                if (obj instanceof ThreeLinesDataHolder) {
                    ThreeLinesDataHolder threeLinesdDataHolder = (ThreeLinesDataHolder) obj;
                    // updateDataHolder(threeLinesdDataHolder);

                    threeLinesViewHolder.updateViewComponent(R.id.title, threeLinesdDataHolder.getTitle());
                    threeLinesViewHolder.updateViewComponent(R.id.row1col1, threeLinesdDataHolder.getLine1()[0]);
                    threeLinesViewHolder.updateViewComponent(R.id.row1col2, threeLinesdDataHolder.getLine1()[1]);
                    threeLinesViewHolder.updateViewComponent(R.id.row1col3, threeLinesdDataHolder.getLine1()[2]);
                    threeLinesViewHolder.updateViewComponent(R.id.row2col1, threeLinesdDataHolder.getLine2()[0]);
                    threeLinesViewHolder.updateViewComponent(R.id.row2col2, threeLinesdDataHolder.getLine2()[1]);
                    threeLinesViewHolder.updateViewComponent(R.id.row2col3, threeLinesdDataHolder.getLine2()[2]);
                    threeLinesViewHolder.updateViewComponent(R.id.row3col1, threeLinesdDataHolder.getLine3()[0]);
                    threeLinesViewHolder.updateViewComponent(R.id.row3col2, threeLinesdDataHolder.getLine3()[1]);
                    threeLinesViewHolder.updateViewComponent(R.id.row3col3, threeLinesdDataHolder.getLine3()[2]);
                    threeLinesViewHolder.updateViewComponent(R.id.icon, String.valueOf(threeLinesdDataHolder.getImgResId()));

                    updateDataHolder(threeLinesdDataHolder);
                }
            }*/


        } else
            onBindViewHolder(viewHolder, pos);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int pos) {
        try {
            DataHolder dataHolder = mInListDataHolderArr.get(pos);
            // updateDataHolder(dataHolder);
            int viewType = dataHolder.getType();

            if (viewType == MainActivity.ONE_BUTTON_ITEM)
                bindOneButtonViewHolder((OneButtonViewHolder) viewHolder, (OneButtonDataHolder) dataHolder, pos);
            else if (viewType == MainActivity.TWO_BUTTON_ITEM)
                bindTwoButtonViewHolder((TwoButtonViewHolder) viewHolder, (TwoButtonDataHolder) dataHolder, pos);
            else if (viewType == MainActivity.TWO_LINES_ITEM)
                bindTwoLinesViewHolder((TwoLinesViewHolder) viewHolder, (TwoLinesDataHolder) dataHolder, pos);
            else if (viewType == MainActivity.THREE_LINES_ITEM)
                bindThreeLinesViewHolder((ThreeLinesViewHolder) viewHolder, (ThreeLinesDataHolder) dataHolder, pos);
            //else if( viewType == MainActivity.FOUR_BUTTON_ITEM) {
            // TODO
            // }
            //else if( viewType == MainActivity.TEXT_ITEM) {
            // TODO
            // }

            viewHolder.itemView.setTag(dataHolder.getModulId());

            try {
                updateDataHolder(dataHolder);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
            setAnimation(viewHolder.itemView, pos);

            if (mDoAnimateFirstRow)
                doAnimateFirstRow(viewHolder);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void doAnimateFirstRow(RecyclerView.ViewHolder viewHolder) {
        mDoAnimateFirstRow = false;
        Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
        viewHolder.itemView.startAnimation(animation);
        playSoundForAddItem(mContext);
    }

    private void bindOneButtonViewHolder(OneButtonViewHolder view, final OneButtonDataHolder model, int pos) {
        view.mTitle.setText(model.getTitle());
        view.mInfo.setText(model.getInfo());
        if (model.getHighlighted() && model.getAltImgResId() > 0)
            view.mImg.setImageResource(model.getAltImgResId());
        else
            view.mImg.setImageResource(model.getImgResId());
        view.mInfoButton.setText(model.getButtonInfo());
        view.mButton.setText(model.getButtonText());
        view.mButton.setTag(new ClickInfo(model.getModulId(), 1, String.valueOf(view.mButton.getText()), pos));

        setButtonListener(view.mButton, model.getModulId(), 1);

        view.showHighlighted(model.getHighlighted());
        view.mPos = pos;
        model.setPos(pos);
    }

    private void bindTwoButtonViewHolder(TwoButtonViewHolder view, TwoButtonDataHolder model, int pos) {
        view.mTitle.setText(model.getTitle());
        view.mInfo.setText(model.getInfo());
        if (model.getHighlighted() && model.getAltImgResId() > 0)
            view.mImg.setImageResource(model.getAltImgResId());
        else
            view.mImg.setImageResource(model.getImgResId());
        view.mInfoButton1.setText(model.getButton1Info());
        view.mButton1.setText(model.getButtonText(TwoButtonDataHolder.B1));
        view.mButton11.setText(model.getButtonText(TwoButtonDataHolder.B11));
        view.mButton12.setText(model.getButtonText(TwoButtonDataHolder.B12));
        view.mInfoButton2.setText(model.getButton2Info());
        view.mButton2.setText(model.getButtonText(TwoButtonDataHolder.B2));
        view.mButton21.setText(model.getButtonText(TwoButtonDataHolder.B21));
        view.mButton22.setText(model.getButtonText(TwoButtonDataHolder.B22));
        view.mButton1.setOnClickListener(mClickListener);
        view.mButton1.setTag(new ClickInfo(model.getModulId(), 1, String.valueOf(view.mButton1.getText()), pos));
        view.mButton2.setOnClickListener(mClickListener);
        view.mButton2.setTag(new ClickInfo(model.getModulId(), 2, String.valueOf(view.mButton2.getText()), pos));

        view.mButton1.setVisibility(model.getButtonVisible(TwoButtonDataHolder.B1) ? View.VISIBLE : View.INVISIBLE);
        view.mButton11.setVisibility(model.getButtonVisible(TwoButtonDataHolder.B11) ? View.VISIBLE : View.INVISIBLE);
        view.mButton12.setVisibility(model.getButtonVisible(TwoButtonDataHolder.B12) ? View.VISIBLE : View.INVISIBLE);
        view.mButton2.setVisibility(model.getButtonVisible(TwoButtonDataHolder.B2) ? View.VISIBLE : View.INVISIBLE);
        view.mButton21.setVisibility(model.getButtonVisible(TwoButtonDataHolder.B21) ? View.VISIBLE : View.INVISIBLE);
        view.mButton22.setVisibility(model.getButtonVisible(TwoButtonDataHolder.B22) ? View.VISIBLE : View.INVISIBLE);

        setButtonListener(view.mButton1, model.getModulId(), TwoButtonDataHolder.B1);
        setButtonListener(view.mButton11, model.getModulId(), TwoButtonDataHolder.B11);
        setButtonListener(view.mButton12, model.getModulId(), TwoButtonDataHolder.B12);
        setButtonListener(view.mButton2, model.getModulId(), TwoButtonDataHolder.B2);
        setButtonListener(view.mButton21, model.getModulId(), TwoButtonDataHolder.B21);
        setButtonListener(view.mButton22, model.getModulId(), TwoButtonDataHolder.B22);

        view.mPos = pos;
        model.setPos(pos);
    }

    private void bindTwoLinesViewHolder(TwoLinesViewHolder view, TwoLinesDataHolder model, int pos) {
        view.mTitle.setText(model.getLine1());
        view.mLine2.setText(model.getLine2());
        view.mLine2Center.setVisibility(model.getLine2Center().length() > 0 ? View.VISIBLE : View.INVISIBLE);
        view.mLine2Center.setText(model.getLine2Center());
        view.mLine2Right.setVisibility(model.getLine2Right().length() > 0 ? View.VISIBLE : View.INVISIBLE);
        view.mLine2Right.setText(model.getLine2Right());
        view.mIcon1.setVisibility(model.getIcon1Visible() ? View.VISIBLE : View.INVISIBLE);
        view.mIcon2.setVisibility(model.getIcon2Visible() ? View.VISIBLE : View.INVISIBLE);
        if (model.getHighlighted() && model.getAltImgResId() > 0)
            view.mImg.setImageResource(model.getAltImgResId());
        else
            view.mImg.setImageResource(model.getImgResId());
        view.mPos = pos;
        model.setPos(pos);
    }

    private void bindThreeLinesViewHolder(ThreeLinesViewHolder view, ThreeLinesDataHolder model, int pos) {
        view.mTitle.setText(model.getTitle());
        view.mRow1Col1.setText(model.getLine1()[0]);
        view.mRow1Col2.setText(model.getLine1()[1]);
        view.mRow1Col3.setText(model.getLine1()[2]);
        view.mRow2Col1.setText(model.getLine2()[0]);
        view.mRow2Col2.setText(model.getLine2()[1]);
        view.mRow2Col3.setText(model.getLine2()[2]);
        view.mRow3Col1.setText(model.getLine3()[0]);
        view.mRow3Col2.setText(model.getLine3()[1]);
        view.mRow3Col3.setText(model.getLine3()[2]);
        if (model.getHighlighted() && model.getAltImgResId() > 0)
            view.mImg.setImageResource(model.getAltImgResId());
        else
            view.mImg.setImageResource(model.getImgResId());
        view.mPos = pos;
        model.setPos(pos);
    }

    @Override
    public int getItemCount() {
        return mInListDataHolderArr == null ? 0 : mInListDataHolderArr.size();
    }

    public int getIdxForModulInList(int modulId) {
        int cnt = 0;
        for (DataHolder dataHolder : mInListDataHolderArr) {
            if (dataHolder.getModulId() == modulId)
                return cnt;
            cnt++;
        }
        return -1;
    }

    private void setButtonListener(Button btn, final int modulId, final int btnNr) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendButtonTouchBroadcast(modulId, btnNr);
            }
        });
    }

    /*
    public int getIdxForModul( int modulId) {

        for( DataHolder dataHolder: mInListDataHolderArr) {
            if( dataHolder.getModulId() == modulId)
                return dataHolder.getPos();
        }
        for( DataHolder dataHolder: mNOTinListDataHolderArr) {
            if( dataHolder.getModulId() == modulId)
                return dataHolder.getPos();
        }
        return -1;
    } */

    public DataHolder getItemAtPosition(int pos) {
        return mInListDataHolderArr.get(pos);
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    private String getDate(String dateAsString) {
        return "";//return dateAsString.equals( new SimpleDateFormat( Event.DATE_PATTERN, Locale.getDefault()).format( now)) ? context.getString( R.string.today) : dateAsString;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mInListDataHolderArr, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mInListDataHolderArr, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
                               // getNotifications();
        Log.d(TAG, "onItemDismiss [position:" + position + "]");
        playSoundForRemoveItem(mContext);
/*
        mInListDataHolderArr.get(position).setVisible(false);
                                // notifyDataSetChanged();
        removeDataHolderFromList(mInListDataHolderArr.get(position));
        notifyItemRemoved(position);
                                // notifyDataSetChanged();
        */

        new RemoveItemTask().execute(position);
    }

    private void sendButtonTouchBroadcast(int modulId, int buttonNr) {
        Intent bc = new Intent();
        bc.setAction(ACTION_BUTTON_CLICK_MODUL + modulId);
        bc.putExtra(KEY_BUTTON_NR, buttonNr);
        mContext.sendBroadcast(bc);
    }

    private void sendPanelTouchBroadcast(int modulId) {
        Intent bc = new Intent();
        bc.setAction(ACTION_PANEL_CLICK_MODUL + modulId);
        mContext.sendBroadcast(bc);
    }

    public void playSoundForRemoveItem(final Context ctx) {
        //Define sound URI
        //mView.playSoundEffect(SoundEffectConstants.
       /*  Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); //Doink
        Ringtone r = RingtoneManager.getRingtone(ctx, soundUri);
        r.play();
        */

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                // Uri soundUri = Uri.parse( "content://media/internal/audio/media/50");//RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                //  Ringtone r = RingtoneManager.getRingtone(ctx, soundUri);
                //  r.play();

                Log.d(TAG, "playSoundForRemoveItem");
                mView.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
                //mView.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);

                // AudioManager audio = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
                //audio.playSoundEffect(Sounds.TAP);
                //    audio.playSoundEffect(AudioManager.FX_KEY_CLICK);
            }
        });
    }

    public void playSoundForAddItem(final Context ctx) {
        // getNotifications();
        /*
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Uri soundUri = Uri.parse( "content://media/internal/audio/media/89");
                Ringtone r = RingtoneManager.getRingtone(ctx, soundUri);
                r.play();
            }
        }); */
    }

    public Map<String, String> getNotifications() {
        RingtoneManager manager = new RingtoneManager(mContext);
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor cursor = manager.getCursor();

        Map<String, String> list = new HashMap<>();
        while (cursor.moveToNext()) {
            String notificationId = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String notificationUri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            System.out.println("id: " + notificationId + ", title: " + notificationTitle + ", " + notificationUri);
            list.put(notificationTitle, notificationUri);
        }

        return list;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Broadcast Receiver
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class MyBroadcastReceicer extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int modulId = intent.getIntExtra(_MainListAdapter.KEY_MODUL_ID, -1);
            int posInAdapter = getIdxForModulInList(modulId);
            if (posInAdapter >= 0) {
                DataHolder dataHolder = getDataHolderAtPos(posInAdapter);
                if (dataHolder != null) {
                    if (dataHolder.getUpdateAll()) {
                        dataHolder.setUpdateAll(false);  // reset flag
                        notifyItemChanged(posInAdapter);  // update full row
                    } else
                        notifyItemChanged(posInAdapter, dataHolder); // update with payload
                }
            } else if (intent.getBooleanExtra(_MainListAdapter.KEY_SHOW_IN_LIST, false)) {  // if not in list and flag "show in List" then add it
                DataHolder dataHolder = findModul(modulId);
                if (intent.getBooleanExtra(_MainListAdapter.KEY_SORT, false)) {
                    mDoAnimateFirstRow = true;
                }
                addDataHolderToList(dataHolder);
                sort();
                notifyDataSetChanged();

                // doAnimateFirstRow(dataHolder);
            }

            /*
            int posInAdapter = intent.getIntExtra(MainListAdapter.KEY_POSITION, -1);
            if (posInAdapter >= 0) {
                DataHolder dataHolder = getDataHolderAtPos(posInAdapter);
                if (dataHolder != null)
                    notifyItemChanged(posInAdapter, dataHolder); // update with payload
                   // notifyItemChanged(posInAdapter);  // update full row
            } */
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

        private final ItemTouchHelperAdapter mAdapter;

        public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // WebSocket AsyncTask
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public class RemoveItemTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... param) {
            Integer position = param[0];

            //playSoundForRemoveItem(mContext);

           // mInListDataHolderArr.get(position).setVisible(false);
            // notifyDataSetChanged();
            DataHolder dh = mInListDataHolderArr.get(position);
            Log.d(TAG, "RemoveItemTask: " + dh.toString());
            removeDataHolderFromList(dh);
            notifyItemRemoved(position);

            return null;
        }
    }
}