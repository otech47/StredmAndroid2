package com.stredm.android.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	private Context context;
	public String imageUrl;
    public ImageCache imageCache;
    private final WeakReference<ImageView> imageViewReference;
    public TileGenerator tilegen;

	public DownloadImageTask(Context context, ImageCache cache, ImageView imageView, TileGenerator tilegen) {
		this.context = context;
        this.imageCache = cache;
        this.imageViewReference = new WeakReference<ImageView>(imageView);
        this.tilegen = tilegen;
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

    @Override
	protected Bitmap doInBackground(String... urls) {
		this.imageUrl = urls[0];
		Bitmap image = null;
        if(imageCache.getBitmapFromMemCache(imageUrl) == null) {
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(in, null, options);
                options.inSampleSize = 2;
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
		if(imageViewReference != null && result != null) {
            final ImageView imageView = imageViewReference.get();
            tilegen.onDownloadImage(imageView, result);
        }
	}
}

