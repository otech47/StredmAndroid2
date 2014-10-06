package com.stredm.android;

import org.json.JSONObject;

/**
 * Created by oscarlafarga on 10/2/14.
 */
public interface LineupsSetsApiCaller {
    public void onLineupsSetsReceived(JSONObject jsonObject, String identifier);
}
