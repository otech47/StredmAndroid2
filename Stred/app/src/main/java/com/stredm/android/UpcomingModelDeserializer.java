package com.stredm.android;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by oscarlafarga on 9/19/14.
 */
public class UpcomingModelDeserializer implements JsonDeserializer<UpcomingModel> {

    @Override
    public UpcomingModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        Model[] soonestArr = context.deserialize(jsonObject.get("soonestEvents"), Model[].class);
        Model[] closestArr = context.deserialize(jsonObject.get("closestEvents"), Model[].class);
        List<Model> soonest = new ArrayList<Model>(Arrays.asList(soonestArr));
        List<Model> closest = new ArrayList<Model>(Arrays.asList(closestArr));
        final UpcomingModel result = new UpcomingModel(soonest, closest);
        return result;
    }
}
