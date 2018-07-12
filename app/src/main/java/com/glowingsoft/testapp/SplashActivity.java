package com.glowingsoft.testapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash);

        Intent intent = new Intent(SplashActivity.this, Receiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(SplashActivity.this, 100, intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(am.RTC_WAKEUP, System.currentTimeMillis(), am.INTERVAL_DAY*7, pendingIntent);

        mContext=this;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
            if (isLoggedIn()){
                startActivity(new Intent(SplashActivity.this,HomeActivity.class));
                finish();
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
            }
        },3000);
    }
}
