package com.setmine.android.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.setmine.android.SetMineMainActivity;
import com.setmine.android.util.HttpUtils;

import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/21/14.
 */
public class InitialApiCallAsyncTask extends AsyncTask<String, Integer, JSONObject> {

    SetMineMainActivity activity = null;
    HttpUtils apiCaller;
    String modelType;

    public InitialApiCallAsyncTask(SetMineMainActivity activity, Context context, String apiRoot) {
        this.activity = activity;
        this.apiCaller = new HttpUtils(context, apiRoot);
    }

    @Override
    protected void onPreExecute() {
        activity.asyncTasksInProgress++;
        Log.v("Task started: ", ((Integer)activity.asyncTasksInProgress).toString());
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        if(params[1] != null) {
            modelType = params[1];
        }
        String apiRequest = params[0];
        JSONObject jsonResponse = null;
        try {
            String jsonString = apiCaller.getJSONStringFromURL(apiRequest);
            jsonResponse = new JSONObject(jsonString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return jsonResponse;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        activity.asyncTasksInProgress--;
        Log.v("Task complete: ", ((Integer)activity.asyncTasksInProgress).toString());
        if(jsonObject != null) {
        } else {
            Toast.makeText(activity.getApplicationContext(), "Check your Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }
}
