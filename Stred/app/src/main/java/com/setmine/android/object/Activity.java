package com.setmine.android.object;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarlafarga on 1/5/15.
 */
public class Activity {
    private static final String imageServerUrl = "http://setmine.com/images/";

    private Integer id;
    private String activityName;
    private String imageURL;
    private List<Integer> setIDs;
    private List<Set> sets;

    public Activity() {
    }

    public Activity(JSONObject jsonObject) {
        try {
            this.id = jsonObject.getInt("id");
            this.activityName = jsonObject.getString("activity");
            this.imageURL = imageServerUrl + jsonObject.getString("imageURL");
            this.setIDs = new ArrayList<Integer>();
            this.sets = new ArrayList<Set>();
            JSONArray setIDsArray = jsonObject.getJSONArray("set_ids");
            JSONArray setsArray = jsonObject.getJSONArray("sets");
            for(int i = 0; i < setIDsArray.length() ; i++) {
                setIDs.add(setIDsArray.getInt(i));
            }
            for(int j = 0; j < setsArray.length() ; j++) {
                sets.add(new Set(setsArray.getJSONObject(j)));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public List<Integer> getSetIDs() {
        return setIDs;
    }

    public void setSetIDs(List<Integer> setIDs) {
        this.setIDs = setIDs;
    }

    public List<Set> getSets() {
        return sets;
    }

    public void setSets(List<Set> sets) {
        this.sets = sets;
    }
}
