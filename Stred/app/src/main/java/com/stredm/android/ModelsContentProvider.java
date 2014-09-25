package com.stredm.android;

import com.google.gson.Gson;
import com.stredm.android.object.Artist;
import com.stredm.android.object.Event;
import com.stredm.android.object.Genre;
import com.stredm.android.object.Mix;
import com.stredm.android.object.Set;
import com.stredm.android.object.Track;

import java.util.List;

public class ModelsContentProvider {

    private Artist[] artists = null;
    private Event[] events = null;
    private Mix[] mixes = null;
    private Genre[] genres = null;
    private Set[] popularSets = null;
    private Set[] recentSets = null;
    private Track[] tracks = null;
    public List<Model> upcomingEvents = null;
    public List<Model> recentEvents = null;
    public List<Model> searchEvents = null;
//    private HttpUtils apiCallsManager = new HttpUtils(getContext());
    private Gson gson = new Gson();



    public void setModel(List<Model> model, String modelName) {
        if(modelName.equals("upcomingEvents")) {
            upcomingEvents = model;
        }
        if(modelName.equals("recentEvents")) {
            recentEvents = model;
        }
        if(modelName.equals("searchEvents")) {
            searchEvents = model;
        }
    }


//    @Override
//    public boolean onCreate() {
        // Initialize only upcomingEvents and recentEvents through API call
//        EventApiCallTask recentEventsCall = new EventApiCallTask(getContext());
//        try {
//            ApiResponse response = recentEventsCall.execute("featured").get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        EventApiCallTask upcomingEventsCall = new EventApiCallTask(getContext());
//        try {
//            ApiResponse response = recentEventsCall.execute("upcoming").get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        return true;
//    }

}
