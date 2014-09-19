package com.stredm.android.util;

import android.content.Context;
import android.net.Uri;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpUtils {
	private static final int TIMEOUT_CONNECTION = 10000;
	private static final int TIMEOUT_SOCKET = 10000;
    private static final int API_VERSION = 1;
	private final Context context;
	private InputStreamReader reader;
    private String apiUrl = "http://stredm.com/api/v/" + API_VERSION + "/";

	public HttpUtils(Context context) {
		this.context = context;
	}

	public InputStreamReader getReaderFromScriptAndParams(String... params) {
		int len = params.length;
		String url = "http://stredm.com/api/v/" + API_VERSION + "/";
		if (len > 0) {
			if (params[0] instanceof String) {
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

	public InputStreamReader getReaderFromURL(String route) {
        String url = apiUrl + route;
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
				reader = new InputStreamReader(in, "UTF-8");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reader;
	}

    public InputStreamReader getReaderFromFullURL(String fullUrl) {
        String url = fullUrl;
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
                reader = new InputStreamReader(in, "UTF-8");
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
