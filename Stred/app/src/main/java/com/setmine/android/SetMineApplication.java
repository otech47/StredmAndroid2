package com.setmine.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Application;
import android.content.res.Configuration;

import com.setmine.android.artist.Artist;
import com.setmine.android.event.Event;
import com.setmine.android.genre.Genre;
import com.setmine.android.set.Mix;
import com.setmine.android.set.Set;

public class SetMineApplication extends Application {
	private List<Set> playlist;
	private List<Set> playlistShuffled;
	private List<Artist> artists;
	private List<Event> events;
	private List<Mix> mixes;
	private List<Genre> genres;
	private int playlistLength = 0;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public List<Set> getPlaylist() {
		return this.playlist;
	}

	public void setPlaylist(List<Set> pl) {
		this.playlist = pl;
		this.playlistShuffled = new ArrayList<Set>(pl);
		Collections.shuffle(this.playlistShuffled);
		this.setPlaylistLength(pl.size());
	}

	public List<Set> getPlaylistShuffled() {
		return playlistShuffled;
	}

	public int getPlaylistLength() {
		return playlistLength;
	}

	public void setPlaylistLength(int playlistLength) {
		this.playlistLength = playlistLength;
	}

	public boolean playlistEmpty() {
		return (playlistLength != 0) ? false : true;
	}

	public List<Artist> getArtists() {
		return artists;
	}

	public void setArtists(List<Artist> artists) {
		this.artists = artists;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public List<Mix> getMixes() {
		return mixes;
	}

	public void setMixes(List<Mix> mixes) {
		this.mixes = mixes;
	}

	public List<Genre> getGenres() {
		return genres;
	}

	public void setGenres(List<Genre> genres) {
		this.genres = genres;
	}
}