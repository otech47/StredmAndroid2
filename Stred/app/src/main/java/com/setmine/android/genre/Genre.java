package com.setmine.android.genre;


import com.setmine.android.api.JSONModel;

import org.json.JSONException;
import org.json.JSONObject;

public class Genre extends JSONModel {
	private String mId;
	private String mGenre;

    public Genre() {}
	
	public Genre(String id, String genre) {
		setId(id);
		setGenre(genre);
	}

    public Genre(JSONObject json) {
        jsonModelString = json.toString();
        try {
            setId(json.getString("genre"));
            setId(json.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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