package com.glowingsoft.testapp;


import android.app.Application;
import android.content.Context;
import android.location.LocationManager;
import android.util.Log;

public class GlobalClass extends Application {

//   public static String BASE_URL = "http://glowingsoft.com/ais/";
   //this url should have been used
   public static String BASE_URL ="http://valisbioscience.com/ais/";
    private static GlobalClass singleton;

    public static GlobalClass getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        Log.d("application","start");
    }

}
