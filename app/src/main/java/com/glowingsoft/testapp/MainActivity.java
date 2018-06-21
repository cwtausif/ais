package com.glowingsoft.testapp;

import android.app.Activity;
import android.app.MediaRouteButton;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //region variables
    public EditText emailEt,passwordEt;
    public Button loginBtn,nextBtn;
    public Context mContext;
    String email,password;
    public boolean mReqFlag=true;
    private int requestType=1;
    private ProgressBar mPb;
    private SharedPreferences mPref;
    private String mPrefName="aisPref";
    int totalQuestions = 0;
    ArrayList<QuestionModel> mData;
    private int currentQuestion=0;
    protected RadioGroup choicesGroup;
    protected TextView questionTv,questionNoTv,typeTv;
    protected String[] choices = {"Never","Rarely","Sometimes","Very Often","Always"};
    private String emailText="";
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id==R.id.loginBtn){
            email = emailEt.getText().toString();
            password = passwordEt.getText().toString();

            if (email.length()==0){
                showToast("Email can't be empty",false);
                return;
            }
            if (!emailValidator(email)){
                showToast("Please enter valid email",false);
            }
            if (password.length()==0){
                showToast("password can't be empty",false);
                return;
            }


            if (isConnected()){
                    loginRequest();
            }

        }else if(id==R.id.nextBtn){
            int choice = choicesGroup.indexOfChild(findViewById(choicesGroup.getCheckedRadioButtonId()));
            Log.d("response ", nextBtn.getText().toString() + " " + " Choice: " + choice);
            mData.get(currentQuestion).setChoice(choices[choice]);

            if (nextBtn.getText().toString().equals("Submit")){
                Log.d("response ","Submit Data");

                emailText += "Awareness of Illness Scale\n";
                emailText += "Questionnaire Filled By:\t"+retrivePreferencesValues("emailais")+"\n";
                emailText +=" \nAll Questions and Answers \n";
                for (int i = 0; i < mData.size(); i++) {
                    emailText +="\n\n"+mData.get(i).getTitle()+": "+mData.get(i).question+"\n";
                    emailText +="Answer Choice: "+mData.get(i).choice+"\n\n";
                }
                emailText +="\n Thanks \n";
                Log.d("response ","Submit Data"+emailText);
                sendEmail();
            }else {
                currentQuestion = currentQuestion + 1;
                loadQuestion();
            }
        }
    }

    //region server requests
    public void sendEmail(){
        if (mReqFlag){
            RequestParams mParams = new RequestParams();
            mParams.put("from",retrivePreferencesValues("emailais"));
            mParams.put("data",emailText);
            requestType = 3;
            WebReq.post(mContext,"email.php",mParams,new MyTextHttpResponseHandler());
        }else{
            showToast("Request Already in Progress",false);
        }
    }
    public void loginRequest(){
        if (mReqFlag){
            RequestParams mParams = new RequestParams();
            mParams.put("email",email);
            mParams.put("password",password);
            requestType = 1;
            WebReq.post(mContext,"applogin.php",mParams,new MyTextHttpResponseHandler());
        }else{
            showToast("Request Already in Progress",false);
        }
    }
    public void getQuestionsRequest(){
        if (mReqFlag){
            RequestParams mParams = new RequestParams();
            requestType = 2;
            WebReq.get(mContext,"app.php",mParams,new MyTextHttpResponseHandler());
        }else{
            showToast("Request Already in Progress",false);
        }
    }


    class MyTextHttpResponseHandler extends JsonHttpResponseHandler {
        MyTextHttpResponseHandler() {
        }

        @Override
        public void onStart() {
            super.onStart();
            mReqFlag = false;
            if (mPb != null) {
                mPb.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onFinish() {
            super.onFinish();
            mReqFlag = true;
            if (mPb != null) {
                mPb.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            super.onFailure(statusCode, headers, throwable, errorResponse);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            super.onFailure(statusCode, headers, responseString, throwable);
            Log.d("response error on fail",responseString+" "+throwable);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, final JSONObject mResponse) {
            if (mPb!=null) {
                mPb.setVisibility(View.INVISIBLE);
            }
            Log.d("response",mResponse.toString()+"");
            try{
                if (mResponse.getBoolean("status")) {
                    switch (requestType){
                        case 1:
                            setSharedPreferences("userId","1");
                            setSharedPreferences("emailais",email);
                            showToast(mResponse.getString("message"),false);
                            startActivity(new Intent(mContext,HomeActivity.class));
                            finish();
                            break;
                        case 2:
                            JSONArray data = mResponse.getJSONArray("data");
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject questionObj = data.getJSONObject(i);
                                Log.d("response ",questionObj+" question Object");
                                QuestionModel questionModel = new QuestionModel();
                                questionModel.setId(questionObj.getString("id"));
                                questionModel.setQuestion(questionObj.getString("question"));
                                questionModel.setTitle(questionObj.getString("title"));
                                questionModel.setPart(questionObj.getString("part"));
                                questionModel.setNext(questionObj.getString("next"));
                                mData.add(questionModel);
                            }
                                currentQuestion = 0;
                                loadQuestion();
                                showToast("Total Questions: "+mData.size(),false);

                            break;
                        case 3:
                            showToast(mResponse.getString("message"),false);
                            askToSubmitAnotherResponse();
                            break;

                    }
                } else {
                    showToast(mResponse.getString("message"),false);
                }
            }catch (Exception e){
                e.printStackTrace();
                showToast("Request is Success But Empty Data",false);
            }
        }
    }
//endregion

    //region functions
    public void setSharedPreferences(String key, String value) {
        mPref = getSharedPreferences(mPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public String retrivePreferencesValues(String key) {
        mPref = getSharedPreferences(mPrefName, Context.MODE_PRIVATE);
        String storeValue = mPref.getString(key, "");
        return storeValue;
    }
    public  boolean isLoggedIn() {
        String session = retrivePreferencesValues("userId");
        boolean loginStatus = false;
        if (session != null && !session.equals("")) {
            loginStatus = true;
        }
        return loginStatus;
    }
    public boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            //networkConnectionFailed();
            new AlertDialog.Builder(this)
                    .setMessage("Please check your Internet Connection and try again !!!")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (mContext instanceof Activity){
                                finish();
                            }
                        }
                    })

                    .show();
            return false;
    }
    public void askToSubmitAnotherResponse(){
        new AlertDialog.Builder(this)
                .setMessage("Thank You")
                .setCancelable(false)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mContext instanceof Activity){
                            finish();
                        }
                    }
                })

                .show();
    }
    public void networkConnectionFailed() {
        showToast("Network Connection Failed", false);
    }
    public void showToast(final String message, final boolean length) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (length) {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                } else  {
                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public boolean emailValidator(String email) {
        Pattern pattern;
        Matcher matcher;
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }


    public void loadQuestion(){
        showToast("current question: " +currentQuestion,false);
        QuestionModel questionModel = mData.get(currentQuestion);
        try {
            typeTv.setText(questionModel.getPart());
            questionNoTv.setText(questionModel.getTitle());
            questionTv.setText(questionModel.getQuestion());
            nextBtn.setText(questionModel.getNext());
        }catch (Exception e){
            e.printStackTrace();
        }
        removeAllRadioButtons();
        loadRadioButtons();
    }
    public void loadRadioButtons(){
        for (int i=0; i<choices.length; i++) {
            RadioButton rdbtn = new RadioButton(this);
            rdbtn.setId(i);
            rdbtn.setText(choices[i]);
            if (i==0){
                rdbtn.setChecked(true);
            }
            choicesGroup.addView(rdbtn);
        }
    }
    public void removeAllRadioButtons(){
        int count = choicesGroup.getChildCount();
        if(count>0) {
            for (int i=count-1;i>=0;i--) {
                View o = choicesGroup.getChildAt(i);
                if (o instanceof RadioButton) {
                    choicesGroup.removeViewAt(i);
                }
            }
        }
    }
    //endregion




}
