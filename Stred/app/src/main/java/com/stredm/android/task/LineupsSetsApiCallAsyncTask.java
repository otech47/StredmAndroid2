package com.stredm.android.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.stredm.android.LineupsSetsApiCaller;
import com.stredm.android.SetMineMainActivity;
import com.stredm.android.util.HttpUtils;

import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/21/14.
 */
public class LineupsSetsApiCallAsyncTask extends AsyncTask<String, Integer, JSONObject> {

    SetMineMainActivity activity = null;
    HttpUtils apiCaller;
    String modelType;
    LineupsSetsApiCaller caller;

    public LineupsSetsApiCallAsyncTask(SetMineMainActivity activity, Context context, String apiRoot, LineupsSetsApiCaller caller) {
        this.activity = activity;
        this.apiCaller = new HttpUtils(context, apiRoot);
        this.caller = caller;
    }

    @Override
    protected void onPreExecute() {
        activity.asyncTasksInProgress++;
        Log.v("Task started. Still in queue: ", ((Integer) activity.asyncTasksInProgress).toString());
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        if(params[1] != null) {
            modelType = params[1];
        }
        String apiRequest = params[0];
        Log.v("JSONAPITask requested url", apiRequest);
        JSONObject jsonResponse = null;
        try {
            String jsonString = apiCaller.getJSONStringFromURL(apiRequest);
            Log.v("Returned JSON String", jsonString);
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
        Log.v("Task complete. Still in queue: ", ((Integer)activity.asyncTasksInProgress).toString());
        caller.onLineupsSetsReceived(jsonObject, modelType);
    }
}
