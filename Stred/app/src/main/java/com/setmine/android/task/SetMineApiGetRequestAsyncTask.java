package com.setmine.android.task;

import android.os.AsyncTask;
import android.util.Log;

import com.setmine.android.ApiCaller;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.Constants;
import com.setmine.android.util.HttpUtils;

import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/21/14.
 */

public class SetMineApiGetRequestAsyncTask extends AsyncTask<String, Integer, JSONObject> {

    SetMineMainActivity activity = null;
    HttpUtils httpUtil;
    String identifier;
    ApiCaller apiCaller;

    final String TAG = "SetMineGetTask";

    // Create the Task by passing in the activity and the ApiCaller that is creating the task

    public SetMineApiGetRequestAsyncTask(SetMineMainActivity activity, ApiCaller apiCaller) {
        this.activity = activity;
        this.httpUtil = new HttpUtils(activity.getApplicationContext(), Constants.API_ROOT_URL);
        this.apiCaller = apiCaller;
    }

    // Count the async tasks for debugging and logging purposes. Executed on main thread.

    @Override
    protected void onPreExecute() {
        activity.asyncTasksInProgress++;
        Log.d(TAG, "Task started: "+ ((Integer)activity.asyncTasksInProgress).toString());
    }

    // Pass in the api route needed with SetMineApiGetRequestAsyncTask.executeOnExecutor(apiRoute)

    @Override
    protected JSONObject doInBackground(String... params) {

        // If there is a second parameter, then we are retrieving a model that needs to be stored

        if(params[1] != null) {
            identifier = params[1];
        }
        String apiRequest = params[0];
        JSONObject jsonResponse = null;

        // Use the HttpUtil to retrieve a JSON string from the SetMine API
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
        Log.d(TAG, identifier + " task complete: "+ ((Integer)activity.asyncTasksInProgress).toString());
        if(response != null) {
            apiCaller.onApiResponseReceived(response, identifier);
        } else {
        }

    }


}
