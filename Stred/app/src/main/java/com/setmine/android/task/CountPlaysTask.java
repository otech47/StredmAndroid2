package com.setmine.android.task;

import android.os.AsyncTask;

import com.setmine.android.SetMineMainActivity;
import com.setmine.android.util.HttpUtils;

/**
 * Created by oscarlafarga on 10/29/14.
 */
public class CountPlaysTask extends AsyncTask<String, Integer, String> {

    HttpUtils http;
    String serverUrl;

    public CountPlaysTask(SetMineMainActivity activity) {
        this.serverUrl = activity.PUBLIC_ROOT_URL;
        http = new HttpUtils(activity.getApplicationContext(), serverUrl);
    }

    @Override
    protected String doInBackground(String... params) {
        http.sendPlayCountGetRequest(params[0]);
        return null;
    }
}
