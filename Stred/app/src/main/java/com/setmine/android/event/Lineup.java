package com.setmine.android.event;

import com.setmine.android.api.JSONModel;
import com.setmine.android.set.LineupSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarlafarga on 9/29/14.
 */
public class Lineup extends JSONModel {
    public String id;
    public String event;
    public List<LineupSet> lineup;

    public Lineup(JSONObject json) {
        jsonModelString = json.toString();
        try {
            setId(json.getString("id"));
            setEvent(json.getString("event"));
            setLineup(generateLineup(json.getJSONArray("lineup")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public List<LineupSet> generateLineup(JSONArray json) {
       List<LineupSet> lineupList = new ArrayList<LineupSet>();
        try {
            for(int i = 0 ; i < json.length(); i++) {
                lineupList.add(new LineupSet(json.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lineupList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public List<LineupSet> getLineup() {
        return lineup;
    }

    public void setLineup(List<LineupSet> lineup) {
        this.lineup = lineup;
    }
}
