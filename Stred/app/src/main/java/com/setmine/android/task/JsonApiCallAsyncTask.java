package com.setmine.android.task;

import android.os.AsyncTask;
import android.util.Log;

import com.setmine.android.ApiCaller;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.util.HttpUtils;

import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/21/14.
 */
public class JsonApiCallAsyncTask extends AsyncTask<String, Integer, JSONObject> {

    SetMineMainActivity activity = null;
    HttpUtils httpUtil;
    String modelType;
    ApiCaller apiCaller;

    final String TAG = "JsonApiCallAsyncTask";

    public JsonApiCallAsyncTask(SetMineMainActivity activity, ApiCaller apiCaller) {
        this.activity = activity;
        this.httpUtil = new HttpUtils(activity.getApplicationContext(), activity.API_ROOT_URL);
        this.apiCaller = apiCaller;
    }

    @Override
    protected void onPreExecute() {
        activity.asyncTasksInProgress++;
        Log.d(TAG, "Task started. Still in queue: "+ ((Integer)activity.asyncTasksInProgress).toString());
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        if(params[1] != null) {
            modelType = params[1];
        }
        String apiRequest = params[0];
        JSONObject jsonResponse = null;
        try {
            String jsonString = httpUtil.getJSONStringFromURL(apiRequest);
            jsonResponse = new JSONObject(jsonString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return jsonResponse;
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        activity.asyncTasksInProgress--;
        Log.d(TAG, "Task complete. Still in queue: "+ ((Integer)activity.asyncTasksInProgress).toString());
        if(response != null) {
            apiCaller.onApiResponseReceived(response, modelType);
        } else {
//            Toast.makeText(activity.getApplicationContext(), "Check your Internet Connection", Toast.LENGTH_SHORT).show();
        }

    }


}
