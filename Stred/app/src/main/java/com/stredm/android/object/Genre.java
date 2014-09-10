package com.stredm.android.object;


public class Genre {
	private String mId;
	private String mGenre;

	public Genre() {
		// default testing values
		this("-1",
			"genre");
	}
	
	public Genre(String id, String genre) {
		setId(id);
		setGenre(genre);
	}
	
	public String getId() {
		return mId;
	}

	public void setId(String mId) {
		this.mId = mId;
	}

	public String getGenre() {
		return mGenre;
	}
	
	public void setGenre(String genre) {
		this.mGenre = genre;
	}

}