package com.setmine.android.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.stredm.android.ImageCache;
import com.stredm.android.SetMineMainActivity;
import com.stredm.android.object.ImageViewChangeRequest;

import java.io.InputStream;
import java.lang.ref.WeakReference;

public class GetImageAsyncTask extends AsyncTask<ImageViewChangeRequest, Void, Bitmap> {
    public ImageCache imageCache;
    public WeakReference<ImageView> imageViewReference;
    public String imageStorageRoot;
    public SetMineMainActivity activity;

    public GetImageAsyncTask(SetMineMainActivity activity, ImageCache cache, String imageStorageRoot) {
        this.activity = activity;
        this.imageStorageRoot = imageStorageRoot;
        this.imageCache = cache;
    }

    @Override
    protected void onPreExecute() {
        if(activity.asyncTasksInProgress > 125) {
            this.cancel(true);
            activity.asyncTasksInProgress--;
        }
        activity.asyncTasksInProgress++;
        Log.v("Task started. Still in queue: ", ((Integer) activity.asyncTasksInProgress).toString());
    }

    @Override
    protected Bitmap doInBackground(ImageViewChangeRequest... requests) {
        String imageUrl = imageStorageRoot + requests[0].imageURL;
        if(requests[0].imageView != null) {
            this.imageViewReference = new WeakReference<ImageView>(requests[0].imageView);
        }
        Bitmap image = null;
        if(imageCache.getBitmapFromMemCache(imageUrl) == null) {
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, options);
                options.inSampleSize = 4;
                options.inJustDecodeBounds = false;
                in = new java.net.URL(imageUrl).openStream();
                image = BitmapFactory.decodeStream(in, null, options);
                imageCache.addBitmapToMemoryCache(imageUrl, image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            image = imageCache.getBitmapFromMemCache(imageUrl);
        }
        return image;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        activity.asyncTasksInProgress--;
        Log.v("Task complete. Still in queue: ", ((Integer)activity.asyncTasksInProgress).toString());
        if(imageViewReference != null && result != null) {
            final ImageView imageView = imageViewReference.get();
            Log.v("Image Bitmap Set, ", "onPostExecute");
            imageView.setImageBitmap(result);
        }
    }

    public static Integer calculateInSampleSize(BitmapFactory.Options options, Integer reqHeight, Integer reqWidth) {
        final Integer height = options.outHeight;
        final Integer width = options.outWidth;
        Integer inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.v("sample size", inSampleSize.toString());
        return inSampleSize;
    }
}

