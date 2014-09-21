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
 * Created by oscarlafarga on 9/18/14.
 */
public class PayloadDeserializer implements JsonDeserializer<Payload> {

    @Override
    public Payload deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.entrySet().iterator().next().getKey();
        if(type.equals("featured")) {
            Model[] dataArr = context.deserialize(jsonObject.get(type), Model[].class);
            List<Model> data = new ArrayList<Model>(Arrays.asList(dataArr));
            Payload<List<Model>> result = new Payload<List<Model>>(data, type);
            return result;
        }
        else if(type.equals("upcoming")) {
            UpcomingModel data = context.deserialize(jsonObject.get(type), UpcomingModel.class);
            Payload<UpcomingModel> result = new Payload<UpcomingModel>(data, type);
            return result;
        }
        else
            return null;
    }
}
