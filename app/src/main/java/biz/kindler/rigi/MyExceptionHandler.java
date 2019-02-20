package biz.kindler.rigi;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by patrick kindler (katermoritz100@gmail.com) on 20.02.19
 */
public class MyExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Activity activity;

    public MyExceptionHandler(Activity a) {
        activity = a;
    }
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.putExtra("crash", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(RigiApplication.getAppContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) RigiApplication.getAppContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, pendingIntent);
        activity.finish();
        System.exit(2);
    }
}
