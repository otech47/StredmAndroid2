package com.setmine.android.player;

import android.util.Log;

import com.setmine.android.set.Set;
import com.setmine.android.track.Track;
import com.setmine.android.util.TimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by oscarlafarga on 9/23/14.
 */
public class PlayerManager {

    private static final String TAG = "PlayerManager";


    private List<Set> playlist = new ArrayList<Set>();
    private List<Set> playlistShuffled;
    private int playlistLength = 0;
    public Set selectedSet;
    public int selectedSetIndex;
    public List<Track> currentTracklist;

    public PlayerManager() {
        selectedSet = null;
        currentTracklist = new ArrayList<Track>();
    }

    public List<Set> getPlaylist() {
        return this.playlist;
    }

    public void clearPlaylist() {
        getPlaylist().clear();
        setPlaylistLength(0);
        selectedSet = null;
    }

    public void addToPlaylist(Set set) {
        playlist.add(set);
        setPlaylistLength(getPlaylistLength()+1);
    }

    public void setPlaylist(List<Set> pl) {
        Log.d(TAG, "setPlaylist");
        this.playlist = pl;
        this.playlistShuffled = new ArrayList<Set>(pl);
        Collections.shuffle(this.playlistShuffled);
        setPlaylistLength(pl.size());
    }

    public void selectSetById(String setid) {
        Log.d(TAG, "selectSetById: " + setid);
        for(int i = 0; i < playlist.size(); i++) {
            if(playlist.get(i).getId().equals(setid)) {
                selectedSetIndex = i;
                selectedSet = playlist.get(i);
            }
        }
    }

    public void selectSetByIndex(int index) {
        selectedSetIndex = index;
        selectedSet = this.playlist.get(this.selectedSetIndex);

    }

    public Set getSelectedSet() {
        return this.selectedSet;
    }

    public int getPlaylistLength() {
        return playlistLength;
    }

    public void setPlaylistLength(int playlistLength) {
        this.playlistLength = playlistLength;
    }

    public void setTracklist(List<Track> tracklist) {
        this.currentTracklist = tracklist;
    }

    public List<Track> getTracklist() {
        return currentTracklist;
    }

    public Track getCurrentTrack(long time) {
        Track currentTrack = new Track();
        if(currentTracklist.size() > 0) {
            for (int i = 0; i < currentTracklist.size(); i++) {
                Track t = currentTracklist.get(i);
                TimeUtils utils = new TimeUtils();
                if (utils.timerToMilliSeconds(t.getStartTime()) <= time) {
                    currentTrack = t;
                } else {
                    break;
                }
            }
        }
        return currentTrack;
    }

}
