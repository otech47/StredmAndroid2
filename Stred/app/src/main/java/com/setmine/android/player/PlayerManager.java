package com.setmine.android.player;

import com.setmine.android.set.Set;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by oscarlafarga on 9/23/14.
 */
public class PlayerManager {

    private List<Set> playlist = new ArrayList<Set>();
    private List<Set> playlistShuffled;
    private int playlistLength = 0;
    public Set selectedSet;
    public int selectedSetIndex;

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
        this.playlist = pl;
        this.playlistShuffled = new ArrayList<Set>(pl);
        Collections.shuffle(this.playlistShuffled);
        setPlaylistLength(pl.size());
    }

    public void selectSetById(String setid) {
        for(int i = 0; i < playlist.size(); i++) {
            if(playlist.get(i).getId().equals(setid)) {
                selectedSetIndex = i;
                selectedSet = playlist.get(i);
            }
        }
    }

    public void selectSetByIndex(int index) {
        this.selectedSetIndex = index;
        this.selectedSet = this.playlist.get(this.selectedSetIndex);
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

}
