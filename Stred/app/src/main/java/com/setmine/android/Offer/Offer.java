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
    private String mMessage;



    public Offer(){}

    public Offer(JSONObject json){
        jsonModelString=json.toString();

        try{
            setOfferId(json.getString("id"));
            setArtist(new Artist(json.getJSONObject("artist")));
            setSetId(json.getString("set_id"));
            setVenue(new Venue(json.getJSONObject("venue")));
            setDateReleased(json.getString("date_released"));
            setDateExpired(json.getString("date_expired"));
            setTotalRevenue(json.getString("total_revenue"));
            setTotalConvergences(json.getString("total_convergences"));
            setMessage(json.getString("message"));

        }catch (JSONException e){
            e.printStackTrace();
        }

    }



    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        mMessage = message;
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
