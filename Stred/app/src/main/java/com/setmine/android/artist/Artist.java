package com.setmine.android.artist;


import com.setmine.android.api.JSONModel;

import com.setmine.android.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class Artist  extends JSONModel {
	private String mId;
	private String mArtist;
    private String mBio;
    private String mFacebookLink;
    private String mTwitterLink;
    private String mWebLink;
    private String mImageUrl;

    public Artist() {
    }

    public Artist(JSONObject json) {
        jsonModelString = json.toString();
        try {
            setId(json.getString("id"));
            setArtist(json.getString("artist"));
            setBio(json.getString("bio"));
            setFacebookLink(json.getString("fb_link"));
            setTwitterLink(json.getString("twitter_link"));
            if(json.has("web_link")) {
                setWebLink(json.getString("web_link"));
            }
            setImageUrl(json.getString("imageURL"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getId() {
		return mId;
	}

	public void setId(String id) {
		this.mId = id;
	}

	public String getArtist() {
		return mArtist;
	}
	
	public void setArtist(String artist) {
		this.mArtist = artist;
	}

    public String getBio() {
        return mBio;
    }

    public void setBio(String bio) {
        this.mBio = bio;
    }

    public String getFacebookLink() {
        return mFacebookLink;
    }

    public void setFacebookLink(String facebookLink) {
        this.mFacebookLink = facebookLink;
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

    public void setWebLink(String mWebLink) {
        this.mWebLink = mWebLink;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.mImageUrl = Constants.CLOUDFRONT_URL_FOR_IMAGES + imageUrl;
    }

}