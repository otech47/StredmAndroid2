package com.stredm.android;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by oscarlafarga on 9/18/14.
 */
public class PayloadDeserializer implements JsonDeserializer<Payload> {

    @Override
    public Payload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final Model[] featured = context.deserialize(jsonObject.get("featured"), Model[].class);
        final Payload result = new Payload(featured);
        return result;
    }
}
