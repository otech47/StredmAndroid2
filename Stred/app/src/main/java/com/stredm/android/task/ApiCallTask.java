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
import com.stredm.android.R;
import com.stredm.android.util.HttpUtils;

import java.io.InputStreamReader;


public class ApiCallTask extends AsyncTask<String, Integer, ApiResponse> {

    private ApiResponse response;
    private Context context;
    private HttpUtils apiCallsManager;
    private EventPageFragment epFragment;
    public GsonBuilder gsonBuilder = new GsonBuilder();

    public ApiCallTask(Context context, EventPageFragment fragment) {
        this.context = context;
        this.epFragment = fragment;
        apiCallsManager = new HttpUtils(context);
    }

    @Override
    protected ApiResponse doInBackground(String... params) {
        InputStreamReader reader = apiCallsManager.getReaderFromURL(params[0]);
        gsonBuilder.registerTypeAdapter(ApiResponse.class, new ApiResponseDeserializer());
        gsonBuilder.registerTypeAdapter(Payload.class, new PayloadDeserializer());
        Gson gson = gsonBuilder.create();
        response = gson.fromJson(reader, ApiResponse.class);
        return response;
    }

    @Override
    protected void onPostExecute(ApiResponse apiResponse) {
//        ((EventPagerActivity) epFragment.getActivity()).cache.addToCache(apiResponse);
        TileGenerator tileGen = new TileGenerator(context);
        tileGen.modelsToViews(apiResponse.payload.featured, epFragment.getView().findViewById(R.id.eventsList));
    }
}
