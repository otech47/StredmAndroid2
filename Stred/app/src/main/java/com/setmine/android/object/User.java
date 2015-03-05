package com.setmine.android.object;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarlafarga on 1/5/15.
 */
public class User extends JSONModel {

    private String email;
    private String firstName;
    private String lastName;
    private String facebookID;
    private List<Set> favoriteSets;

    public User() {}

    public User(JSONObject json) {
        jsonModelString = json.toString();
        try {
            setEmail(json.getString("username"));
            setFirstName(json.getString("first_name"));
            setLastName(json.getString("last_name"));
            setFacebookID(json.getString("facebook_id"));
            setFavoriteSets(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        for(int i = 0 ; i < favoriteSets.size() ; i++) {
            if(s.getId().equals(favoriteSets.get(i).getId())) {
                return true;
            }
        }
        return false;
    }

}
