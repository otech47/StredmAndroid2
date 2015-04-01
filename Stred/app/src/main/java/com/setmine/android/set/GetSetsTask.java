package com.setmine.android.set;

import android.content.Context;
import android.os.AsyncTask;

import com.setmine.android.interfaces.OnTaskCompleted;
import com.setmine.android.SetMineMainActivity;
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
        if(jsonObject != null) {
            activity.modelsCP.setModel(jsonObject, modelType);
            listener.onTaskCompleted(activity.modelsCP.getSearchedSets());
        } else {

        }
    }
}
