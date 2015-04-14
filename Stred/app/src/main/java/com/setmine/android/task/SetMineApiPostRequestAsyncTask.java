package com.setmine.android.task;

import android.os.AsyncTask;

import com.setmine.android.ApiCaller;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.Constants;
import com.setmine.android.util.HttpUtils;

import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/21/14.
 */

public class SetMineApiPostRequestAsyncTask extends AsyncTask<String, Integer, JSONObject> {

    SetMineMainActivity activity = null;
    HttpUtils httpUtil;
    String identifier;
    ApiCaller apiCaller;

    final String TAG = "SetMineApiPostRequestAsyncTask";

    // Create the Task by passing in the activity and the ApiCaller that is creating the task

    public SetMineApiPostRequestAsyncTask(SetMineMainActivity activity, ApiCaller apiCaller) {
        this.activity = activity;
        this.httpUtil = new HttpUtils(activity.getApplicationContext(), Constants.API_ROOT_URL);
        this.apiCaller = apiCaller;
    }

    // Count the async tasks for debugging and logging purposes. Executed on main thread.

    @Override
    protected void onPreExecute() {
    }

    // Pass in the api route needed with SetMineApiGetRequestAsyncTask.executeOnExecutor(apiRoute)
    // First parameter: API route
    // Second parameter: POST Data as JSON String
    // Third Parameter: Identifier for the response data

    @Override
    protected JSONObject doInBackground(String... params) {

        // If there is a third parameter, then we are retrieving a model that needs to be stored

        if(params[2] != null) {
            identifier = params[2];
        }
        String route = params[0];
        String jsonPostDataString = params[1];
        JSONObject jsonResponse = null;

        // Use the HttpUtil to retrieve a JSON string from the SetMine API
        try {
            String jsonString = httpUtil.postApiRequest(route, jsonPostDataString);
            jsonResponse = new JSONObject(jsonString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return jsonResponse;
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        if(response != null) {
            apiCaller.onApiResponseReceived(response, identifier);
        } else {
        }
        super.onPostExecute(response);
    }


}
