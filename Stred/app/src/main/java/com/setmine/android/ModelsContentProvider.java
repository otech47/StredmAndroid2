package com.setmine.android;

import com.setmine.android.object.Artist;
import com.setmine.android.object.Event;
import com.setmine.android.object.Genre;
import com.setmine.android.object.Lineup;
import com.setmine.android.object.Mix;
import com.setmine.android.object.Set;
import com.setmine.android.object.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ModelsContentProvider {

    private List<Artist> artists;
    private List<Artist> allArtists;
    private List<Event> events;
    private List<Mix> mixes;
    private List<Genre> genres;
    public List<Set> searchedSets;
    public List<Set> allSets;
    private List<Set> popularSets;
    private List<Set> recentSets;
    private List<Track> tracks = null;
    public HashMap<String, Lineup> lineups;
    public List<Event> upcomingEvents;
    public List<Event> recentEvents;
    public List<Event> searchEvents;
    public HashMap<String, List<Set>> detailSets;
    public HashMap<String, List<Event>> detailEvents;
    public boolean initialModelsReady = false;

    public ModelsContentProvider() {
        upcomingEvents = new ArrayList<Event>();
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
                else if(modelName.equals("upcomingEventsAroundMe")) {
                    JSONObject upcoming = payload.getJSONObject("upcoming");
                    JSONArray around = upcoming.getJSONArray("soonestEventsAroundMe");
                    for(int i = 0 ; i < around.length() ; i++) {
                        upcomingEvents.add(new Event(around.getJSONObject(i)));
                    }
                    if(around.length() == 0) {
                        JSONArray soonest = upcoming.getJSONArray("soonestEvents");
                        for(int i = 0 ; i < soonest.length() ; i++) {
                            upcomingEvents.add(new Event(soonest.getJSONObject(i)));
                        }
                    }
                }
                else if(modelName.equals("recentEvents")) {
                    JSONArray recent = payload.getJSONArray("featured");
                    for(int i = 0 ; i < recent.length() ; i++) {
                        recentEvents.add(new Event(recent.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("searchEvents")) {
                    JSONObject upcoming = payload.getJSONObject("upcoming");
                    JSONArray closest = upcoming.getJSONArray("soonestEventsAroundMe");
                    for(int i = 0 ; i < closest.length() ; i++) {
                        searchEvents.add(new Event(closest.getJSONObject(i)));
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
                    JSONArray artistsArray = payload.getJSONArray("artist");
                    for(int i = 0 ; i < artistsArray.length() ; i++) {
                        artists.add(new Artist(artistsArray.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("festivals")) {
                    JSONArray allFestivals = payload.getJSONArray("festival");
                    for(int i = 0 ; i < allFestivals.length() ; i++) {
                        events.add(new Event(allFestivals.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("mixes")) {
                    JSONArray allMixes = payload.getJSONArray("mix");
                    for(int i = 0 ; i < allMixes.length() ; i++) {
                        Mix newMix = new Mix();
                        newMix.setMix(allMixes.getString(i));
                        mixes.add(newMix);
                    }

                }
                else if(modelName.equals("genres")) {
                    JSONArray allGenres = payload.getJSONArray("genre");
                    for(int i = 0 ; i < allGenres.length() ; i++) {
                        Genre newGenre = new Genre();
                        newGenre.setGenre(allGenres.getString(i));
                        genres.add(newGenre);
                    }
                }
                else if(modelName.equals("searchedSets")) {
                    JSONArray sets = payload.getJSONArray("search");
                    searchedSets.clear();
                    for(int i = 0 ; i < sets.length() ; i++) {
                        searchedSets.add(new Set(sets.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("popularSets")) {
                    JSONArray sets = payload.getJSONArray("popular");
                    for(int i = 0 ; i < sets.length() ; i++) {
                        popularSets.add(new Set(sets.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("recentSets")) {
                    JSONArray sets = payload.getJSONArray("recent");
                    for(int i = 0 ; i < sets.length() ; i++) {
                        recentSets.add(new Set(sets.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("allArtists")) {
                    JSONArray allArtistsArray = payload.getJSONArray("artist");
                    for(int i = 0 ; i < allArtistsArray.length() ; i++) {
                        allArtists.add(new Artist(allArtistsArray.getJSONObject(i)));
                    }
                }
            }
            else {
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        onModelsChange();
    }

    public void onModelsChange() {
        if(upcomingEvents.size() > 0 &&
                recentEvents.size() > 0 &&
                searchEvents.size() > 0 &&
                artists.size() > 0 &&
                events.size() > 0 &&
                mixes.size() > 0 &&
                genres.size() > 0) {
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

    public void setLineups(JSONObject response) {
        JSONObject payload;
        try {
            if(response.getString("status").equals("success")) {
                payload = response.getJSONObject("payload");
                String event = payload.getString("event");
                lineups.put(event, new Lineup(payload));
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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
