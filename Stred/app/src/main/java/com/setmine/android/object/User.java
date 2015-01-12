package com.setmine.android.object;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarlafarga on 1/5/15.
 */
public class User {

    private String email;
    private List<Set> favoriteSets;

    public User() {}

    public User(JSONObject json) {
        try {
            setEmail(json.getString("email"));
            JSONArray sets = json.getJSONArray("favorite_sets");
            favoriteSets = new ArrayList<Set>();
            for(int i = 0 ; i < sets.length() ; i++) {
                favoriteSets.add(new Set(sets.getJSONObject(i)));
            }
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

    public List<Set> getFavoriteSets() {
        return favoriteSets;
    }

    public void setFavoriteSets(List<Set> favoriteSets) {
        this.favoriteSets = favoriteSets;
    }
}
