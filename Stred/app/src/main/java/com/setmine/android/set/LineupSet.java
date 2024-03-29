package com.setmine.android.set;

import com.setmine.android.Constants;
import com.setmine.android.api.JSONModel;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/29/14.
 */
public class LineupSet extends JSONModel {
    public String id;
    public String Artist;
    public String time;
    public int day;
    public String artistImage;
    public boolean hasSets;

    public LineupSet(JSONObject json) {
        jsonModelString = json.toString();
        try {
            setArtist(json.getString("artist"));
            setDay(json.getInt("day"));
            setTime(json.getString("time"));
            setArtistImage(json.getString("imageURL"));
            if(json.has("hasSets")) {
                setHasSets(json.getBoolean("hasSets"));
            } else {
                setHasSets(false);
            }
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
        this.artistImage = Constants.CLOUDFRONT_URL_FOR_IMAGES + artistImage;
    }

    public boolean isHasSets() {
        return hasSets;
    }

    public void setHasSets(boolean hasSets) {
        this.hasSets = hasSets;
    }
}
