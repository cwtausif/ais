package com.glowingsoft.testapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.ArrayList;

public class HomeActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mContext=this;
        mData=new ArrayList<>();
        getViews();
        isConnected();
        getQuestionsRequest();
        //loadRadioButtons();

    }
    public void getViews(){
        typeTv = findViewById(R.id.typeTv);
        questionNoTv = findViewById(R.id.questionNoTv);
        questionTv = findViewById(R.id.questionTv);
        nextBtn = findViewById(R.id.nextBtn);
        choicesGroup = findViewById(R.id.choicesGroup);

        nextBtn.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.logout:
                Log.d("response logout","logout button pressed");
                setSharedPreferences("userId", "");
                startActivity(new Intent(mContext,LoginActivity.class));
                finish();

                break;
        }
        return true;
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        HomeActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
