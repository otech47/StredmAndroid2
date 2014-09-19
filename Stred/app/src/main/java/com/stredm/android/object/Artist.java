package com.stredm.android.object;


public class Artist {
	private String mId;
	private String mArtist;
    private String mBio;
    private String mFacebookLink;
    private String mTwitterLink;
    private String mImageUrl;

    public Artist() {
    }

    public Artist(String mId, String mArtist, String mBio, String mFacebookLink, String mTwitterLink, String mImageUrl) {
        this.mId = mId;
        this.mArtist = mArtist;
        this.mBio = mBio;
        this.mFacebookLink = mFacebookLink;
        this.mTwitterLink = mTwitterLink;
        this.mImageUrl = mImageUrl;
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

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.mImageUrl = imageUrl;
    }

}