package com.setmine.android.object;

import android.os.Parcel;
import android.os.Parcelable;

import com.setmine.android.set.Set;
import com.setmine.android.track.Track;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TrackResponse extends Set {
    protected String mTrackName;
    protected String mArtistName;
    protected String mSongName;
    protected String mStartTime;

    //      Parcelable Implementation

    public static final Parcelable.Creator<TrackResponse> CREATOR = new Parcelable.Creator<TrackResponse>() {
        public TrackResponse createFromParcel(Parcel in) {
            return new TrackResponse(in);
        }

        public TrackResponse[] newArray(int size) {
            return new TrackResponse[size];
        }
    };

    protected TrackResponse(Parcel in) {
        mId = in.readString();
        mTrackName = in.readString();
        mArtistName = in.readString();
        mSongName = in.readString();
        mStartTime = in.readString();
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
    }

    @Override
    public int describeContents() {
        return Integer.parseInt(getId());
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mId);
        out.writeString(mTrackName);
        out.writeString(mArtistName);
        out.writeString(mSongName);
        out.writeString(mStartTime);
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

    }

    public TrackResponse() {
    }

    public TrackResponse(JSONObject json) {
        jsonModelString = json.toString();
        try {
            setId(json.getString("id"));
            setTrackName(json.getString("trackname"));
            setArtistName(json.getString("artistname"));
            setSongName(json.getString("songname"));
            setStartTime(json.getString("starttime"));
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
            setTracklist(generateTracklist(json.getString("tracklist"), json.getString("starttimes")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public TrackResponse(TrackResponse tr) {
        setId(tr.getId());
        setTrackName(tr.getTrackName());
        setArtistName(tr.getArtistName());
        setSongName(tr.getSongName());
        setStartTime(tr.getStartTime());
        setArtist(tr.getArtist());
        setEvent(tr.getEvent());
        setGenre(tr.getGenre());
        setArtistImage(tr.getArtistImage());
        setEventImage(tr.getEventImage());
        setSongURL(tr.getSongURL());
        setSetLength(tr.getSetLength());
        setIsRadiomix(tr.isRadiomix());
        setTracklist(tr.getTracklist());
    }

    public TrackResponse(String id, String trackName, String artistName, String songName, String startTime, String artist, String event, String genre,
               String imageURL, String songURL, List<Track> tracklist,
               Integer isRadiomix, boolean isDownloaded) {
        setId(id);
        setTrackName(trackName);
        setArtistName(artistName);
        setSongName(songName);
        setStartTime(startTime);
        setArtist(artist);
        setEvent(event);
        setGenre(genre);
        setArtistImage(imageURL);
        setSongURL(songURL);
        setIsRadiomix(isRadiomix);
        setTracklist(tracklist);
    }

    public Set getSet() {
        Set s;
        s = new Set(this.mId, this.mArtist, this.mEvent, this.mGenre,
                this.mEventImage, this.mSongURL, this.mTracklist,
                this.mRadiomix, false);
        return s;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public void setTrackName(String mTrackName) {
        this.mTrackName = mTrackName;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String mArtistName) {
        this.mArtistName = mArtistName;
    }

    public String getSongName() {
        return mSongName;
    }

    public void setSongName(String mSongName) {
        this.mSongName = mSongName;
    }

    public String getStartTime() {
        return mStartTime;
    }

    public void setStartTime(String mStartTime) {
        this.mStartTime = mStartTime;
    }

}