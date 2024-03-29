package com.setmine.android;

import android.content.Context;
import android.os.AsyncTask;

import com.setmine.android.util.HttpUtils;

/**
 * Created by oscarlafarga on 10/29/14.
 */
public class CountPlaysTask extends AsyncTask<String, Integer, String> {

    HttpUtils http;
    String serverUrl;

    public CountPlaysTask(Context c) {
        this.serverUrl = Constants.PUBLIC_ROOT_URL;
        http = new HttpUtils(c, serverUrl);
    }

    @Override
    protected String doInBackground(String... params) {
        http.sendPlayCountGetRequest(params[0]);
        return null;
    }
}
