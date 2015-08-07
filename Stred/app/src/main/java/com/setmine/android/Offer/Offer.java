package com.setmine.android.Offer;

import com.setmine.android.api.JSONModel;
import com.setmine.android.artist.Artist;
import com.setmine.android.set.Set;
import com.setmine.android.venue.Venue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryan on 6/21/2015.
 */


public class Offer extends JSONModel {

    private String mOfferId;
    private Artist mArtist;
    private String mSetId;
    private List<Venue> mVenues;
    private String mDateReleased;
    private String mDateExpired;
    private String mTotalRevenue;
    private String mTotalConvergedSuperfans;
    private String mMessage;
    private String mImageURL;
    private String mLink;
    private Set mSet;
    private String status;

    public Offer(){}

    public Offer(JSONObject json){
        jsonModelString = json.toString();

        try{
            setOfferId(json.getString("id"));
            setArtist(new Artist(json.getJSONObject("artist")));
            setSetId(json.getString("set_id"));
            setVenues(json.getJSONArray("venue"));
            setDateReleased(json.getString("date_released"));
            setDateExpired(json.getString("date_expired"));
            setTotalRevenue(json.getString("total_revenue"));
            setTotalConvergedSuperfans(json.getString("total_converged_superfans"));
            setMessage(json.getString("message"));
            setLink(json.getString("link"));
            setSet(new Set(json.getJSONObject("set")));

            if(json.has("image_url")) {
                setImageURL(json.getString("image_url"));
            } else {
                setImageURL(null);
            }
            if(json.has("link")) {
                setLink(json.getString("link"));
            } else {
                setLink(null);
            }
            if (json.has("unlocked_set")) {
                setStatus("unlocked");
            } else {
                setStatus("locked");
            }

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

    public List<Venue> getVenues() {
        return mVenues;
    }

    public void setVenues(List<Venue> venues) {
        this.mVenues = venues;
    }

    public void setVenues(JSONArray venues) {
        this.mVenues = new ArrayList<Venue>();
        for(int i = 0; i < venues.length(); i++) {
            try {
                this.mVenues.add(new Venue(venues.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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

    public String getTotalConvergedSuperfans() {
        return mTotalConvergedSuperfans;
    }

    public void setTotalConvergedSuperfans(String totalConvergences) {
        this.mTotalConvergedSuperfans = totalConvergences;
    }



    public String getImageURL() {
        return mImageURL;
    }

    public void setImageURL(String mImageURL) {
        this.mImageURL = mImageURL;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String mLink) {
        this.mLink = mLink;
    }

    public Set getSet() {
        return mSet;
    }

    public void setSet(Set set) {
        this.mSet = set;
    }

    public boolean checkUnlock(String userID) {
        return false;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
