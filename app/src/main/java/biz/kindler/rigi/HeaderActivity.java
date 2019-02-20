package biz.kindler.rigi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 28.10.16.
 */

public class HeaderActivity extends Activity {

    public static final String  KEY_BGFILENAME  = "bgfile";

    private TextView            mTime;
    private SimpleDateFormat    mTimeFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Time
        mTime = (TextView)findViewById(R.id.time);
        mTimeFormatter = new SimpleDateFormat( "HH:mm");
        mTimeFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
    }

    @Override
    protected void onResume() {
        super.onResume();

       // Calendar time = Calendar.getInstance();
      //  int dayOfMonth = time.get( Calendar.DAY_OF_MONTH);

      //  Intent returnIntent = new Intent();
      //  returnIntent.putExtra(KEY_BGFILENAME, getResIdForDay( dayOfMonth));
      //  setResult(Activity.RESULT_OK,returnIntent);
     //  finish();
    }

    private int getResIdForDay( int day) {
        switch( day) {
            case 0 : return R.drawable.background1;
            case 1 : return R.drawable.background2;
            case 2 : return R.drawable.background3;
            case 3 : return R.drawable.background4;
            case 4 : return R.drawable.background5;
            case 5 : return R.drawable.background6;
            case 6 : return R.drawable.background7;
            case 7 : return R.drawable.background8;
            case 8 : return R.drawable.background9;
            case 9 : return R.drawable.background10;
            case 10 : return R.drawable.background11;
            case 11 : return R.drawable.background12;
            case 12 : return R.drawable.background13;
            case 13 : return R.drawable.background14;
            case 14 : return R.drawable.background15;
            case 15 : return R.drawable.background16;
            case 16 : return R.drawable.background17;
            case 17 : return R.drawable.background18;
            case 18 : return R.drawable.background19;
            case 19 : return R.drawable.background20;
            case 20 : return R.drawable.background21;
            case 21 : return R.drawable.background22;
            case 22 : return R.drawable.background23;
            case 23 : return R.drawable.background24;
            case 24 : return R.drawable.background25;
            case 25 : return R.drawable.background26;
            case 26 : return R.drawable.background27;
            case 27 : return R.drawable.background28;
            case 28 : return R.drawable.background29;
            case 29 : return R.drawable.background30;
            case 30 : return R.drawable.background31;
            default : return R.drawable.background1;
        }
    }

}
