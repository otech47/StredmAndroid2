package com.setmine.android.object;


import org.json.JSONException;
import org.json.JSONObject;

public class Mix {
	private String id;
	private String mix;
    private String bio;
    private String facebookLink;
    private String twitterLink;
    private String webLink;
    private String iconImageUrl;

	
	public Mix(String id, String mix) {
		setId(id);
		setMix(mix);
	}

    public Mix(JSONObject json) {
        try {
            setId(json.getString("id"));
            setMix(json.getString("mix"));
            setBio(json.getString("bio"));
            setFacebookLink(json.getString("fb_link"));
            setTwitterLink(json.getString("twitter_link"));
            setWebLink(json.getString("web_link"));
            setIconImageUrl(json.getString("imageURL"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
	
	public String getId() {
		return id;
	}

	public void setId(String mId) {
		this.id = mId;
	}

	public String getMix() {
		return mix;
	}
	
	public void setMix(String mix) {
		this.mix = mix;
	}

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFacebookLink() {
        return facebookLink;
    }

    public void setFacebookLink(String facebookLink) {
        this.facebookLink = facebookLink;
    }

    public String getTwitterLink() {
        return twitterLink;
    }

    public void setTwitterLink(String twitterLink) {
        this.twitterLink = twitterLink;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }

    public String getIconImageUrl() {
        return iconImageUrl;
    }

    public void setIconImageUrl(String iconImageUrl) {
        this.iconImageUrl = iconImageUrl;
    }
}