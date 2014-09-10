package com.stredm.android.object;


public class Artist {
	private String mId;
	private String mArtist;

	public Artist() {
		// default testing values
		this("-1",
			"artist");
	}
	
	public Artist(String id, String artist) {
		setId(id);
		setArtist(artist);
	}
	
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getArtist() {
		return mArtist;
	}
	
	public void setArtist(String artist) {
		this.mArtist = artist;
	}

}