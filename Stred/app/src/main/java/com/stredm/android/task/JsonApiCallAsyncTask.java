package com.stredm.android.task;

import android.os.AsyncTask;

import com.stredm.android.EventPagerActivity;

import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/21/14.
 */
public class JsonApiCallAsyncTask extends AsyncTask<String, Integer, JSONObject> {

    EventPagerActivity epActivity = null;

    @Override
    protected JSONObject doInBackground(String... params) {
        return null;
    }
}
