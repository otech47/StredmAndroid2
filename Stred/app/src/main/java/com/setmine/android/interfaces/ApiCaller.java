package com.setmine.android.interfaces;

import org.json.JSONObject;

/**
 * Created by oscarlafarga on 9/21/14.
 */
public interface ApiCaller {

    public void onApiResponseReceived(JSONObject jsonObject, String identifier);

}
