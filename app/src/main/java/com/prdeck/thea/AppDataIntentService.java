package com.prdeck.thea;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class AppDataIntentService extends JobIntentService {
    static final int JOB_ID = 1000;
    private String mHeader = "Package name,Start_Time,End_Time,Duration,Category\n";
    private static String TAG = "Apps";
    private static String ACTION_FIREBASE = "Firebase";
    List<AppData> data = new ArrayList<>();
    static final String USER_PREF = "USER_PREF" ;
    SharedPreferences mSp;
    static final String LATEST_TIMESTAMP = "KEY_NAME";

    private int getAppCat(String packname){
        int cat = ApplicationInfo.CATEGORY_UNDEFINED;
        try {
            ApplicationInfo app = this.getPackageManager().getApplicationInfo(packname, 0);
            cat = app.category;
        }catch(PackageManager.NameNotFoundException e){e.printStackTrace();}
        return cat;
    }

    private List<String> getInstalledApps(Context context){
        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> l =  context.getPackageManager().queryIntentActivities(intent,0);
        List<String> s = new ArrayList<>();
        l.forEach(a -> {
            if ((a.activityInfo != null) && (!TextUtils.isEmpty(a.activityInfo.taskAffinity))) {
                //Log.d(tag, "activity:" + a.activityInfo.taskAffinity);
                if(a.activityInfo.taskAffinity != "com.prdeck.thea"){
                    s.add(a.activityInfo.taskAffinity);
                }

            }
        });
        return s;
    }

    public File saveToTextFile(Context context, List<AppData> data){
        String filename = Long.toString(System.currentTimeMillis()) + ".txt";
        File file = new File(context.getFilesDir() + "/" + filename);
        try{
            FileOutputStream f = context.openFileOutput(filename, Context.MODE_PRIVATE);
            final OutputStreamWriter osw = new OutputStreamWriter(f);
            osw.write(mHeader);
            data.forEach(d -> {
                try {
                    osw.write(d.toString() + "\n");
                }catch (IOException e ){
                    e.printStackTrace();
                }
            });
            osw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return file;
    }

    public void getStats(Context context){
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        /*calendar.add(Calendar.DAY_OF_WEEK, -1);
        long startTime = calendar.getTimeInMillis();*/
        long startTime = getTimeStamp();
        UsageEvents uEvents = usm.queryEvents(startTime,endTime);
        List<String> p = getInstalledApps(context);
        HashMap<String,Long> map = new HashMap<String,Long>();
        while (uEvents.hasNextEvent()){
            UsageEvents.Event e = new UsageEvents.Event();
            uEvents.getNextEvent(e);
            if(e == null) continue;
            if((p.size() != 0) && (p.contains(e.getPackageName()))) {
                if(e.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND){
                    map.put(e.getPackageName(),e.getTimeStamp());
                }else if(e.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND){
                    if(map.containsKey(e.getPackageName())){
                        Long stime = map.get(e.getPackageName());
                        map.remove(e.getPackageName());
                        AppData a = new AppData();
                        a.setCategory(getAppCat(e.getPackageName()));
                        if(e.getTimeStamp() - stime != 0){
                            a.setDuration(e.getTimeStamp() - stime);
                        }
                        a.setPackageName(e.getPackageName());
                        a.setEndTime(e.getTimeStamp());
                        a.setStartTime(stime);
                        data.add(a);
                    }
                }
            }
        }

        /*data.forEach(d -> {
            Log.d(tag, d.toString());
        });*/
        if(data.size() > 0){
            File fname = saveToTextFile(context, data);
            uploadToCloud(fname);
        }

    }

    private  void saveTimeStamp(){
        AppData p = data.get(data.size() - 1);
        SharedPreferences.Editor editor = mSp.edit();
        editor.putLong(LATEST_TIMESTAMP,  p.getStartTime());
        editor.commit();
    }

    private long getTimeStamp(){
        return mSp.getLong(LATEST_TIMESTAMP,0L);
    }

    private void uploadToCloud(File fname){
        Uri uri = Uri.fromFile(fname);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference dataRef = storageRef.child(fname.getName());
        UploadTask ut = dataRef.putFile(uri);
        ut.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Unsuccessfully uploaded the data");
                e.printStackTrace();
                sendStatus(0);
            }
        });
        ut.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "Successfully uploaded the data");
                saveTimeStamp();
                sendStatus(1);

            }
        });

    }

    @Override
    public void onCreate(){
        super.onCreate();
        mSp = getSharedPreferences(USER_PREF, Context.MODE_PRIVATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void sendStatus(int status){
        Intent intent = new Intent(Const.ACTION_COMPLETE);
        intent.putExtra(Const.EXTRA_STATUS, status);
        LocalBroadcastManager.getInstance(AppDataIntentService.this)
                .sendBroadcast(intent);
    }

    static void enqueueWork(Context context) {
        Intent work = new Intent(context, AppDataIntentService.class);
        work.setAction(ACTION_FIREBASE);
        enqueueWork(context, AppDataIntentService.class, JOB_ID, work);

    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG, "Executing work: " + intent);
        if (intent != null && intent.getAction() == ACTION_FIREBASE) {
            getStats(this);
        }
    }
}