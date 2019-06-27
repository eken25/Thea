package com.prdeck.thea;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import static com.prdeck.thea.Const.ACTION_COMPLETE;
import static com.prdeck.thea.Const.TIMER_ID;


public class MainActivity extends AppCompatActivity {
    TextView mTxt;
    private static String TAG = Const.TAG;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTxt = this.findViewById(R.id.output);
        ImageView u = (ImageView) this.findViewById(R.id.gloss);
        u.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b = Util.checkForPermission(getApplicationContext());
                if(b){
                    AppDataIntentService.enqueueWork(getApplicationContext());
                    //scheduleJob(getApplicationContext());

                }else{
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            }
        });
        IntentFilter filter = new IntentFilter(ACTION_COMPLETE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, filter);
        enableBootCompleteReceiver(getApplicationContext());
        Util.scheduleUpload(getApplicationContext(), TIMER_ID);
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == ACTION_COMPLETE){
                int status = intent.getIntExtra(Const.EXTRA_STATUS,0);
                if(status == 1){
                    mTxt.setText(getString(R.string.action_complete));
                }else{
                    mTxt.setText(getString(R.string.action_not_complete));
                }

            }
        }
    };


    /*private void scheduleJob(Context context){
        JobScheduler mJobScheduler = (JobScheduler)
                getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1,
                new ComponentName(getPackageName(),
                        AppDataIntentService.class.getName()));
        builder.setPeriodic(5*60*1000);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

        if (mJobScheduler.schedule(builder.build()) <= 0) {
            Log.e(TAG, "onCreate: Some error while scheduling the job");
        }else{
            Toast.makeText(context,"Success",Toast.LENGTH_SHORT).show();
        }
    }*/

    private void enableBootCompleteReceiver(Context context){
        ComponentName componentName = new ComponentName(context, TheaBroadcastReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}
