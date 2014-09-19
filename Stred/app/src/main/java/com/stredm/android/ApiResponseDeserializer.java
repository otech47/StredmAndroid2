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
public class ApiResponseDeserializer implements JsonDeserializer<ApiResponse> {

    @Override
    public ApiResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final int version = jsonObject.get("version").getAsInt();
        final String status = jsonObject.get("status").getAsString();
        final Payload payload = context.deserialize(jsonObject.get("payload"), Payload.class);
        final ApiResponse result = new ApiResponse(version, status, payload);
        return result;
    }
}
