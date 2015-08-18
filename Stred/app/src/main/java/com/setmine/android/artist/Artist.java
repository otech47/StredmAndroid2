package com.setmine.android.artist;


import com.setmine.android.Constants;
import com.setmine.android.api.JSONModel;
import com.setmine.android.event.Event;
import com.setmine.android.set.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Artist  extends JSONModel {
	private String mId;
	private String mArtist;
    private String mBio;
    private String mFacebookLink;
    private String mTwitterLink;
    private String mWebLink;
    private String mInstagramLink;
    private String mSoundcloudLink;
    private String mYoutubeLink;
    private String mImageUrl;
    private List<Set> mSets;
    private List<Event> mUpcomingEvents;


    public Artist() {
    }

    public Artist(JSONObject json) {
        jsonModelString = json.toString();
        try {
            mId = json.getString("id");
            mArtist = json.getString("artist");
            mBio = json.getString("bio");
            mImageUrl = Constants.CLOUDFRONT_URL_FOR_IMAGES + json.getString("imageURL");

            if(!json.isNull("fb_link")) {
                mFacebookLink = json.getString("fb_link");
            }
            if(!json.isNull("twitter_link")) {
                mTwitterLink = json.getString("twitter_link");
            }
            if(!json.isNull("instagram_link")) {
                mInstagramLink = json.getString("instagram_link");
            }
            if(!json.isNull("soundcloud_link")) {
                mSoundcloudLink = json.getString("soundcloud_link");
            }
            if(!json.isNull("youtube_link")) {
                mYoutubeLink = json.getString("youtube_link");
            }
            if(!json.isNull("web_link")) {
                mWebLink = json.getString("web_link");
            }
            if(json.has("sets")) {
                setSets(json.getJSONArray("sets"));
            }
            if(json.has("upcomingEvents")) {
                setUpcomingEvents(json.getJSONArray("upcomingEvents"));
            }

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

    public String getInstagramLink() {
        return mInstagramLink;
    }

    public void setInstagramLink(String mInstagramLink) {
        this.mInstagramLink = mInstagramLink;
    }

    public String getSoundcloudLink() {
        return mSoundcloudLink;
    }

    public void setSoundcloudLink(String mSoundcloudLink) {
        this.mSoundcloudLink = mSoundcloudLink;
    }

    public String getYoutubeLink() {
        return mYoutubeLink;
    }

    public void setYoutubeLink(String mYoutubeLink) {
        this.mYoutubeLink = mYoutubeLink;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.mImageUrl = Constants.CLOUDFRONT_URL_FOR_IMAGES + imageUrl;
    }

    public List<Set> getSets() {
        return mSets;
    }

    public void setSets(List<Set> mSets) {
        this.mSets = mSets;
    }

    public void setSets(JSONArray setsJson) {
        this.mSets = new ArrayList<Set>();
        try {
            for(int i = 0; i < setsJson.length(); i++) {
                Set set = new Set(setsJson.getJSONObject(i));
                mSets.add(set);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }

    }

    public List<Event> getUpcomingEvents() {
        return mUpcomingEvents;
    }

    public void setUpcomingEvents(List<Event> mUpcomingEvents) {
        this.mUpcomingEvents = mUpcomingEvents;
    }

    public void setUpcomingEvents(JSONArray eventsJson) {
        this.mUpcomingEvents = new ArrayList<Event>();
        try {
            for(int i = 0; i < eventsJson.length(); i++) {
                Event event = new Event(eventsJson.getJSONObject(i));
                mUpcomingEvents.add(event);
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }
}