package com.prdeck.thea;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import java.util.Calendar;

import static com.prdeck.thea.Const.INTENT_ALARM;

class Util {
    private static String TAG = Const.TAG;

    protected static boolean checkForPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
    protected static void scheduleUpload(Context context, int id){
        /*if(isTimerActive(id, context)){
            Log.d(TAG, "Timer is already active");
            stopTimer(context, id);
            //return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id,getAlarmIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,pendingIntent);
        /*alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + (2*60*1000),
                (2*60*1000), pendingIntent);
        Log.d(TAG, "Alarm Scheduled for upload");*/
    }

    protected static Intent getAlarmIntent(Context context) {
        Intent intent = new Intent(context, TheaBroadcastReceiver.class);
        intent.setAction(INTENT_ALARM);
        return intent;
    }
    protected static boolean isTimerActive(int id, Context context){
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id,getAlarmIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent != null;
    }

    protected static void stopTimer(Context context, int id){
        if(isTimerActive(id, context)){
            PendingIntent.getBroadcast(context, id, getAlarmIntent(context),
                    PendingIntent.FLAG_NO_CREATE).cancel();
            Log.d(TAG, "UploadSchedule Cancelled");
        }
    }
}
