package com.example.mainactivity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class BackgroundRoutine extends Worker{
    public BackgroundRoutine(@NonNull Context context, @NonNull WorkerParameters params){
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork(){
        //TODO: Firebase polling for data
        //Need temp variable from Firebase
        //Need humidity variable from Firebase

        Context context = getApplicationContext();
        NotificationHelper notificationHelper = new NotificationHelper(context);
        if(temp < 24 || temp > 28)
            notificationHelper.Alert_Temperature();

        if(humidity < 70 || humidity > 85)
            notificationHelper.Alert_Humidity();

        return Result.success();
    }

}
