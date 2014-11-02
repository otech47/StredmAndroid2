package com.setmine.android;

import android.util.Log;

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
import java.util.List;

public class ModelsContentProvider {

    private List<Artist> artists;
    private List<Event> events;
    private List<Mix> mixes;
    private List<Genre> genres;
    public List<Set> searchedSets;
    public List<Set> allSets;
    private List<Set> popularSets;
    private List<Set> recentSets;
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
        artists = new ArrayList<Artist>();
        events = new ArrayList<Event>();
        mixes = new ArrayList<Mix>();
        genres = new ArrayList<Genre>();
        searchedSets = new ArrayList<Set>();
        popularSets = new ArrayList<Set>();
        recentSets = new ArrayList<Set>();
    }

    public void setModel(JSONObject model, String modelName) {
        JSONObject payload;
        Log.v("Storing model: ", modelName);
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
                else if(modelName.equals("searchEvents")) {
                    JSONObject upcoming = payload.getJSONObject("upcoming");
                    JSONArray closest = upcoming.getJSONArray("soonestEventsAroundMe");
                    for(int i = 0 ; i < closest.length() ; i++) {
                        searchEvents.add(new Event(closest.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("lineups")) {
                    lineups.add(new Lineup(payload));
                }
                else if(modelName.equals("sets")) {
                    JSONObject festival = payload.getJSONObject("festival");
                    JSONArray sets = festival.getJSONArray("sets");
                    for(int i = 0 ; i < sets.length() ; i++) {
                        allSets.add(new Set(sets.getJSONObject(i)));
                    }
                }
                else if(modelName.equals("artists")) {
                    JSONArray allArtists = payload.getJSONArray("artist");
                    for(int i = 0 ; i < allArtists.length() ; i++) {
                        artists.add(new Artist(allArtists.getJSONObject(i)));
                    }
                    Log.v("number", ((Integer) artists.size()).toString());
                }
                else if(modelName.equals("festivals")) {
                    JSONArray allFestivals = payload.getJSONArray("festival");
                    for(int i = 0 ; i < allFestivals.length() ; i++) {
                        events.add(new Event(allFestivals.getJSONObject(i)));
                    }
                    Log.v("number", ((Integer) events.size()).toString());
                }
                else if(modelName.equals("mixes")) {
                    JSONArray allMixes = payload.getJSONArray("mix");
                    for(int i = 0 ; i < allMixes.length() ; i++) {
                        Mix newMix = new Mix();
                        newMix.setMix(allMixes.getString(i));
                        mixes.add(newMix);
                    }
                    Log.v("number", ((Integer) mixes.size()).toString());

                }
                else if(modelName.equals("genres")) {
                    JSONArray allGenres = payload.getJSONArray("genre");
                    for(int i = 0 ; i < allGenres.length() ; i++) {
                        Genre newGenre = new Genre();
                        newGenre.setGenre(allGenres.getString(i));
                        genres.add(newGenre);
                    }
                    Log.v("number", ((Integer) genres.size()).toString());
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

    public Lineup getLastLineup() {
        return lineups.get(lineups.size()-1);
    }

    public List<Artist> getArtists() { return artists; }

    public List<Event> getEvents() {
        return events;
    }

    public List<Mix> getMixes() {
        return mixes;
    }

    public List<Genre> getGenres() {
        return genres;
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
}
