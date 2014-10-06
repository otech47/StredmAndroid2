package com.stredm.android.object;

public class Track {
	private String trackName;
	private String startTime;

	public Track(String trackname, String starttime) {
		setTrackName(trackname);
		setStartTime(starttime);
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

}