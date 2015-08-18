package com.setmine.android.track;

import com.setmine.android.api.JSONModel;

import org.json.JSONObject;

public class Track extends JSONModel {
	private String trackName;
	private String artistName;
	private String songName;

	private String startTime;
	private String setLength;

	public Track() {
		songName = "Unknown";
		artistName = "Unknown Artist";
		trackName = "Unknown - Unknown Artist";
		startTime = "00:00";
	}

	public Track(JSONObject json) {
		try {
			trackName = json.getString("trackname");
			artistName = json.getString("artistname");
			songName = json.getString("songname");
			startTime = json.getString("starttime");
			setLength = json.getString("set_length");

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public String getTrackName() {
		return trackName;
	}

	public void setTrackName(String mTrackName) {
		this.trackName = mTrackName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String mStartTime) {
		this.startTime = mStartTime;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public String getSongName() {
		return songName;
	}

	public void setSongName(String songName) {
		this.songName = songName;
	}

	public String getSetLength() {
		return setLength;
	}

	public void setSetLength(String setLength) {
		this.setLength = setLength;
	}
}