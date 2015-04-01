package com.setmine.android.interfaces;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public interface BitmapCache {

	final int maxMemory = (int) (Runtime.getRuntime().maxMemory()) / 1024;

    final int cacheSize = maxMemory / 4;

    LruCache<String, Bitmap> mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            return bitmap.getByteCount() / 1024;
        }
    };

	public void addBitmapToMemoryCache(String key, Bitmap bitmap);

	public Bitmap getBitmapFromMemCache(String key);

}