package com.stredm.android.object;


import org.json.JSONException;
import org.json.JSONObject;

public class Artist {
	private String mId;
	private String mArtist;
    private String mBio;
    private String mFacebookLink;
    private String mTwitterLink;
    private String mWebLink;
    private String mImageUrl;

    public Artist() {
    }

    public Artist(String mId, String mArtist, String mBio, String mFacebookLink, String mTwitterLink, String mImageUrl) {
        this.mId = mId;
        this.mArtist = mArtist;
        this.mBio = mBio;
        this.mFacebookLink = mFacebookLink;
        this.mTwitterLink = mTwitterLink;
        this.mWebLink = mWebLink;
        this.mImageUrl = mImageUrl;
    }

    public Artist(JSONObject json) {
        try {
            setId(json.getString("id"));
            setArtist(json.getString("artist"));
            setBio(json.getString("bio"));
            setFacebookLink(json.getString("fb_link"));
            setTwitterLink(json.getString("twitter_link"));
            setWebLink(json.getString("web_link"));
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
        this.mImageUrl = imageUrl;
    }

}