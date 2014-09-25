package com.stredm.android.task;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;

import com.stredm.android.OnTaskCompleted;
import com.stredm.android.object.Set;
import com.stredm.android.object.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GetSetsTask extends GetTask<Set> {

	public GetSetsTask(Context context, OnTaskCompleted<Set> listener) {
		super(context, listener);
	}

	@Override
	protected Set readResource(JsonReader reader) throws IOException {
		Set r = new Set();
		List<Track> tracklist = new ArrayList<Track>();
		List<String> tracks = null;
		List<String> times = null;

		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (reader.peek() == JsonToken.NULL) {
				// do nothing
				reader.skipValue();
			} else if (name.equals("id")) {
				r.setId(reader.nextString());
			} else if (name.equals("artist")) {
				r.setArtist(reader.nextString());
			} else if (name.equals("event")) {
				r.setEvent(reader.nextString());
			} else if (name.equals("genre")) {
				r.setGenre(reader.nextString());
			} else if (name.equals("imageURL")) {
				r.setArtistImage("http://stredm.com/uploads/" + reader.nextString());
			} else if (name.equals("songURL")) {
				r.setSongURL("http://stredm.com/uploads/" + reader.nextString());
			} else if (name.equals("tracklist")) {
				String x = reader.nextString();
				tracks = Arrays.asList(x.split("\\s*,\\s*"));
			} else if (name.equals("starttimes")) {
				String x = reader.nextString();
				times = Arrays.asList(x.split("\\s*,\\s*"));
			} else if (name.equals("is_radiomix")) {
				r.setIsRadiomix(reader.nextInt());
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();

		if (tracks != null && times != null && tracks.size() == times.size()) {
			for (int i = 0; i < tracks.size(); i++) {
				Track t = new Track(tracks.get(i), times.get(i));
				tracklist.add(t);
			}
			r.setTracklist(tracklist);
		}
		return r;
	}
}
