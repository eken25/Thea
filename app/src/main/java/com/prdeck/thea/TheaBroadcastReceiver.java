package com.prdeck.thea;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.prdeck.thea.Const.TIMER_ID;
import static com.prdeck.thea.Util.scheduleUpload;

public class TheaBroadcastReceiver extends BroadcastReceiver {
    private static String TAG = Const.TAG;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received intent: " + intent.getAction());
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            scheduleUpload(context, TIMER_ID);

        } else if (intent.getAction().equals("com.prdeck.thea.ALARM")) {
            AppDataIntentService.enqueueWork(context);
        }
    }
}
