package com.stredm.flume.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.Uri;
import android.util.JsonReader;

public class HttpUtils {
	private static final int TIMEOUT_CONNECTION = 10000;
	private static final int TIMEOUT_SOCKET = 10000;
	private final Context context;
	private JsonReader reader;

	public HttpUtils(Context context) {
		this.context = context;
	}

	public JsonReader getReaderFromScriptAndParams(String... params) {
		int len = params.length;
		String url = "http://stredm.com/api/";
		if (len > 0) {
			if (params[0] == "search") {
				url += params[0];
			} else if (params[0] == "random" || params[0] == "artists"
					|| params[0] == "events" || params[0] == "radiomixes"
					|| params[0] == "genres") {
				url = "http://stredm.com/scripts/mobile/" + params[0] + ".php";
			} else {
				url += params[0];
			}
		} else {
			return null;
		}
		Uri uri = Uri.parse(url);
		if (len == 3) {
			url = uri.buildUpon().appendQueryParameter("search", params[2])
					.appendQueryParameter("type", params[1]).toString();
		} else if (len == 2) {
			url = uri.buildUpon().appendQueryParameter("search", params[1])
					.toString();
		}

		return getReaderFromURL(url);
	}

	public JsonReader getReaderFromURL(String url) {

		reader = null;
		try {
			HttpResponse response = null;
			HttpParams httpParameters = new BasicHttpParams();
			HeaderGroup headers = new HeaderGroup();

			HttpConnectionParams.setConnectionTimeout(httpParameters,
					TIMEOUT_CONNECTION);
			HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT_SOCKET);

			HttpClient httpclient = new DefaultHttpClient(httpParameters);

			HttpGet request = new HttpGet(url);
			request.setHeaders(headers.getAllHeaders());
			ConnectionUtils cd = new ConnectionUtils(context);
			if (cd.isConnectingToInternet()) {
				response = httpclient.execute(request);
				InputStream in = response.getEntity().getContent();
				reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reader;
	}

	public void closeReader() {
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
