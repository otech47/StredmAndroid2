package com.setmine.android.user;

import android.location.Location;

import com.setmine.android.Offer.Offer;
import com.setmine.android.api.JSONModel;
import com.setmine.android.event.Event;
import com.setmine.android.set.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarlafarga on 1/5/15.
 */
public class User extends JSONModel {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String facebookID;
    private List<Set> favoriteSets;
    private Event nextEvent;
    private List<Set> newSets;
    private boolean isRegistered;
    private List<Offer> mOffers;
    private Location location;


    public User() {
        jsonModelString = "";
        isRegistered = false;
    }

    public User(JSONObject json) {
        jsonModelString = json.toString();
        try {
            setId(json.getString("id"));
            setEmail(json.getString("username"));
            setFirstName(json.getString("first_name"));
            setLastName(json.getString("last_name"));
            setFacebookID(json.getString("facebook_id"));
            setFavoriteSets(json);
            isRegistered = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void isRegistered(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFacebookID() {
        return facebookID;
    }

    public void setFacebookID(String facebookID) {
        this.facebookID = facebookID;
    }

    public List<Set> getFavoriteSets() {
        return favoriteSets;
    }

    public void setFavoriteSets(List<Set> favoriteSets) {
        this.favoriteSets = favoriteSets;
    }

    public void setFavoriteSets(JSONObject json) {
        try {
            JSONArray sets = json.getJSONArray("favorite_sets");
            favoriteSets = new ArrayList<Set>();
            for(int i = 0 ; i < sets.length() ; i++) {
                favoriteSets.add(new Set(sets.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public boolean isSetFavorited(Set s) {
        if(favoriteSets == null) {
            return false;
        }
        for(int i = 0 ; i < favoriteSets.size() ; i++) {
            if(s.getId().equals(favoriteSets.get(i).getId())) {
                return true;
            }
        }
        return false;
    }

    public Event getNextEvent() {
        return nextEvent;
    }

    public void setNextEvent(Event nextEvent) {
        this.nextEvent = nextEvent;
    }

    public void setNextEvent(JSONObject json) {
        nextEvent = new Event(json);
    }

    public List<Set> getNewSets() {
        return newSets;
    }

    public void setNewSets(JSONArray json) {
        try {
            newSets = new ArrayList<Set>();
            for(int i = 0 ; i < json.length() ; i++) {
                newSets.add(new Set(json.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Offer> getNewOffers() {
        return mOffers;
    }

    public void setNewOffers(JSONArray json) {
        try {
            mOffers = new ArrayList<Offer>();
            for(int i = 0 ; i < json.length() ; i++) {
                mOffers.add(new Offer(json.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
