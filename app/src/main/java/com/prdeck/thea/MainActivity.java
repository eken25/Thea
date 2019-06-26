package com.prdeck.thea;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.app.AppOpsManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import static com.prdeck.thea.Const.ACTION_COMPLETE;


public class MainActivity extends AppCompatActivity {
    TextView mTxt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTxt = this.findViewById(R.id.output);
        ImageView u = (ImageView) this.findViewById(R.id.gloss);
        u.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean b = checkForPermission(getApplicationContext());
                if(b){
                    AppDataIntentService.enqueueWork(getApplicationContext());

                }else{
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            }
        });
        IntentFilter filter = new IntentFilter(ACTION_COMPLETE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBrodcastReceiver, filter);
    }

    BroadcastReceiver mBrodcastReceiver = new BroadcastReceiver() {
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

    private boolean checkForPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

}
