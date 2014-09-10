package com.stredm.android.task;

import java.io.IOException;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;

import com.stredm.android.OnTaskCompleted;
import com.stredm.android.object.Artist;

public class GetArtistsTask extends GetTask<Artist> {

    public GetArtistsTask(Context context, OnTaskCompleted<Artist> listener) {
		super(context, listener);
	}
	
    @Override
	protected Artist readResource(JsonReader reader) throws IOException {
		Artist a = new Artist();
		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if(reader.peek() == JsonToken.NULL) {
				// do nothing
				reader.skipValue();
			} else if(name.equals("id")) {
				a.setId(reader.nextString());
			} else if(name.equals("artist")) {
				a.setArtist(reader.nextString());
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return a;
	}

}
