package com.setmine.android.venue;

import com.setmine.android.Constants;
import com.setmine.android.api.JSONModel;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by ryan on 6/22/2015.
 */
public class Venue extends JSONModel{

    private String mId;
    private String mVenueName;



    private String mFbLink;
    private String mTwitterLink;
    private String mWebLink;
    private String mInstaLink;
    private String mIconImageId;
    private String mBannerImageId;
    private String mIconImageUrl;
    private String mBannerImageUrl;
    private String mLatitude;
    private String mLongitude;
    private String mAddress;
    private String mBeacon;


    public Venue(){}

    public Venue(JSONObject json){


        jsonModelString=json.toString();

        try{
            setId(json.getString("id"));
            setVenueName(json.getString("venue"));
            setFbLink(json.getString("fb_link"));
            setTwitterLink(json.getString("twitter_link"));
            setWebLink(json.getString("web_link"));
            setInstaLink(json.getString("ig_link"));
            setIconImageId(json.getString("icon_imageID"));
            setBannerImageId(json.getString("banner_imageID"));
            setIconImageUrl(json.getString("icon_imageURL"));
            setLongitude(json.getString("longitude"));
            setLatitude(json.getString("latitude"));
            setBeacon(json.getString("beacon"));

        }catch (JSONException e){
            e.printStackTrace();
        }






    }


    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getFbLink() {
        return mFbLink;
    }

    public void setFbLink(String fbLink) {
        this.mFbLink = fbLink;
    }

    public String getTwitterLink() {
        return mTwitterLink;
    }

    public void setTwitterLink(String twitterLink) {
        this.mTwitterLink = twitterLink;
    }

    public String getWebLink() {
        return mWebLink;
    }

    public void setWebLink(String webLink) {
        this.mWebLink = webLink;
    }

    public String getInstaLink() {
        return mInstaLink;
    }

    public void setInstaLink(String instaLink) {
        this.mInstaLink = instaLink;
    }

    public String getIconImageId() {
        return mIconImageId;
    }

    public void setIconImageId(String iconImageId) {
        this.mIconImageId = iconImageId;
    }

    public String getBannerImageId() {
        return mBannerImageId;
    }

    public void setBannerImageId(String bannerImageId) {
        this.mBannerImageId = bannerImageId;
    }

    public String getIconImageUrl() {
        return mIconImageUrl;
    }

    public void setIconImageUrl(String iconImageUrl) {
        this.mIconImageUrl = Constants.CLOUDFRONT_URL_FOR_IMAGES+ iconImageUrl;
    }

    public String getBannerImageUrl() {
        return mBannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.mBannerImageUrl = Constants.CLOUDFRONT_URL_FOR_IMAGES+bannerImageUrl;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String latitude) {
        mLatitude = latitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String longitude) {
        mLongitude = longitude;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public String getBeacon() {
        return mBeacon;
    }

    public void setBeacon(String beacon) {
        mBeacon = beacon;
    }

    public String getVenueName() {
        return mVenueName;
    }

    public void setVenueName(String venueName) {
        mVenueName = venueName;
    }

}
