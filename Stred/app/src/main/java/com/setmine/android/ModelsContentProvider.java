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

    private List<Track> tracks = null;
    public HashMap<String, Lineup> lineups;
    public List<Event> closestEvents;
    public List<Event> upcomingEvents;

    public HashMap<String, List<Set>> detailSets;
    public HashMap<String, List<Event>> detailEvents;
    public boolean initialModelsReady = false;

    public HashMap<String, String> jsonMappings;

    public ModelsContentProvider() {
        closestEvents = new ArrayList<Event>();
        upcomingEvents = new ArrayList<Event>();
        lineups = new HashMap<String, Lineup>();

        detailSets = new HashMap<String, List<Set>>();
        detailEvents = new HashMap<String, List<Event>>();
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

    public static List createModel(JSONObject model, String modelName) {
        JSONObject payload;
        try {

            if(model.getString("status").equals("success")) {
                payload = model.getJSONObject("payload");
                if(modelName.equals("upcomingEvents")) {
                    JSONObject upcoming = payload.getJSONObject("upcoming");
                    JSONArray around = upcoming.getJSONArray("soonestEventsAroundMe");

                    List<Event> soonestEventsAroundMe = new ArrayList<Event>();
                    for(int i = 0 ; i < around.length() ; i++) {
                        soonestEventsAroundMe.add(new Event(around.getJSONObject(i)));
                    }
                    return soonestEventsAroundMe;
                }
                else if(modelName.equals("recentEvents")) {
                    List<Event> recentEvents = new ArrayList<Event>();
                    JSONArray recent = payload.getJSONArray("featured");
                    for(int i = 0 ; i < recent.length() ; i++) {
                        recentEvents.add(new Event(recent.getJSONObject(i)));
                    }
                    return recentEvents;
                }
                else if(modelName.equals("searchEvents")) {
                    JSONObject upcoming = payload.getJSONObject("upcoming");
                    JSONArray around = upcoming.getJSONArray("closestEvents");
                    List<Event> searchEvents = new ArrayList<Event>();
                    for(int i = 0 ; i < around.length() ; i++) {
                        searchEvents.add(new Event(around.getJSONObject(i)));
                    }
                    return searchEvents;
                }
                else if(modelName.equals("sets")) {
                    JSONObject festival = payload.getJSONObject("festival");
                    JSONArray sets = festival.getJSONArray("sets");
                    List<Set> allSets = new ArrayList<Set>();
                    for(int i = 0 ; i < sets.length() ; i++) {
                        allSets.add(new Set(sets.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("artists")) {
                    JSONArray artistsArray = payload.getJSONArray("artist");
                    List<Artist> artists= new ArrayList<Artist>();
                    for(int i = 0 ; i < artistsArray.length() ; i++) {
                        artists.add(new Artist(artistsArray.getJSONObject(i)));
                    }
                    return artists;
                }
                else if(modelName.equals("festivals")) {
                    JSONArray allFestivals = payload.getJSONArray("festival");
                    List<Event> events = new ArrayList<Event>();
                    for(int i = 0 ; i < allFestivals.length() ; i++) {
                        events.add(new Event(allFestivals.getJSONObject(i)));
                    }
                    return events;
                }
                else if(modelName.equals("mixes")) {
                    JSONArray allMixes = payload.getJSONArray("mix");
                    List<Mix> mixes= new ArrayList<Mix>();
                    for(int i = 0 ; i < allMixes.length() ; i++) {
                        mixes.add(new Mix(allMixes.getJSONObject(i)));
                    }
                    return mixes;
                }
                else if(modelName.equals("genres")) {
                    JSONArray allGenres = payload.getJSONArray("genre");
                    List<Genre> genres = new ArrayList<Genre>();
                    for(int i = 0 ; i < allGenres.length() ; i++) {
                        Genre newGenre = new Genre();
                        newGenre.setGenre(allGenres.getString(i));
                        genres.add(newGenre);
                    }
                    return genres;
                }
                else if(modelName.equals("searchedSets")) {
                    JSONArray sets = payload.getJSONObject("search").getJSONArray("sets");
                    List<Set> searchedSets = new ArrayList<Set>();
                    for (int i = 0; i < sets.length(); i++) {
                        searchedSets.add(new Set(sets.getJSONObject(i)));
                    }
                    return searchedSets;
                }
                else if(modelName.equals("searchedEvents")) {
                    JSONArray upcomingEvents = payload.getJSONObject("search").getJSONArray("upcomingEvents");
                    List<Event> searchedUpcomingEvents = new ArrayList<Event>();
                    for(int i = 0 ; i < upcomingEvents.length() ; i++) {
                        searchedUpcomingEvents.add(new Event(upcomingEvents.getJSONObject(i)));
                    }
                    return searchedUpcomingEvents;
                }
                else if(modelName.equals("searchedTracks")) {
                    JSONArray tracks = payload.getJSONObject("search").getJSONArray("tracks");
                    List<TrackResponse> searchedTracks = new ArrayList<TrackResponse>();
                    for(int i = 0 ; i < tracks.length() ; i++) {
                        searchedTracks.add(new TrackResponse(tracks.getJSONObject(i)));
                    }
                    return searchedTracks;
                }
                else if(modelName.equals("popularSets")) {
                    JSONArray sets = payload.getJSONArray("popular");
                    List<Set> popularSets = new ArrayList<Set>();

                    for(int i = 0 ; i < sets.length() ; i++) {
                        popularSets.add(new Set(sets.getJSONObject(i)));
                    }
                    return popularSets;
                }
                else if(modelName.equals("recentSets")) {
                    JSONArray sets = payload.getJSONArray("recent");
                    List<Set> recentSets  = new ArrayList<Set>();
                    for(int i = 0 ; i < sets.length() ; i++) {
                        recentSets.add(new Set(sets.getJSONObject(i)));
                    }
                    return recentSets;
                }
                else if(modelName.equals("allArtists")) {
                    JSONArray allArtistsArray = payload.getJSONArray("artist");
                    List<Artist> allArtists  = new ArrayList<Artist>();
                    for(int i = 0 ; i < allArtistsArray.length() ; i++) {
                        allArtists.add(new Artist(allArtistsArray.getJSONObject(i)));
                    }
                    return allArtists;
                }
                else if(modelName.equals("activities")) {
                    JSONArray activitiesArray = payload.getJSONArray("activity");
                    List<Activity> activities  = new ArrayList<Activity>();
                    for(int i = 0 ; i < activitiesArray.length() ; i++) {
                        activities.add(new Activity(activitiesArray.getJSONObject(i)));
                    }
                    return activities;
                }
            } else {
                Log.d(TAG, "Failed: " + modelName);
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
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


}
