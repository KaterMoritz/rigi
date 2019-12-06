package biz.kindler.rigi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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

}
