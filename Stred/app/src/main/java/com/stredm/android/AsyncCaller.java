package com.stredm.android;

import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/21/14.
 */
public interface AsyncCaller {
    public void onApiResponse(JSONObject jsonObject, boolean alternative);
}
