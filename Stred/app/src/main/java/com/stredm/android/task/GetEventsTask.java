package com.stredm.android.task;

import java.io.IOException;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;

import com.stredm.android.OnTaskCompleted;
import com.stredm.android.object.Event;

public class GetEventsTask extends GetTask<Event> {

    public GetEventsTask(Context context, OnTaskCompleted<Event> listener) {
		super(context, listener);
	}

    @Override
	protected Event readResource(JsonReader reader) throws IOException {
		Event e = new Event();
		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if(reader.peek() == JsonToken.NULL) {
				// do nothing
				reader.skipValue();
			} else if(name.equals("id")) {
				e.setId(reader.nextString());
			} else if(name.equals("event")) {
				e.setEvent(reader.nextString());
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return e;
	}

}
