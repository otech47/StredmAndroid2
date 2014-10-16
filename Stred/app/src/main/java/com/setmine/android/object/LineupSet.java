package com.setmine.android.object;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/29/14.
 */
public class LineupSet {
    public String Artist;
    public String time;
    public int day;
    public String artistImage;

    public LineupSet(JSONObject json) {
        try {
            setArtist(json.getString("artist"));
            setDay(json.getInt("day"));
            setTime(json.getString("time").substring(0, json.getString("time").length()-3));
            setArtistImage(json.getString("imageURL"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String artist) {
        Artist = artist;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public String getArtistImage() {
        return artistImage;
    }

    public void setArtistImage(String artistImage) {
        this.artistImage = artistImage;
    }
}
