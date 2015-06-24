package com.setmine.android.api;

import android.content.Context;
import android.os.AsyncTask;

import com.setmine.android.SetMineMainActivity;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.util.HttpUtils;

import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/21/14.
 */
public class ApiCallAsyncTask extends AsyncTask<String, Integer, JSONObject> {

    SetMineMainActivity activity = null;
    HttpUtils apiCaller;
    String modelType;
    ApiCaller taskCaller;

    public ApiCallAsyncTask(SetMineMainActivity activity, Context context, String apiRoot, ApiCaller caller) {
        this.activity = activity;
        this.apiCaller = new HttpUtils(context, apiRoot);
        this.taskCaller = caller;
    }

    @Override
    protected void onPreExecute() {
        activity.asyncTasksInProgress++;
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
        taskCaller.onApiResponseReceived(jsonObject, modelType);
    }
}
