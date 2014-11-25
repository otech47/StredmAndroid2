package com.setmine.android.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.setmine.android.OnTaskCompleted;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.object.Set;
import com.setmine.android.util.HttpUtils;

import org.json.JSONObject;

public class GetSetsTask extends AsyncTask<String, Integer, JSONObject> {

    SetMineMainActivity activity = null;
    HttpUtils apiCaller;
    String modelType;
    OnTaskCompleted<Set> listener;

    public GetSetsTask(SetMineMainActivity activity, Context context,
                       String apiRoot, OnTaskCompleted<Set> listener) {
        this.activity = activity;
        this.apiCaller = new HttpUtils(context, apiRoot);
        this.listener = listener;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        if(params[1] != null) {
            modelType = params[1];
        }
        String apiRequest = params[0];
        JSONObject jsonResponse = null;
        try {
            if(!this.isCancelled()) {
                String jsonString = apiCaller.getJSONStringFromURL(apiRequest);
                jsonResponse = new JSONObject(jsonString);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return jsonResponse;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        Log.v("Task complete. Still in queue: ", ((Integer)activity.asyncTasksInProgress).toString());
        if(jsonObject != null) {
            activity.modelsCP.setModel(jsonObject, modelType);
            listener.onTaskCompleted(activity.modelsCP.getSearchedSets());
        } else {

        }
    }
}
