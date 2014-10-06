package com.stredm.android.task;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stredm.android.ApiResponse;
import com.stredm.android.ApiResponseDeserializer;
import com.stredm.android.EventPageFragment;
import com.stredm.android.Payload;
import com.stredm.android.PayloadDeserializer;
import com.stredm.android.UpcomingModel;
import com.stredm.android.UpcomingModelDeserializer;
import com.stredm.android.util.HttpUtils;

import java.io.InputStreamReader;


public class EventApiCallTask extends AsyncTask<String, Integer, ApiResponse> {

    private ApiResponse response;
    private Context context;
    private HttpUtils apiCallsManager;
    private EventPageFragment epFragment;
    public GsonBuilder gsonBuilder = new GsonBuilder();
    private static final int API_VERSION = 1;
    private String apiUrl = "http://stredm.com/api/v/" + API_VERSION + "/";

    public EventApiCallTask(Context context, EventPageFragment fragment) {
        this.context = context;
        this.epFragment = fragment;
        apiCallsManager = new HttpUtils(context, apiUrl);
    }

    @Override
    protected ApiResponse doInBackground(String... params) {
        InputStreamReader reader = apiCallsManager.getReaderFromURL(params[0]);
        gsonBuilder.registerTypeAdapter(ApiResponse.class, new ApiResponseDeserializer());
        gsonBuilder.registerTypeAdapter(Payload.class, new PayloadDeserializer());
        gsonBuilder.registerTypeAdapter(UpcomingModel.class, new UpcomingModelDeserializer());
        Gson gson = gsonBuilder.create();
        response = gson.fromJson(reader, ApiResponse.class);
        apiCallsManager.closeReader();
        return response;
    }

    @Override
    protected void onPostExecute(ApiResponse apiResponse) {
    }
}
