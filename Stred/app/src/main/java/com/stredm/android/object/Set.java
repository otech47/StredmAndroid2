package com.stredm.android.object;

import java.util.ArrayList;
import java.util.List;

import com.stredm.android.util.TimeUtils;

public class Set {
	private String mId;
	private String mImage;
	private String mArtist;
	private String mEvent;
	private String mGenre;
	private String mSongURL;
	private List<Track> mTracklist;
	private boolean mRadiomix;
	private boolean mDownloaded;

	public Set() {
		// default testing values
		this("-1", "artist", "event", "genre", "http://stredm.com/favicon.ico",
				"", new ArrayList<Track>(), false, false);
	}

	public Set(Set s) {
		setId(s.getId());
		setArtist(s.getArtist());
		setEvent(s.getEvent());
		setGenre(s.getGenre());
		setImage(s.getImage());
		setSongURL(s.getSongURL());
		setIsRadiomix(s.isRadiomix());
		setIsDownloaded(s.isDownloaded());
		setTracklist(s.getTracklist());
	}

	public Set(String id, String artist, String event, String genre,
			String imageURL, String songURL, List<Track> tracklist,
			boolean isRadiomix, boolean isDownloaded) {
		setId(id);
		setArtist(artist);
		setEvent(event);
		setGenre(genre);
		setImage(imageURL);
		setSongURL(songURL);
		setIsRadiomix(isRadiomix);
		setIsDownloaded(isDownloaded);
		setTracklist(tracklist);
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

	public String getEvent() {
		return mEvent;
	}

	public void setEvent(String event) {
		this.mEvent = event;
	}

	public String getGenre() {
		return mGenre;
	}

	public void setGenre(String genre) {
		this.mGenre = genre;
	}

	public String getImage() {
		return mImage;
	}

	public void setImage(String image) {
		this.mImage = image;
	}

	public String getSongURL() {
		return mSongURL;
	}

	public void setSongURL(String mSongURL) {
		this.mSongURL = mSongURL;
	}

	public List<Track> getTracklist() {
		return mTracklist;
	}

	public void setTracklist(List<Track> tracklist) {
		this.mTracklist = tracklist;
	}

	public boolean isRadiomix() {
		return mRadiomix;
	}

	public void setIsRadiomix(boolean mRadiomix) {
		this.mRadiomix = mRadiomix;
	}

	public boolean isDownloaded() {
		return mDownloaded;
	}

	public void setIsDownloaded(boolean mDownloaded) {
		this.mDownloaded = mDownloaded;
	}

	public String getCurrentTrack(long time) {
		String currentTrack = "";
		for (int i = 0; i < mTracklist.size(); i++) {
			Track t = mTracklist.get(i);
			TimeUtils utils = new TimeUtils();
			if (utils.timerToMilliSeconds(t.getStartTime()) <= time) {
				currentTrack = t.getTrackName();
			} else {
				break;
			}
		}
		return currentTrack;
	}
}