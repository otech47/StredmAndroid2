package com.setmine.android.api;

import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/21/14.
 */
public interface InitialApiCaller {
    public void onInitialResponseReceived(JSONObject jsonObject, String identifier);
}
