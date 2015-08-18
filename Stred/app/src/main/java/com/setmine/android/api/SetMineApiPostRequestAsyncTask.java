package com.setmine.android.api;

import android.os.AsyncTask;
import android.util.Log;

import com.setmine.android.Constants;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.interfaces.ApiCaller;
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

    final String TAG = "SetMineApiPostRequest";

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

        if(params.length > 2) {
            identifier = params[2];
        } else {
            identifier = params[1];
        }
        String route = params[0];
        String jsonPostDataString = params[1];
        if(route.contains("?")) {
            route += "&setmine_api_key=" + Constants.API_KEY;
        } else {
            route += "?setmine_api_key=" + Constants.API_KEY;
        }

        JSONObject jsonResponse = null;
        Log.d(TAG, route);
        Log.d(TAG, jsonPostDataString);

        // Use the HttpUtil to retrieve a JSON string from the SetMine API
        try {
            String jsonString = httpUtil.postApiRequest(route, jsonPostDataString);
            Log.d(TAG, jsonString);
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
