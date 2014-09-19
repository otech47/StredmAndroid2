package com.stredm.android;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.google.gson.Gson;
import com.stredm.android.object.Artist;
import com.stredm.android.object.Event;
import com.stredm.android.object.Genre;
import com.stredm.android.object.Mix;
import com.stredm.android.object.Set;
import com.stredm.android.object.Track;
import com.stredm.android.util.HttpUtils;

public class ModelsContentProvider extends ContentProvider {

    private Artist[] artists;
    private Event[] events;
    private Mix[] mixes;
    private Genre[] genres;
    private Set[] popularSets;
    private Set[] recentSets;
    private Track[] tracks;
    private Event[] upcomingEvents;
    private Event[] recentEvents;
    private HttpUtils apiCallsManager = new HttpUtils(getContext());
    private Gson gson = new Gson();


    @Override
    public boolean onCreate() {
        // Initialize only upcomingEvents and recentEvents through API call
//        ApiCallTask recentEventsCall = new ApiCallTask(getContext());
//        try {
//            ApiResponse response = recentEventsCall.execute("featured").get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        ApiCallTask upcomingEventsCall = new ApiCallTask(getContext());
//        try {
//            ApiResponse response = recentEventsCall.execute("upcoming").get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        String type = "json";
        return type;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

}
