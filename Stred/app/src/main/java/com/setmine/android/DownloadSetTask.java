package com.setmine.android;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat.Builder;

import com.setmine.android.R;
import com.setmine.android.set.Set;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

// usually, subclasses of AsyncTask are declared inside the activity class.
// that way, you can easily modify the UI thread from here
public class DownloadSetTask extends AsyncTask<String, Integer, String> {

	private final Context context;
	private PowerManager.WakeLock mWakeLock;
	// declare the dialog as a member field of your activity
	private final NotificationManager mNotifyManager;
	private final Builder mBuilder;
	private final String mTitle;
	private final String mSongURL;
	private final Set set;
	private final DatabaseHandler db;

	public DownloadSetTask(Context context, NotificationManager notifyManager,
			Builder builder, String title, String songURL, Set set,
			DatabaseHandler db) {
		this.context = context;
		this.mNotifyManager = notifyManager;
		this.mBuilder = builder;
		this.mTitle = title;
		this.mSongURL = songURL;
		this.set = set;
		this.db = db;
	}

	@Override
	protected String doInBackground(String... sUrl) {
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(sUrl[0]);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			// expect HTTP 200 OK, so we don't mistakenly save error report
			// instead of the file
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return "Server returned HTTP " + connection.getResponseCode()
						+ " " + connection.getResponseMessage();
			}

			// this will be useful to display download percentage
			// might be -1: server did not report the length
			int fileLength = connection.getContentLength();

			// download the file
			input = connection.getInputStream();
			File file = new File(mSongURL);
			output = new FileOutputStream(file);

			byte data[] = new byte[4096];
			long total = 0;
			int count;
			while ((count = input.read(data)) != -1) {
				// allow canceling with back button
				// if (isCancelled()) {
				// input.close();
				// return null;
				// }
				total += count;
				// publishing the progress....
				if (fileLength > 0) // only if total length is known
					publishProgress((int) (total * 100 / fileLength));
				output.write(data, 0, count);
			}
		} catch (Exception e) {
			return e.toString();
		} finally {
			try {
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} catch (IOException ignored) {
			}

			if (connection != null)
				connection.disconnect();
		}
		return null;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// take CPU lock to prevent CPU from going off if the user
		// presses the power button during download
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass()
				.getName());
		mWakeLock.acquire();
		mBuilder.setContentTitle("Downloading " + mTitle)
				.setContentText("0% Downloaded").setSmallIcon(R.drawable.logo)
				.setOngoing(true);
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		// if we get here, length is known, now set indeterminate to false
		mBuilder.setContentText(progress[0] + "% Downloaded")
				.setProgress(100, progress[0], false).setOngoing(true);
		mNotifyManager.notify(1324, mBuilder.build());
	}

	@Override
	protected void onPostExecute(String result) {
		mWakeLock.release();
		mBuilder.setContentTitle(mTitle).setContentText("Download Complete")
				.setProgress(0, 0, false).setOngoing(false);
		mNotifyManager.notify(1324, mBuilder.build());
		db.updateSet(db.getSet("1"), "2");
		db.updateSet(set, "1");
		db.cleanupFiles();
	}
}