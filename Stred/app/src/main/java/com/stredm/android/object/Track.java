package com.stredm.flume.object;

public class Track {
	private String mTrackName;
	private String mStartTime;

	public Track() {
		// default testing values
		this("Unknown", "00:00");
	}

	public Track(String trackname, String starttime) {
		setTrackName(trackname);
		setStartTime(starttime);
	}

	public String getTrackName() {
		return mTrackName;
	}

	public void setTrackName(String mTrackName) {
		this.mTrackName = mTrackName;
	}

	public String getStartTime() {
		return mStartTime;
	}

	public void setStartTime(String mStartTime) {
		this.mStartTime = mStartTime;
	}

}