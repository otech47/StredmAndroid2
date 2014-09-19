package com.stredm.android.task;

import com.stredm.android.ApiResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarlafarga on 9/18/14.
 */
public class ApiResponseCache {

    public List<ApiResponse> cache = new ArrayList<ApiResponse>();

    public void addToCache(ApiResponse res) {
        cache.add(res);
    }


}
