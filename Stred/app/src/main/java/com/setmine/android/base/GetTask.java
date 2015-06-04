package com.setmine.android.base;

import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.widget.Toast;

import com.setmine.android.interfaces.OnTaskCompleted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetTask<T> extends AsyncTask<String, Void, List<T>> {
	private final OnTaskCompleted<T> listener;
	private final Context context;

	public GetTask(Context context, OnTaskCompleted<T> listener) {
		this.listener = listener;
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(List<T> result) {
		super.onPostExecute(result);
		if (!isCancelled() && result != null) {
			listener.onTaskCompleted(result);
		} else if (result == null) {
			Toast.makeText(context, "Check your Internet Connection",
					Toast.LENGTH_SHORT).show();
			listener.onTaskFailed();
		}

	}

	@Override
	public List<T> doInBackground(String... params) {
		List<T> result = new ArrayList<T>();

//		HttpUtils http = new HttpUtils(context);
//		JsonReader reader =  new JsonReader(http.getReaderFromScriptAndParams(params));
//		if (reader != null) {
//			try {
//				result = getResourceList(reader);
//			} catch (IOException e) {
//				e.printStackTrace();
//			} finally {
//				http.closeReader();
//			}
//		} else {
//			result = null;
//		}

		return result;
	}

	private List<T> getResourceList(JsonReader reader) throws IOException {
		List<T> resources = new ArrayList<T>();

		reader.beginArray();
		while (reader.hasNext()) {
			resources.add(readResource(reader));
		}
		reader.endArray();
		return resources;
	}

	protected T readResource(JsonReader reader) throws IOException {
		return null;
	}

}
