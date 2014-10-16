package com.setmine.android.object;

import com.setmine.android.util.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Set {
    private static final String amazonS3Url = "http://stredm.s3-website-us-east-1.amazonaws.com/namecheap/";
    private String mId;
    private String mArtistImage;
    private String mEventImage;
    private String mArtist;
    private String mEvent;
    private String mGenre;
    private String mSongURL;
    private List<Track> mTracklist;
    private Integer popularity;
    private Integer mRadiomix;
    private boolean mDownloaded;

    public Set() {
        // default testing values
        this("-1", "artist", "event", "genre", "http://setmine.com/favicon.ico",
                "", new ArrayList<Track>(), 0, false);
    }

    public Set(JSONObject json) {
        try {
            setId(json.getString("id"));
            setArtistImage(json.getString("artistimageURL"));
            setEventImage(json.getString("eventimageURL"));
            setArtist(json.getString("artist"));
            setEvent(json.getString("event"));
            setGenre(json.getString("genre"));
            setSongURL(json.getString("songURL"));
            setPopularity(json.getInt("popularity"));
            setIsRadiomix(json.getInt("is_radiomix"));
            setTracklist(generateTracklist(json.getString("tracklist"), json.getString("starttimes")));
            setIsDownloaded(false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Set(Set s) {
        setId(s.getId());
        setArtist(s.getArtist());
        setEvent(s.getEvent());
        setGenre(s.getGenre());
        setArtistImage(s.getArtistImage());
        setEventImage(s.getEventImage());
        setSongURL(s.getSongURL());
        setIsRadiomix(s.isRadiomix());
        setIsDownloaded(s.isDownloaded());
        setTracklist(s.getTracklist());
    }

    public Set(String id, String artist, String event, String genre,
               String imageURL, String songURL, List<Track> tracklist,
               Integer isRadiomix, boolean isDownloaded) {
        setId(id);
        setArtist(artist);
        setEvent(event);
        setGenre(genre);
        setArtistImage(imageURL);
        setSongURL(songURL);
        setIsRadiomix(isRadiomix);
        setIsDownloaded(isDownloaded);
        setTracklist(tracklist);
    }

    public Integer getPopularity() {
        return popularity;
    }

    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
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

    public String getArtistImage() {
        return mArtistImage;
    }

    public void setArtistImage(String image) {
        this.mArtistImage = image;
    }

    public String getEventImage() {
        return mEventImage;
    }

    public void setEventImage(String mEventImage) {
        this.mEventImage = mEventImage;
    }

    public String getSongURL() {
        return mSongURL;
    }

    public void setSongURL(String mSongURL) {
        this.mSongURL = amazonS3Url + mSongURL;
    }

    public List<Track> getTracklist() {
        return mTracklist;
    }

    public void setTracklist(List<Track> tracklist) {
        this.mTracklist = tracklist;
    }

    public Integer isRadiomix() {
        return mRadiomix;
    }

    public void setIsRadiomix(Integer mRadiomix) {
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

    public List<Track> generateTracklist(String tracklistString, String starttimesString) {
        String[] tracklistArray = tracklistString.split(", ");
        String[] starttimesArray = starttimesString.split(", ");
        List<Track> trackList = new ArrayList<Track>();
        for(int i = 0 ; i < tracklistArray.length; i++) {
            trackList.add(new Track(tracklistString, starttimesString));
        }
        return trackList;
    }
}