package com.setmine.android.object;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.setmine.android.Constants;
import com.setmine.android.util.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Set extends JSONModel implements Parcelable {
    private String mId;
    private String mArtistImage;
    private String mEventImage;
    private String mArtist;
    private String mEvent;
    private String mGenre;
    private String mSongURL;
    private String episode;
    private String setLength;
    private List<Track> mTracklist;
    private Integer popularity;
    private Integer mRadiomix;
    private String datetime;

//      Parcelable Implementation

    public static final Parcelable.Creator<Set> CREATOR = new Parcelable.Creator<Set>() {
        public Set createFromParcel(Parcel in) {
            return new Set(in);
        }

        public Set[] newArray(int size) {
            return new Set[size];
        }
    };

    private Set(Parcel in) {
        mId = in.readString();
        mArtistImage = in.readString();
        mEventImage = in.readString();
        mArtist = in.readString();
        mEvent = in.readString();
        mGenre = in.readString();
        mSongURL = in.readString();
        episode = in.readString();
        setLength = in.readString();
        popularity = in.readInt();
        mRadiomix = in.readInt();
        datetime = in.readString();
    }

    @Override
    public int describeContents() {
        return Integer.parseInt(getId());
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mId);
        out.writeString(mArtistImage);
        out.writeString(mEventImage);
        out.writeString(mArtist);
        out.writeString(mEvent);
        out.writeString(mGenre);
        out.writeString(mSongURL);
        out.writeString(episode);
        out.writeString(setLength);
        out.writeInt(popularity);
        out.writeInt(mRadiomix);
        out.writeString(datetime);

    }

    public Set() {
    }

    public Set(JSONObject json) {
        jsonModelString = json.toString();
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
            setEpisode(json.getString("episode"));
            setSetLength(json.getString("set_length"));
            setDatetime(json.getString("datetime"));

            setTracklist(generateTracklist(json.getString("tracklist"), json.getString("starttimes")));
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
        setSetLength(s.getSetLength());
        setIsRadiomix(s.isRadiomix());
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
        setTracklist(tracklist);
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
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
        this.mArtistImage = Constants.CLOUDFRONT_URL_FOR_IMAGES + image;
    }

    public String getEventImage() {
        return mEventImage;
    }

    public void setEventImage(String mEventImage) {
        this.mEventImage = Constants.CLOUDFRONT_URL_FOR_IMAGES + mEventImage;
    }

    public String getSongURL() {
        return mSongURL;
    }

    public void setSongURL(String mSongURL) {
        this.mSongURL = Constants.S3_ROOT_URL + mSongURL;
    }

    public String getEpisode() {
        return episode;
    }

    public void setEpisode(String episode) {
        this.episode = episode;
    }

    public String getSetLength() {
        return setLength;
    }

    public void setSetLength(String setLength) {
        this.setLength = setLength;
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
        List<String> tracklistArray = new ArrayList<String>();
        List<String> starttimesArray = new ArrayList<String>();
        if(tracklistString.contains(", ")) {
            tracklistArray = new ArrayList<String>(Arrays.asList(tracklistString.split(", ")));
            starttimesArray = new ArrayList<String>(Arrays.asList(starttimesString.split(", ")));
        }
        else {
            tracklistArray.add(tracklistString);
            starttimesArray.add(starttimesString);
        }
        List<Track> trackList = new ArrayList<Track>();
        while(tracklistArray.size() != starttimesArray.size()) {
            String lastValue = starttimesArray.get(starttimesArray.size() - 1);
            starttimesArray.add(lastValue);
        }
        for(int i = 0 ; i < tracklistArray.size() ; i++) {
            try {
                trackList.add(new Track(tracklistArray.get(i), starttimesArray.get(i)));
            } catch(IndexOutOfBoundsException e) {
                Log.v("Error", e.toString());
            }
        }
        return trackList;
    }
}