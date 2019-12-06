package biz.kindler.rigi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import biz.kindler.rigi.modul.OneButtonDataHolder;
import biz.kindler.rigi.modul.OneButtonViewHolder;
import biz.kindler.rigi.modul.ThreeLinesDataHolder;
import biz.kindler.rigi.modul.ThreeLinesViewHolder;
import biz.kindler.rigi.modul.TwoButtonDataHolder;
import biz.kindler.rigi.modul.TwoButtonViewHolder;
import biz.kindler.rigi.modul.TwoLinesDataHolder;
import biz.kindler.rigi.modul.TwoLinesViewHolder;
import biz.kindler.rigi.modul.system.Log;
import biz.kindler.rigi.modul.system.SystemModel;
import biz.kindler.rigi.settings.LogPreferenceFragment;

/**
 * Created by patrick kindler (katermoritz100@gmail.com)
 *
 */
public class MainListAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter {

    private final static String TAG = MainListAdapter2.class.getSimpleName();

    public static final String ACTION_UPDATE_LISTITEM = "action-update-listitem";
    public static final String ACTION_BUTTON_CLICK_MODUL = "action-button-touch-modulid-";
    public static final String ACTION_PANEL_CLICK_MODUL = "action-modul-touch-modulid-";
    public static final String KEY_SHOW_IN_LIST = "show-in-list";  // true = add to list | false = remove from list
    public static final String KEY_DO_ANIMATE = "animate";
    public static final String KEY_MODUL_ID = "modul-id";
    public static final String KEY_BUTTON_NR = "button-nr";
    public static final String ACTION_STATUS_CHANGED_MODUL_IN_LIST = "status-modul-in-list"; // this class is sending this event


    private int lastPosition = -1;
    private Context mContext;
    private RecyclerView mView;
    private ArrayList<DataHolder> mListDataHolderArr;
    private View.OnClickListener mClickListener;
    private boolean mDoAnimateFirstRow;
    private SharedPreferences mPrefs;

    public MainListAdapter2(Context ctx, RecyclerView listView) {
        mContext = ctx;
        mView = listView;
        mListDataHolderArr = new ArrayList<DataHolder>();

        setHasStableIds(false);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_LISTITEM);
        intentFilter.addAction(LogPreferenceFragment.SYSTEMSERVICE);
        ctx.registerReceiver(new MyBroadcastReceicer(), intentFilter);

        mView.setItemAnimator(animator);
      //  mView.setHasFixedSize(true);
    }

    public void setAllData(ArrayList<DataHolder> dataHolder) {
        mListDataHolderArr = dataHolder;
    }
/*
    private int getModulPos(int modulId, ArrayList<DataHolder> list) {
        int cnt = 0;
        for (DataHolder dataHolder : list) {
            if (dataHolder.getModulId() == modulId) {
                return cnt;
            }
            cnt++;
        }
        return -1;
    } */

    public void setClickListener(View.OnClickListener clickListener) {
        mClickListener = clickListener;
    }

    @Override
    public int getItemViewType(int position) {
        return mListDataHolderArr.get(position).getType();
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

                twoLinesViewHolder.showHighlighted(twoLinesDataHolder.getHighlighted());
                twoLinesViewHolder.updateViewComponent(R.id.title, twoLinesDataHolder.getLine1());
                twoLinesViewHolder.updateViewComponent(R.id.line2, twoLinesDataHolder.getLine2());
                twoLinesViewHolder.updateViewComponent(R.id.line2_center, twoLinesDataHolder.getLine2Center());
                twoLinesViewHolder.updateViewComponent(R.id.line2_right, twoLinesDataHolder.getLine2Right());
                twoLinesViewHolder.updateViewComponent(R.id.icon, String.valueOf(twoLinesDataHolder.getImgResId()));
                twoLinesViewHolder.setComponentVisibility(R.id.line2_center, twoLinesDataHolder.getLine2Center().length() > 0);
                twoLinesViewHolder.setComponentVisibility(R.id.line2_right, twoLinesDataHolder.getLine2Right().length() > 0);
                twoLinesViewHolder.setComponentVisibility(R.id.icon1, twoLinesDataHolder.getIcon1Visible());
                twoLinesViewHolder.setComponentVisibility(R.id.icon2, twoLinesDataHolder.getIcon2Visible());

                twoLinesViewHolder.showVisible( twoLinesDataHolder.getVisible());

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

                threeLinesViewHolder.showVisible( threeLinesDataHolder.getVisible());

            } else if (obj instanceof OneButtonDataHolder) {
                OneButtonDataHolder oneButtonDataHolder = (OneButtonDataHolder) obj;
                OneButtonViewHolder oneButtonViewHolder = (OneButtonViewHolder) viewHolder;

                oneButtonViewHolder.showHighlighted(oneButtonDataHolder.getHighlighted());
                oneButtonViewHolder.updateViewComponent( R.id.title, oneButtonDataHolder.getTitle());
                oneButtonViewHolder.updateViewComponent( R.id.modul_info, oneButtonDataHolder.getInfo());
                oneButtonViewHolder.updateViewComponent( R.id.button_info, oneButtonDataHolder.getButtonInfo());
                oneButtonViewHolder.updateViewComponent( R.id.button, oneButtonDataHolder.getButtonText());
                oneButtonViewHolder.updateViewComponent( R.id.icon, String.valueOf(oneButtonDataHolder.getImgResId()));

                oneButtonViewHolder.showVisible( oneButtonDataHolder.getVisible());

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

                twoButtonViewHolder.showVisible( twoButtonDataHolder.getVisible());
            }

        } else
            onBindViewHolder(viewHolder, pos);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int pos) {
        try {
            int safePosition = viewHolder.getAdapterPosition(); // see: https://stackoverflow.com/questions/43813425/recyclerview-and-java-lang-indexoutofboundsexception-invalid-view-holder-adapter

            DataHolder dataHolder = mListDataHolderArr.get(safePosition);
            // updateDataHolder(dataHolder);
            int viewType = dataHolder.getType();

            if (viewType == MainActivity.ONE_BUTTON_ITEM) {
                bindOneButtonViewHolder((OneButtonViewHolder) viewHolder, (OneButtonDataHolder) dataHolder, safePosition);
                ((OneButtonViewHolder)viewHolder).showVisible( dataHolder.getVisible());
            }
            else if (viewType == MainActivity.TWO_BUTTON_ITEM) {
                bindTwoButtonViewHolder((TwoButtonViewHolder) viewHolder, (TwoButtonDataHolder) dataHolder, safePosition);
                ((TwoButtonViewHolder)viewHolder).showVisible( dataHolder.getVisible());
            }
            else if (viewType == MainActivity.TWO_LINES_ITEM) {
                bindTwoLinesViewHolder((TwoLinesViewHolder) viewHolder, (TwoLinesDataHolder) dataHolder, safePosition);
                ((TwoLinesViewHolder)viewHolder).showVisible( dataHolder.getVisible());
            }
            else if (viewType == MainActivity.THREE_LINES_ITEM) {
                bindThreeLinesViewHolder((ThreeLinesViewHolder) viewHolder, (ThreeLinesDataHolder) dataHolder, safePosition);
                ((ThreeLinesViewHolder)viewHolder).showVisible( dataHolder.getVisible());
            }

            viewHolder.itemView.setTag(dataHolder.getModulId());
           // viewHolder.itemView.setId(dataHolder.getModulId());

          //  setAnimation(viewHolder.itemView, safePosition);

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
        mView.invalidate();
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

        view.showHighlighted(model.getHighlighted());
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
        return mListDataHolderArr == null ? 0 : mListDataHolderArr.size();
    }

    @Override
    public long getItemId(int position) {
        //return mInListDataHolderArr.get(position).getModulId(); // only with: setHasStableIds(true); //Return the stable ID for the item at position.
        return super.getItemId(position);
    }

    private void setButtonListener(Button btn, final int modulId, final int btnNr) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendButtonTouchBroadcast(modulId, btnNr);
            }
        });
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mListDataHolderArr, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mListDataHolderArr, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        DataHolder dh = mListDataHolderArr.get(position);
        if (dh != null) {
            Log.d(TAG, "onItemDismiss [position:" + position + ",modul:" + dh.getModulId() + "]");
            hideModulInList(dh.getModulId(), false);
            mView.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
        }
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

    private SharedPreferences getPrefs() {
        if (mPrefs == null)
            // mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            mPrefs = mContext.getSharedPreferences(MainActivity.PREFS_ID, Activity.MODE_PRIVATE);
        return mPrefs;
    }

    private void setPropertyModulInList(int modulId, boolean inList) {
        SharedPreferences.Editor prefEditor = getPrefs().edit();
        prefEditor.putBoolean(String.valueOf(modulId), inList);
       // prefEditor.putLong("ACTIONTIME_" + String.valueOf(modulId), new Date().getTime());
        prefEditor.apply();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // https://stackoverflow.com/questions/32463136/recyclerview-adapter-notifyitemchanged-never-passes-payload-to-onbindviewholde
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    DefaultItemAnimator animator = new DefaultItemAnimator() {
        @Override
        public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
            return true;
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Broadcast Receiver
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class MyBroadcastReceicer extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if( action.equals(ACTION_UPDATE_LISTITEM)) {
                int modulId = intent.getIntExtra(KEY_MODUL_ID, -1);
                int posInAdapter = findDataHolderPositionByModulId(modulId);// getModulPos(modulId, mListDataHolderArr);
                if (posInAdapter >= 0) {
                    DataHolder dataHolder = mListDataHolderArr.get(posInAdapter);
                    if (dataHolder != null) {
                        if (dataHolder.getUpdateAll()) {
                            dataHolder.setUpdateAll(false);  // reset flag
                            Log.d(TAG, MainActivity.getModulName( dataHolder.getModulId()) + " update all");
                            notifyItemChanged(posInAdapter);  // update full row
                        } else {
                            Log.d(TAG, MainActivity.getModulName( dataHolder.getModulId()) + " update with payload");
                            notifyItemChanged(posInAdapter, dataHolder); // update with payload
                        }
                    }
                }
                if (intent.hasExtra(KEY_SHOW_IN_LIST)) {
                    boolean showInList = intent.getBooleanExtra(KEY_SHOW_IN_LIST, false);
                    boolean animate = intent.getBooleanExtra(KEY_DO_ANIMATE, false);
                    if (showInList) {
                        Log.d(TAG, MainActivity.getModulName( modulId) + " show in list");
                        addOrMoveModulInList(modulId, animate);
                    }
                    else {
                        Log.d(TAG, MainActivity.getModulName( modulId) + " remove from list");
                        hideModulInList(modulId, animate);
                    }
                }
            }
            else if( action.equals(LogPreferenceFragment.SYSTEMSERVICE)) {
                String sysCmd = intent.getStringExtra( SystemModel.KEY_MESSAGE);
                runSystemCmd( sysCmd);
            }
        }
    }

    private View findViewByModulId( int modulId) {
        for( int cnt=0; cnt<getItemCount(); cnt++) {
            View v = mView.getLayoutManager().getChildAt(cnt);
            if((Integer)v.getTag() == modulId)
                return v;
        }
        return null;
    }

    private DataHolder findDataHolderByModulId( int modulId) {
        Iterator<DataHolder> iter = mListDataHolderArr.iterator();
        while( iter.hasNext()) {
            DataHolder dh = iter.next();
            if( dh.getModulId() == modulId)
                return dh;
        }
        return null;
    }

    private int findDataHolderPositionByModulId( int modulId) {
        for( int cnt=0; cnt<getItemCount(); cnt++) {
            DataHolder dh = mListDataHolderArr.get(cnt);
            if( dh.getModulId() == modulId)
                return cnt;
        }
        return -1;
    }

    private synchronized void hideModulInList( int modulId, boolean animate) {
       // final int inListPos = findDataHolderByModulId( modulId);
       // if( inListPos >= 0) {
           // DataHolder dh = mInListDataHolderArr.remove( inListPos);

            final DataHolder dh = findDataHolderByModulId( modulId);
            final int pos = findDataHolderPositionByModulId( modulId);

            View currView = findViewByModulId(modulId);// mView.getLayoutManager().findViewByPosition(inListPos);
            Log.d(TAG, "hide item with TAG: " + currView.getTag() + " [" + MainActivity.getModulName( ((Integer)currView.getTag())) + "]");
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_out_right);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    dh.setVisible( false);
                    notifyItemChanged(pos);
                    mView.invalidate();
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            currView.startAnimation(animation);





           // notifyDataSetChanged();
           // mView.getLayoutManager().findViewByPosition(inListPos).invalidate();
          //  mView.getLayoutManager().requestLayout();
           // mView.getLayoutManager().scrollToPosition(0);
           // mView.invalidate();
            // mNOTinListDataHolderArr.add( dh);
            // notifyItemRemoved(inListPos);
            //mView.invalidate();
            setPropertyModulInList( modulId, false);
            sendStatusChangedModulInListBroadcast( modulId, false);
       // }
    }

    private void addOrMoveModulInList( int modulId, boolean animate) {
       // int notInListPos = getModulPos( modulId, mNOTinListDataHolderArr);
        //int inListPos = getModulPos( modulId, mListDataHolderArr);
        //DataHolder dh = mListDataHolderArr.get(inListPos);
        DataHolder dh = findDataHolderByModulId( modulId);
        if( dh.getVisible())
            ;//moveModulToTop( modulId, false);
        else
            showModulInList( modulId, true); // todo: rename to setVisibleModulInList new: add means setVisible
        /*if( notInListPos == -1)
            moveModulToTop( modulId, animate);
        else
            addModulToList( modulId, animate);
            */
    }

    private synchronized void showModulInList( int modulId, final boolean animate) {
        //int inListPos = getModulPos( modulId, mListDataHolderArr);
       // mListDataHolderArr.get(inListPos).setVisible(true);
        DataHolder dh = findDataHolderByModulId( modulId);
        dh.setVisible( true);
        int pos = findDataHolderPositionByModulId(modulId);
        //RecyclerView.ViewHolder holder = mView.findViewHolderForAdapterPosition(pos);

        notifyItemChanged( pos);
        //mView.getLayoutManager().findViewByPosition(pos).invalidate();
        //notifyItemInserted(pos);

       // if( holder != null && holder instanceof OneButtonViewHolder)
       //     ((OneButtonViewHolder)holder).showVisible(true);



       // mView.getLayoutManager().onItemsUpdated( mView, inListPos, mListDataHolderArr.size());
        //moveModulToTop( modulId, true);
        /*
        new Handler().postDelayed(new Runnable() {
            public void run() {
                DataHolder dh = mListDataHolderArr.get(0);
                dh.setVisible( true);
                Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
                View v0 = mView.getChildAt(0);
                if( v0 != null) {
                    v0.startAnimation(animation);
                }
            }
        }, 500); */

        setPropertyModulInList( modulId, true);
        sendStatusChangedModulInListBroadcast( modulId, true);
    }

    private void moveModulToTop( int modulId, boolean animate) {
        final int inListPos = findDataHolderPositionByModulId( modulId); //getModulPos( modulId, mListDataHolderArr);
        //if( inListPos >= 0) {
           // DataHolder dh = mListDataHolderArr.remove(inListPos);
           // mListDataHolderArr.add(0, dh);

           // Collections.swap(mListDataHolderArr, inListPos, 0);
            notifyItemMoved(inListPos, 0);

            //notifyItemMoved(inListPos, 0);
            mView.getLayoutManager().scrollToPosition(0);
          //  mView.getLayoutManager().onItemsUpdated( mView, 0, mListDataHolderArr.size());

            animate = false;

            if( animate) {
                View currView = mView.getLayoutManager().findViewByPosition(inListPos);
                Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                       // notifyDataSetChanged();
                        mView.invalidate();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                if (currView != null)
                    currView.startAnimation(animation);
            }
       // }
    }

    protected void sendStatusChangedModulInListBroadcast( int modulId, boolean showInlist) {
        Intent bc = new Intent();
        bc.setAction( ACTION_STATUS_CHANGED_MODUL_IN_LIST);
        bc.putExtra( KEY_SHOW_IN_LIST, showInlist);
        bc.putExtra( KEY_MODUL_ID, modulId);
        mContext.sendBroadcast(bc);
    }

    public MainListAdapter2 getInstance() {
        return this;
    }

    private void runSystemCmd( final String cmd) {
        Log.w(TAG, "received sys cmd: " + cmd);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(cmd.equals( "1")) {
                    addOrMoveModulInList(MainActivity.SYSTEM, false);
                    Log.w(TAG, "run sys cmd with delay: addOrMoveModulInList");
                } else if(cmd.equals( "2")) {
                    hideModulInList(MainActivity.SYSTEM, false);
                    Log.w(TAG, "run sys cmd with delay: removeModulFromList");
                }
            }
        }, 5000);
    }
}