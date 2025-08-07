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
        String tempAdvice;
        if(temp < 24 || temp > 28)
            Alert_Temperature();

        if(humidity < 70 || humidity > 85)
            Alert_Humidity();

        return Result.success();
    }

}
