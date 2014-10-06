package com.stredm.android.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.stredm.android.InitialApiCaller;
import com.stredm.android.util.HttpUtils;

import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/21/14.
 */
public class JsonApiCallTask extends AsyncTask<String, Integer, JSONObject> {

    public JSONObject placeResponse;
    public JSONObject placeDetailResponse;
    private Context context;
    private HttpUtils apiCallsManager;
    public GsonBuilder gsonBuilder;
    private InitialApiCaller caller;
    public String detail;

    public JsonApiCallTask(Context context, String rootApiUrl, InitialApiCaller caller) {
        this.context = context;
        this.apiCallsManager = new HttpUtils(context, rootApiUrl);
        this.gsonBuilder = new GsonBuilder();
        this.caller = caller;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        JSONObject response = null;
        if(params.length > 1) {
            detail = params[1];
            try {
                String apiRequest = params[0];
                Log.v("url", apiRequest);
                String jsonString = apiCallsManager.getJSONStringFromURL(apiRequest);
                Log.v("detail jsonString in task", jsonString);
                response = new JSONObject(jsonString);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            detail = "";
            try {
                String apiRequest = params[0];
                Log.v("url", apiRequest);
                String jsonString = apiCallsManager.getJSONStringFromURL(apiRequest);
                Log.v("jsonString in task", jsonString);
                response = new JSONObject(jsonString);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        Log.v("ON post ex", response.toString());
        if(detail.equals("detail")) {
            placeDetailResponse = response;
        }
        else {
            placeResponse = response;
        }

    }


}
