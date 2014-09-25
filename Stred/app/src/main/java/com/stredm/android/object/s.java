//package com.stredm.android.object;
//
///**
// * Created by oscarlafarga on 9/23/14.
// */
//
//import com.stredm.android.util.TimeUtils;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class Set {
//    private String id;
//    private String artistImageURL;
//    private String eventImageURL;
//    private String artist;
//    private String event;
//    private String genre;
//    private String songurl;
//    private String plays;
//    private List<Track> tracklist;
//    private List<String> startTimes;
//    private boolean radiomix;
//
//    public Set(JSONObject json) {
//        try {
//            setId(json.getString("id"));
//            setArtist(json.getString("artist"));
//            setEvent(json.getString("event"));
//            setGenre(json.getString("genre"));
//            setArtistImageURL(json.getString("artistimageURL"));
//            setEventImageURL(json.getString("eventimageURL"));
//            setPlays(json.getString("popularity"));
//            setSongurl(json.getString("songURL"));
//            setRadiomix(json.getString("is_radiomix"));
//            setTracklist(json.getString("tracklist"));
//            setStartTimes(json.getString("startTimes"));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public List<String> getStartTimes() {
//        return startTimes;
//    }
//
//    public void setStartTimes(List<String> startTimes) {
//        this.startTimes = startTimes;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getArtistImageURL() {
//        return artistImageURL;
//    }
//
//    public void setArtistImageURL(String artistImageURL) {
//        this.artistImageURL = artistImageURL;
//    }
//
//    public String getEventImageURL() {
//        return eventImageURL;
//    }
//
//    public void setEventImageURL(String eventImageURL) {
//        this.eventImageURL = eventImageURL;
//    }
//
//    public String getArtist() {
//        return artist;
//    }
//
//    public void setArtist(String artist) {
//        this.artist = artist;
//    }
//
//    public String getEvent() {
//        return event;
//    }
//
//    public void setEvent(String event) {
//        this.event = event;
//    }
//
//    public Integer getPlays() {
//        return plays;
//    }
//
//    public void setPlays(Integer plays) {
//        this.plays = plays;
//    }
//
//    public String getGenre() {
//        return genre;
//    }
//
//    public void setGenre(String genre) {
//        this.genre = genre;
//    }
//
//    public String getSongurl() {
//        return songurl;
//    }
//
//    public void setSongurl(String songurl) {
//        this.songurl = songurl;
//    }
//
//    public List<Track> getTracklist() {
//        return tracklist;
//    }
//
//    public void setTracklist(String tracklist) {
//        List<Track> mTracklist = new ArrayList<Track>();
//        JSONArray mTracklist =
//        for()
//            this.tracklist = tracklist;
//    }
//
//    public boolean isRadiomix() {
//        return radiomix;
//    }
//
//    public void setRadiomix(boolean radiomix) {
//        this.radiomix = radiomix;
//    }
//
//    public String getCurrentTrack(long time) {
//        String currentTrack = "";
//        for (int i = 0; i < tracklist.size(); i++) {
//            Track t = tracklist.get(i);
//            TimeUtils utils = new TimeUtils();
//            if (utils.timerToMilliSeconds(t.getStartTime()) <= time) {
//                currentTrack = t.getTrackName();
//            } else {
//                break;
//            }
//        }
//        return currentTrack;
//    }
//}
