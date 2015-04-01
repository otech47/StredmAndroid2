package com.setmine.android.api;

import android.util.Log;

import com.setmine.android.api.JSONModel;
import com.setmine.android.Constants;
import com.setmine.android.set.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarlafarga on 1/5/15.
 */
public class Activity extends JSONModel {

    private Integer id;
    private String activityName;
    private String imageURL;
    private List<Integer> setIDs;
    private List<Set> sets;

    public Activity() {
    }

    public Activity(JSONObject json) {
        jsonModelString = json.toString();
        try {
            this.id = json.getInt("id");
            this.activityName = json.getString("activity");
            this.imageURL = Constants.S3_ROOT_URL + json.getString("imageURL");
            this.setIDs = new ArrayList<Integer>();
            this.sets = new ArrayList<Set>();
            JSONArray setIDsArray = json.getJSONArray("set_ids");
            JSONArray setsArray = json.getJSONArray("sets");
            for(int i = 0; i < setIDsArray.length() ; i++) {
                setIDs.add(setIDsArray.getInt(i));
            }
            for(int j = 0; j < setsArray.length() ; j++) {
                Log.d("Activity: ", Integer.toString(j));
                Log.d("Activity: ", setsArray.getJSONObject(j).toString());
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
