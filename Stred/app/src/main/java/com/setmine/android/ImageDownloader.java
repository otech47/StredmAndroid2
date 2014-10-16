package com.setmine.android;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by oscarlafarga on 9/23/14.
 */
public interface ImageDownloader  {
    public void onImageDownloaded(ImageView imageView, Bitmap image);
}
