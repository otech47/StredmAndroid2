package com.setmine.android.set;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.setmine.android.api.JSONModel;
import com.setmine.android.track.Track;
import com.setmine.android.Constants;
import com.setmine.android.util.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Set extends JSONModel implements Parcelable {

    protected String mId;
    protected String mArtistImage;
    protected String mEventImage;
    protected String mArtist;
    protected String mEvent;
    protected int mEventID;
    protected String mGenre;
    protected String mSongURL;
    protected String episode;
    protected String setLength;
    protected List<Track> mTracklist;
    protected Integer popularity;
    protected Integer mRadiomix;
    protected String datetime;

//      Parcelable Implementation

    public static final Parcelable.Creator<Set> CREATOR = new Parcelable.Creator<Set>() {
        public Set createFromParcel(Parcel in) {
            return new Set(in);
        }

        public Set[] newArray(int size) {
            return new Set[size];
        }
    };

    protected Set(Parcel in) {
        mId = in.readString();
        mArtistImage = in.readString();
        mEventImage = in.readString();
        mArtist = in.readString();
        mEvent = in.readString();
        mEventID = in.readInt();
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
        out.writeInt(mEventID);
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
            mId = json.getString("id");
            mArtistImage = Constants.CLOUDFRONT_URL_FOR_IMAGES + json.getString("artistimageURL");
            mEventImage = Constants.CLOUDFRONT_URL_FOR_IMAGES + json.getString("eventimageURL");
            mArtist = json.getString("artist");
            mEvent = json.getString("event");
            mEventID = json.getInt("event_id");
            mGenre = json.getString("genre");
            mSongURL = Constants.S3_ROOT_URL + json.getString("songURL");
            popularity = json.getInt("popularity");
            mRadiomix = json.getInt("is_radiomix");
            episode = json.getString("episode");
            setLength = json.getString("set_length");
            datetime = json.getString("datetime");

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

    public int getEventID() {
        return mEventID;
    }

    public void setEventID(int mEventID) {
        this.mEventID = mEventID;
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

}