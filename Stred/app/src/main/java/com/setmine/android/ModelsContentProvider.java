package com.setmine.android;

import android.util.Log;

import com.setmine.android.api.Activity;
import com.setmine.android.artist.Artist;
import com.setmine.android.event.Event;
import com.setmine.android.event.Lineup;
import com.setmine.android.genre.Genre;
import com.setmine.android.object.TrackResponse;
import com.setmine.android.set.Mix;
import com.setmine.android.set.Set;
import com.setmine.android.track.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModelsContentProvider {

    private static final String TAG = "ModelsContentProvider";

    private int readyModels;

    private List<Artist> artists;
    private List<Artist> allArtists;
    private List<Event> events; //all past events
    private List<Mix> mixes;
    private List<Genre> genres;
    public List<Set> searchedSets;
    public List<Set> allSets;
    private List<Set> popularSets;
    private List<Set> recentSets;
    private List<Track> tracks = null;
    public List<TrackResponse> searchedTracks;
    public HashMap<String, Lineup> lineups;
    public List<Event> soonestEvents;//upcoming events
    public List<Event> closestEvents;
    public List<Event> soonestEventsAroundMe;
    public List<Event> upcomingEvents;
    public List<Event> searchedUpcomingEvents;
    public List<Event> recentEvents;
    public List<Event> searchEvents;
    public List<Activity> activities;
    public HashMap<String, List<Set>> detailSets;
    public HashMap<String, List<Event>> detailEvents;
    public boolean initialModelsReady = false;

    public HashMap<String, String> jsonMappings;

    public ModelsContentProvider() {
        soonestEvents = new ArrayList<Event>();
        closestEvents = new ArrayList<Event>();
        soonestEventsAroundMe = new ArrayList<Event>();
        upcomingEvents = new ArrayList<Event>();
        searchedUpcomingEvents = new ArrayList<Event>();
        recentEvents = new ArrayList<Event>();
        searchEvents = new ArrayList<Event>();
        lineups = new HashMap<String, Lineup>();
        allSets = new ArrayList<Set>();
        artists = new ArrayList<Artist>();
        allArtists = new ArrayList<Artist>();
        events = new ArrayList<Event>();
        mixes = new ArrayList<Mix>();
        genres = new ArrayList<Genre>();
        searchedSets = new ArrayList<Set>();
        popularSets = new ArrayList<Set>();
        recentSets = new ArrayList<Set>();
        detailSets = new HashMap<String, List<Set>>();
        detailEvents = new HashMap<String, List<Event>>();
        activities = new ArrayList<Activity>();
        searchedTracks = new ArrayList<TrackResponse>();
        jsonMappings = new HashMap<String, String>();
    }

    public HashMap<String, String> getJsonMappings() {
        return jsonMappings;
    }

    public String convertModelsToString() {
        String jsonStringToSave = "";
        jsonStringToSave = jsonMappings.toString();
        Log.d(TAG, jsonStringToSave);
        return jsonStringToSave;
//        List<Object> keys = new ArrayList<Object>(Arrays.asList(jsonMappings.keySet().toArray()));
//        for(int i = 0; i < jsonMappings.keySet().size(); i++) {
//            jsonStringToSave = jsonStringToSave + "{'" + keys.get(i) + "':"
//                    + jsonMappings.get(keys.get(i)) + "}";
//        }
    }

    public void setModel(JSONObject model, String modelName) {
        JSONObject payload;
        try {

            if(model.getString("status").equals("success")) {
                payload = model.getJSONObject("payload");
                if(modelName.equals("upcomingEvents")) {
                    JSONObject upcoming = payload.getJSONObject("upcoming");
                    JSONArray soonest = upcoming.getJSONArray("soonestEvents");
                    JSONArray closest = upcoming.getJSONArray("closestEvents");
                    JSONArray around = upcoming.getJSONArray("soonestEventsAroundMe");
                    jsonMappings.put("upcomingEvents", model.toString());
                    jsonMappings.put("soonestEvents", model.toString());
                    jsonMappings.put("closestEvents", model.toString());
                    jsonMappings.put("soonestEventsAroundMe", model.toString());
                    for(int i = 0 ; i < soonest.length() ; i++) {
                        soonestEvents.add(new Event(soonest.getJSONObject(i)));
                    }
                    for(int i = 0 ; i < closest.length() ; i++) {
                        closestEvents.add(new Event(closest.getJSONObject(i)));
                    }
                    for(int i = 0 ; i < around.length() ; i++) {
                        soonestEventsAroundMe.add(new Event(around.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("recentEvents")) {
                    jsonMappings.put(modelName, model.toString());
                    JSONArray recent = payload.getJSONArray("featured");
                    for(int i = 0 ; i < recent.length() ; i++) {
                        recentEvents.add(new Event(recent.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("searchEvents")) {
                    jsonMappings.put(modelName, model.toString());
                    JSONObject upcoming = payload.getJSONObject("upcoming");
                    JSONArray around = upcoming.getJSONArray("closestEvents");
                    for(int i = 0 ; i < around.length() ; i++) {
                        searchEvents.add(new Event(around.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("sets")) {
                    JSONObject festival = payload.getJSONObject("festival");
                    JSONArray sets = festival.getJSONArray("sets");
                    for(int i = 0 ; i < sets.length() ; i++) {
                        allSets.add(new Set(sets.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("artists")) {
                    jsonMappings.put(modelName, model.toString());
                    JSONArray artistsArray = payload.getJSONArray("artist");
                    for(int i = 0 ; i < artistsArray.length() ; i++) {
                        artists.add(new Artist(artistsArray.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("festivals")) {
                    jsonMappings.put("events", model.toString());
                    JSONArray allFestivals = payload.getJSONArray("festival");
                    for(int i = 0 ; i < allFestivals.length() ; i++) {
                        events.add(new Event(allFestivals.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("mixes")) {
                    jsonMappings.put(modelName, model.toString());
                    JSONArray allMixes = payload.getJSONArray("mix");
                    for(int i = 0 ; i < allMixes.length() ; i++) {
                        mixes.add(new Mix(allMixes.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("genres")) {
                    jsonMappings.put(modelName, model.toString());
                    JSONArray allGenres = payload.getJSONArray("genre");
                    for(int i = 0 ; i < allGenres.length() ; i++) {
                        Genre newGenre = new Genre();
                        newGenre.setGenre(allGenres.getString(i));
                        genres.add(newGenre);
                    }
                }
                else if(modelName.equals("searchedSets")) {
                    JSONArray sets = payload.getJSONObject("search").getJSONArray("sets");
                    searchedSets.clear();
                    for(int i = 0 ; i < sets.length() ; i++) {
                        searchedSets.add(new Set(sets.getJSONObject(i)));
                    }
                    JSONArray upcomingEvents = payload.getJSONObject("search").getJSONArray("upcomingEvents");
                    searchedUpcomingEvents.clear();
                    for(int i = 0 ; i < upcomingEvents.length() ; i++) {
                        searchedUpcomingEvents.add(new Event(upcomingEvents.getJSONObject(i)));
                    }
                    JSONArray tracks = payload.getJSONObject("search").getJSONArray("tracks");
                    searchedTracks.clear();
                    for(int i = 0 ; i < tracks.length() ; i++) {
                        searchedTracks.add(new TrackResponse(tracks.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("popularSets")) {
                    jsonMappings.put(modelName, model.toString());
                    JSONArray sets = payload.getJSONArray("popular");
                    for(int i = 0 ; i < sets.length() ; i++) {
                        popularSets.add(new Set(sets.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("recentSets")) {
                    jsonMappings.put(modelName, model.toString());
                    JSONArray sets = payload.getJSONArray("recent");
                    for(int i = 0 ; i < sets.length() ; i++) {
                        recentSets.add(new Set(sets.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("allArtists")) {
                    jsonMappings.put(modelName, model.toString());
                    JSONArray allArtistsArray = payload.getJSONArray("artist");
                    for(int i = 0 ; i < allArtistsArray.length() ; i++) {
                        allArtists.add(new Artist(allArtistsArray.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("activities")) {
                    jsonMappings.put(modelName, model.toString());
                    JSONArray activitiesArray = payload.getJSONArray("activity");
                    for(int i = 0 ; i < activitiesArray.length() ; i++) {
                        activities.add(new Activity(activitiesArray.getJSONObject(i)));
                    }
                }
                onModelsChange();
            } else {
                Log.d(TAG, "Failed: " + modelName);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onModelsChange() {
        readyModels++;
        Log.d(TAG, Integer.toString(readyModels));

        if(readyModels == Constants.initialRequiredModels) {
            initialModelsReady = true;
        }
    }

    public List<Set> getAllSets() {
        return allSets;
    }

    public List<Artist> getArtists() { return artists; }

    public List<Artist> getAllArtists() {
        return allArtists;
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<Mix> getMixes() {
        return mixes;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public Lineup getLineups(String key) {
        return lineups.get(key);
    }

    public List<Set> getSearchedSets() {
        return searchedSets;
    }

    public List<Set> getPopularSets() {
        return popularSets;
    }

    public List<Set> getRecentSets() {
        return recentSets;
    }

    public List<Set> getDetailSets(String key) {
        return detailSets.get(key);
    }

    public List<Event> getDetailEvents(String key) {
        return detailEvents.get(key);
    }

    public List<Activity> getActivities() {
        try{
            return activities;

        }finally{
            activities = null;
        }

    }

    public void setLineups(JSONObject response) {
        JSONObject payload;
        try {
            if(response.getString("status").equals("success")) {
                payload = response.getJSONObject("payload");
                JSONObject lineup = payload.getJSONObject("lineup");
                String event = lineup.getString("event");
                lineups.put(event, new Lineup(lineup));
                jsonMappings.put("detailLineups" + event, response.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setDetailSets(JSONObject response) {
        List<Set> setList = new ArrayList<Set>();
        JSONObject payload;
        JSONObject payloadCategory;
        try {
            if(response.getString("status").equals("success")) {
                payload = response.getJSONObject("payload");
                if(payload.has("artist")) {
                    payloadCategory = payload.getJSONObject("artist");
                } else {
                    payloadCategory = payload.getJSONObject("festival");
                }
                JSONArray sets = payloadCategory.getJSONArray("sets");
                for(int i = 0 ; i < sets.length() ; i++) {
                    setList.add(new Set(sets.getJSONObject(i)));
                }
                detailSets.put(payloadCategory.getString("name"), setList);
                jsonMappings.put("detailSets" + payloadCategory.getString("name"), response.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void setDetailEvents(JSONObject response) {
        List<Event> eventList = new ArrayList<Event>();
        JSONObject payload;
        try {
            if(response.getString("status").equals("success")) {
                payload = response.getJSONObject("payload");
                JSONObject payloadArtist = payload.getJSONObject("artist");
                JSONArray uEvents = payloadArtist.getJSONArray("upcomingEvents");
                for(int i = 0 ; i < uEvents.length() ; i++) {
                    eventList.add(new Event(uEvents.getJSONObject(i)));
                }
                detailEvents.put(payloadArtist.getString("name"), eventList);
                jsonMappings.put("detailEvents" + payloadArtist.getString("name"), response.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Event> getSoonestEvents() {
        return soonestEvents;
    }

    public void setSoonestEvents(List<Event> soonestEvents) {
        this.soonestEvents = soonestEvents;
    }

    public List<Event> getClosestEvents() {
        return closestEvents;
    }

    public void setClosestEvents(List<Event> closestEvents) {
        this.closestEvents = closestEvents;
    }

    public List<Event> getSoonestEventsAroundMe() {
        return soonestEventsAroundMe;
    }

    public void setSoonestEventsAroundMe(List<Event> soonestEventsAroundMe) {
        this.soonestEventsAroundMe = soonestEventsAroundMe;
    }

    public List<Event> getUpcomingEvents() {
        return upcomingEvents;
    }

    public void setUpcomingEvents(List<Event> upcomingEvents) {
        this.upcomingEvents = upcomingEvents;
    }

    public List<Event> getRecentEvents() {
        return recentEvents;
    }

    public void setRecentEvents(List<Event> recentEvents) {
        this.recentEvents = recentEvents;
    }

    public List<Event> getSearchEvents() {
        return searchEvents;
    }

    public void setSearchEvents(List<Event> searchEvents) {
        this.searchEvents = searchEvents;
    }
}
