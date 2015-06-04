package com.setmine.android.image;

import android.widget.ImageView;

/**
 * Created by oscarlafarga on 9/29/14.
 */
public class ImageViewChangeRequest {
    public String imageURL;
    public ImageView imageView;

    public ImageViewChangeRequest(String imageURL, ImageView imageView) {
        this.imageURL = imageURL;
        if(imageView != null)
            this.imageView = imageView;
    }
}
