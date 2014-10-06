package com.stredm.android;

import com.stredm.android.object.Artist;
import com.stredm.android.object.Event;
import com.stredm.android.object.Genre;
import com.stredm.android.object.Lineup;
import com.stredm.android.object.Mix;
import com.stredm.android.object.Set;
import com.stredm.android.object.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ModelsContentProvider {

    private List<Artist> artists = null;
    private List<Event> events = null;
    private List<Mix> mixes = null;
    private List<Genre> genres = null;
    public List<Set> allSets;
    private List<Set> popularSets = null;
    private List<Set> recentSets = null;
    private List<Track> tracks = null;
    public List<Lineup> lineups;
    public List<Event> upcomingEvents;
    public List<Event> recentEvents;
    public List<Event> searchEvents;
    public boolean initialModelsReady = false;

    public ModelsContentProvider() {
        upcomingEvents = new ArrayList<Event>();
        recentEvents = new ArrayList<Event>();
        searchEvents = new ArrayList<Event>();
        lineups = new ArrayList<Lineup>();
        allSets = new ArrayList<Set>();
    }

    public void setModel(JSONObject model, String modelName) {
        JSONObject payload;
        try {
            if(model.getString("status").equals("success")) {
                payload = model.getJSONObject("payload");
                if(modelName.equals("upcomingEvents")) {
                    JSONObject upcoming = payload.getJSONObject("upcoming");
                    JSONArray soonest = upcoming.getJSONArray("soonestEvents");
                    for(int i = 0 ; i < soonest.length() ; i++) {
                        upcomingEvents.add(new Event(soonest.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("recentEvents")) {
                    JSONArray recent = payload.getJSONArray("featured");
                    for(int i = 0 ; i < recent.length() ; i++) {
                        recentEvents.add(new Event(recent.getJSONObject(i)));
                    }
                }
                if(modelName.equals("searchEvents")) {
                    JSONObject upcoming = payload.getJSONObject("upcoming");
                    JSONArray closest = upcoming.getJSONArray("closestEvents");
                    for(int i = 0 ; i < closest.length() ; i++) {
                        searchEvents.add(new Event(closest.getJSONObject(i)));
                    }
                }
                if(modelName.equals("lineups")) {
                    lineups.add(new Lineup(payload));
                }
                if(modelName.equals("sets")) {
                    JSONObject festival = payload.getJSONObject("festival");
                    JSONArray sets = festival.getJSONArray("sets");
                    for(int i = 0 ; i < sets.length() ; i++) {
                        allSets.add(new Set(sets.getJSONObject(i)));
                    }
                }
            }
            else {
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public List<Set> getAllSets() {
        return allSets;
    }

    public Lineup getLastLineup() {
        return lineups.get(lineups.size()-1);
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
