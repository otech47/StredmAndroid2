package com.stredm.android.task;

import android.graphics.Bitmap;
import android.util.Log;

import com.stredm.android.BitmapCache;

/**
 * Created by oscarlafarga on 9/19/14.
 */
public class ImageCache implements BitmapCache {

    @Override
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        Log.v("Max memory", ((Integer)mMemoryCache.size()).toString());
        if(getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
            Log.v("Added bitmap to cache", key + " " + mMemoryCache.size());
            Log.v("Cache info: ", mMemoryCache.toString());
        }
    }

    @Override
    public Bitmap getBitmapFromMemCache(String key) {
        if(mMemoryCache.get(key) == null) {
            Log.v("bitmap not found", key);
            return null;
        }
        else {
            Log.v("bitmap found", key);
            return mMemoryCache.get(key);
        }
    }
}
