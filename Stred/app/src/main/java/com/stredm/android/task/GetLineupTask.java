package com.stredm.android.task;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.GsonBuilder;
import com.stredm.android.EventDetailFragment;
import com.stredm.android.util.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/22/14.
 */
public class GetLineupTask extends AsyncTask<String, Integer, JSONObject> {

    private JSONObject response = null;
    private HttpUtils apiCallsManager;
    public Context context;
    public EventDetailFragment eventDetailFragment;
    public GsonBuilder gsonBuilder = new GsonBuilder();
    private static final int API_VERSION = 1;
    private String apiUrl = "http://stredm.com/api/v/" + API_VERSION + "/";

    public GetLineupTask(Context context, EventDetailFragment eventDetailFragment) {
        this.eventDetailFragment = eventDetailFragment;
        apiCallsManager = new HttpUtils(context, apiUrl);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        String jsonString = apiCallsManager.getJSONStringFromURL(params[0]);
        try {
            response = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
    }
}
