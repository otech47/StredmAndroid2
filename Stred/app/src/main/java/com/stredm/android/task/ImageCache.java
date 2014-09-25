package com.stredm.android.task;

import android.graphics.Bitmap;

import com.stredm.android.BitmapCache;

/**
 * Created by oscarlafarga on 9/19/14.
 */
public class ImageCache implements BitmapCache {

    @Override
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if(getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    @Override
    public Bitmap getBitmapFromMemCache(String key) {
        if(mMemoryCache.get(key) == null) {
            return null;
        }
        else {
            return mMemoryCache.get(key);
        }
    }
}
