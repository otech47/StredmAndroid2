package com.setmine.android.Offer;

import com.setmine.android.api.JSONModel;
import com.setmine.android.artist.Artist;
import com.setmine.android.venue.Venue;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ryan on 6/21/2015.
 */


public class Offer extends JSONModel{

    private String mOfferId;
    private Artist mArtist;
    private String mSetId;
    private Venue mVenue;
    private String mDateReleased;
    private String mDateExpired;
    private String mTotalRevenue;
    private String mTotalConvergences;

    public Offer(){}

    public Offer(JSONObject json){
        jsonModelString=json.toString();
        JSONObject payload;
        try{
            if(json.getString("status").equals("success")) {
            payload = json.getJSONObject("payload");

            setOfferId(payload.getString("id"));
            setArtist(new Artist(payload.getJSONObject("artist_id")));
            setSetId(payload.getString("set_id"));
            setVenue(new Venue(payload.getJSONObject("venue")));
            setDateReleased(payload.getString("date_released"));
            setDateExpired(payload.getString("date_expired"));
            setTotalRevenue(payload.getString("total_revenue"));
            setTotalConvergences(payload.getString("total_convergences"));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    public String getOfferId() {
        return mOfferId;
    }

    public void setOfferId(String id) {
        this.mOfferId = id;
    }

    public Artist getArtist() {
        return mArtist;
    }

    public void setArtist(Artist artist) {
        this.mArtist = artist;
    }

    public String getSetId() {
        return mSetId;
    }

    public void setSetId(String setId) {
        this.mSetId = setId;
    }

    public Venue getVenue() {
        return mVenue;
    }

    public void setVenue(Venue venue) {
        this.mVenue = venue;
    }

    public String getDateReleased() {
        return mDateReleased;
    }

    public void setDateReleased(String dateReleased) {
        this.mDateReleased = dateReleased;
    }

    public String getDateExpired() {
        return mDateExpired;
    }

    public void setDateExpired(String dateExpired) {
        this.mDateExpired = dateExpired;
    }

    public String getTotalRevenue() {
        return mTotalRevenue;
    }

    public void setTotalRevenue(String totalRevenue) {
        this.mTotalRevenue = totalRevenue;
    }

    public String getTotalConvergences() {
        return mTotalConvergences;
    }

    public void setTotalConvergences(String totalConvergences) {
        this.mTotalConvergences = totalConvergences;
    }
}
