package com.stredm.flume.task;

import java.io.IOException;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonToken;

import com.stredm.flume.OnTaskCompleted;
import com.stredm.flume.object.Radiomix;

public class GetRadiomixesTask extends GetTask<Radiomix> {

	public GetRadiomixesTask(Context context, OnTaskCompleted<Radiomix> listener) {
		super(context, listener);
	}

	@Override
	protected Radiomix readResource(JsonReader reader) throws IOException {
		Radiomix r = new Radiomix();
		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if(reader.peek() == JsonToken.NULL) {
				// do nothing
				reader.skipValue();
			} else if(name.equals("id")) {
				r.setId(reader.nextString());
			} else if(name.equals("radiomix")) {
				r.setRadiomix(reader.nextString());
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return r;
	}

}
