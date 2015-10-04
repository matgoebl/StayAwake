package com.andreadec.stayawake;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "StayAwakeAlarm";
    private static final int WAKEUP_HOUR = 6;
    private static final int SLEEP_HOUR = 22;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received.");

        if (updateAlarm(context)) {
            Log.d(TAG, "Start StayAlive service.");
            context.startService(new Intent(context, StayAwakeService.class));
        } else {
            Log.d(TAG, "Stop StayAlive service.");
            context.stopService(new Intent(context, StayAwakeService.class));
        }
    }

    public static boolean updateAlarm(Context context) {
        Log.d(TAG, "Updating alarm...");
        cancelAlarm(context);

        int curHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (WAKEUP_HOUR <= curHour && curHour < SLEEP_HOUR ) {
            registerAlarm(context, SLEEP_HOUR, 0);
            return true;
        } else {
            registerAlarm(context, WAKEUP_HOUR, 0);
            return false;
        }
    }

    public static void registerAlarm(Context context, int hour, int minute) {
        Log.d(TAG, "Register new alarm.");
        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.HOUR_OF_DAY) > hour || ( calendar.get(Calendar.HOUR_OF_DAY) == hour && calendar.get(Calendar.MINUTE) >= minute) ) {
            calendar.add(Calendar.DATE, 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 00);

        AlarmReceiver alarmReceiver = new AlarmReceiver();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Log.d(TAG, "Alarm scheduled for " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
	//Toast.makeText(context, "Next: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()), Toast.LENGTH_LONG).show();
    }

    public static void cancelAlarm(Context context) {
        Log.d(TAG, "Cancel old alarm.");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }
}
