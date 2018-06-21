package com.glowingsoft.testapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;


public class WebReq {
    protected SharedPreferences mPref;

    public static AsyncHttpClient client;
    static{
        client = new AsyncHttpClient();
//        client.addHeader("Authorizuser", GlobalClass.getInstance().mApiKey);
        client.addHeader("Cache-Control","no-cache");
        client.addHeader("Pragma","no-cache");
    }

    public static void get(Context context, String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        client.get(context, getAbsoluteUrl(url), params, responseHandler);
        Log.d("response url",url.toString());
    }

    public static void post(Context context, String url, RequestParams params, ResponseHandlerInterface responseHandler) {
        client.post(context, getAbsoluteUrl(url), params, responseHandler);
        Log.d("response url",url.toString());
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        Log.d("response url",GlobalClass.getInstance().BASE_URL + relativeUrl.toString());
        return GlobalClass.getInstance().BASE_URL + relativeUrl;
    }
}