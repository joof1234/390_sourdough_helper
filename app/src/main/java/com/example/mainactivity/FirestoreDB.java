package com.example.mainactivity;

import android.app.Application;

import com.google.firebase.FirebaseApp;

//this class makes it so that we don't need to manage too much for our database.
//later we need to make this our MVC, add the functions to call directly from here.
//on application start, this will connect to firebase to avoid issues when moving from
//activity to another activity.
public class FirestoreDB extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //connect to firebase.
        FirebaseApp.initializeApp(this);
    }
}
