package com.stredm.flume.task;

import java.io.FileOutputStream;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.stredm.flume.BitmapCache;
import com.stredm.flume.ViewHolder;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	private final ViewHolder viewHolder;
	private final BitmapCache listener;
	private String urldisplay;
	private final int position;
	private final String storeFilename;

	public DownloadImageTask(ViewHolder viewHolder, BitmapCache listener) {
		this.storeFilename = "";
		this.viewHolder = viewHolder;
		this.listener = listener;
		this.position = viewHolder.position;
	}

	public DownloadImageTask(String filename) {
		this.storeFilename = filename;
		this.viewHolder = null;
		this.listener = null;
		this.position = -1;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// pd.show();
	}

	@Override
	protected Bitmap doInBackground(String... urls) {
		this.urldisplay = urls[0];
		if (!this.urldisplay.contains("http")) {
			this.urldisplay = "file://" + this.urldisplay;
		}
		if (listener != null) {
			Bitmap bmp = listener.getBitmapFromMemCache(urldisplay);
			if (bmp != null) {
				if (!storeFilename.isEmpty()) {
					storeFile(bmp);
				}
				return bmp;
			}
		}
		Bitmap mIcon11 = null;
		try {
			InputStream in = new java.net.URL(urldisplay).openStream();
			mIcon11 = BitmapFactory.decodeStream(in);
			if (!storeFilename.equals("")) {
				storeFile(mIcon11);
			}
		} catch (Exception e) {
			// Log.e("Error", e.getMessage());
			e.printStackTrace();
		}
		return mIcon11;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		super.onPostExecute(result);
		// pd.dismiss();
		if (listener != null) {
			listener.addBitmapToMemoryCache(urldisplay, result);
		}
		if (viewHolder != null && position == viewHolder.position) {
			viewHolder.imageView.setImageBitmap(result);
			if (viewHolder.imageThumb != null) {
				viewHolder.imageThumb.setImageBitmap(result);
			}
		}
	}

	private void storeFile(Bitmap bmp) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(storeFilename);
			bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (Throwable ignore) {
			}
		}
	}
}