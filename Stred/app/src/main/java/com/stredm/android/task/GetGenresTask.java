package com.stredm.flume.task;

import java.io.IOException;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;

import com.stredm.flume.OnTaskCompleted;
import com.stredm.flume.object.Genre;

public class GetGenresTask extends GetTask<Genre> {
	
	public GetGenresTask(Context context, OnTaskCompleted<Genre> listener) {
		super(context, listener);
	}

	@Override
	protected Genre readResource(JsonReader reader) throws IOException {
		Genre g = new Genre();
		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if(reader.peek() == JsonToken.NULL) {
				// do nothing
				reader.skipValue();
			} else if(name.equals("id")) {
				g.setId(reader.nextString());
			} else if(name.equals("genre")) {
				g.setGenre(reader.nextString());
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return g;
	}

}
