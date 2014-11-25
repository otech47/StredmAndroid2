package com.setmine.android.util;

import android.content.Context;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpUtils {
	private static final int TIMEOUT_CONNECTION = 10000;
	private static final int TIMEOUT_SOCKET = 10000;
	private final Context context;
	private InputStreamReader reader;
    private String apiUrl;
    private StringBuilder builder;

	public HttpUtils(Context context, String apiUrl) {
        this.context = context;
        this.apiUrl = apiUrl;
        builder = new StringBuilder();
	}

    public void sendPlayCountGetRequest(String id) {
        if(id != null) {
            String url = apiUrl + "playCount?id=" + id;
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
                try {
                    httpclient.execute(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

    public String getJSONStringFromURL(String route) {
        String url = apiUrl + route;
        String jsonString = "";
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
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String inputString;
                while((inputString = reader.readLine()) != null) {
                    builder.append(inputString);
                }
                reader.close();
                jsonString = builder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
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
