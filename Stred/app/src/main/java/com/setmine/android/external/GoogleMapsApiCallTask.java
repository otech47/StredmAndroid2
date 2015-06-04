package com.setmine.android.external;

import android.content.Context;
import android.os.AsyncTask;

import com.setmine.android.event.EventDetailFragment;
import com.setmine.android.util.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/23/14.
 */
public class GoogleMapsApiCallTask extends AsyncTask<String, Integer, JSONObject> {

    private JSONObject response = null;
    private HttpUtils apiCallsManager;
    public Context context;
    public EventDetailFragment eventDetailFragment;
    private static final int API_VERSION = 1;
    private String apiUrl = "http://setmine.com/api/v/" + API_VERSION + "/";

    public GoogleMapsApiCallTask(Context context, EventDetailFragment eventDetailFragment) {
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
