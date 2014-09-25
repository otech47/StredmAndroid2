package com.stredm.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stredm.android.task.GetLineupTask;
import com.stredm.android.task.ImageCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by oscarlafarga on 9/22/14.
 */
public class EventDetailFragment extends Fragment implements ImageDownloader{

    public View rootView;
    private static final String amazonS3Url = "http://stredm.s3-website-us-east-1.amazonaws.com/namecheap/";
    public Integer EVENT_ID;
    public String EVENT_NAME;
    public String EVENT_DATE;
    public String EVENT_DATE_UNFORMATTED;
    public String EVENT_CITY;
    public String EVENT_IMAGE;
    public String EVENT_TYPE;
    public ImageCache imageCache;
    public List<HashMap<String, String>> setMapsList;
    public View lineupContainer;
    public JSONObject savedApiResponse = null;
    public SetsManager setsManager;
    public Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("Detail Fragment Created", "");
        setMapsList = new ArrayList<HashMap<String, String>>();
        imageCache = ((EventPagerActivity)getActivity()).tileGen.imageCache;
        setsManager = ((EventPagerActivity)getActivity()).setsManager;
        context = getActivity().getApplicationContext();
        GetLineupTask getLineupTask = new GetLineupTask(getActivity().getApplicationContext(), this);
        if(EVENT_TYPE.equals("recent")) {
            Log.v("Detail Fragment Requesting Sets", "");
            getLineupTask.execute("festival?search="+ Uri.encode(EVENT_NAME));
        }
        else if(EVENT_TYPE.equals("upcoming")) {
            Log.v("Detail Fragment Requesting Lineup", "");
            getLineupTask.execute("lineup/"+ Uri.encode(EVENT_ID.toString()));
        }
        else {
            Log.v("Detail Fragment has no type", "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v("Detail Fragment View Created", "");
        if(savedApiResponse == null) {
            Log.v("Reinflating rootview", "74");
            rootView = inflater.inflate(R.layout.event_detail, container, false);
            setImageBackground(EVENT_IMAGE, (ImageView)rootView.findViewById(R.id.eventImage));
            ((TextView)rootView.findViewById(R.id.eventText)).setText(EVENT_NAME);
            if(EVENT_TYPE == "recent") {
                ((TextView)rootView.findViewById(R.id.eventText)).setBackgroundResource(R.color.setmine_blue);
                ((TextView)rootView.findViewById(R.id.lineupText)).setText("Sets");
            }
            else {
                ((TextView)rootView.findViewById(R.id.eventText)).setBackgroundResource(R.color.setmine_purple);
            }
            ((TextView)rootView.findViewById(R.id.dateText)).setText(EVENT_DATE);
            ((TextView)rootView.findViewById(R.id.locationText)).setText(EVENT_CITY);
            lineupContainer = rootView.findViewById(R.id.lineupContainer);
            return rootView;
        }
        else {
            return rootView;
        }
    }

    @Override
    public void onDestroyView() {
        rootView = getView();
        super.onDestroyView();
    }

    public void onApiResponse(JSONObject jsonResponse) {
        savedApiResponse = jsonResponse;
        try {
            if(EVENT_TYPE.equals("recent")) {
                if(jsonResponse.getString("status").equals("success")) {
                    JSONArray setsJSON = getSetsFromJson(jsonResponse);
                    LayoutInflater inflater = ((EventPagerActivity)lineupContainer.getContext()).getLayoutInflater();
                    setsManager.clearPlaylist();
                    for (int i = 0; i < setsJSON.length(); i++) {
                        JSONObject set = setsJSON.getJSONObject(i);
                        setsManager.addToPlaylist(set);
                        View artistTile = inflater.inflate(R.layout.artist_tile_recent, null);
                        HashMap<String, String> setMap = new HashMap<String, String>();
                        setMap.put("artist", set.getString("artist"));
                        Log.v("image", set.getString("artistimageURL"));
                        setMap.put("artistimageURL", set.getString("artistimageURL"));
//                setMap.put("tracklist", set.getString("tracklist"));
//                setMap.put("starttimes", set.getString("starttimes"));
                        setMap.put("popularity", set.getString("popularity"));
//                setMap.put("songURL", set.getString("songURL"));
                        setMapsList.add(setMap);
                        ((TextView) artistTile.findViewById(R.id.playCount)).setText(setMapsList.get(i).get("popularity") + " plays");
                        ((TextView) artistTile.findViewById(R.id.artistText)).setText(setMapsList.get(i).get("artist"));
                        if(!(setMapsList.get(i).get("artistimageURL").equals("null"))) {
                            setImageBackground(amazonS3Url + setMapsList.get(i).get("artistimageURL"), (ImageView) artistTile.findViewById(R.id.artistImage));
                        }
                        artistTile.setTag((String)set.getString("id"));
                        ((ViewGroup) lineupContainer).addView(artistTile);
                    }
                }
            }
            else {
                if(jsonResponse.getString("status").equals("success")) {
                    JSONArray lineupJSON = getLineupFromJson(jsonResponse);
                    LayoutInflater inflater = ((EventPagerActivity)lineupContainer.getContext()).getLayoutInflater();
                    for (int i = 0; i < lineupJSON.length(); i++) {
                        JSONObject set = lineupJSON.getJSONObject(i);
                        View artistTile = inflater.inflate(R.layout.artist_tile_upcoming, null);
                        HashMap<String, String> setMap = new HashMap<String, String>();
                        setMap.put("artist", set.getString("artist"));
                        setMap.put("artistimageURL", set.getString("imageURL"));
                        setMap.put("day", getDayFromDate(EVENT_DATE_UNFORMATTED, set.getInt("day")));
//                setMap.put("tracklist", set.getString("tracklist"));
//                setMap.put("starttimes", set.getString("starttimes"));
                        setMap.put("time", set.getString("time").substring(0, set.getString("time").length()-3));
//                setMap.put("songURL", set.getString("songURL"));
                        setMapsList.add(setMap);
                        ((TextView) artistTile.findViewById(R.id.setTime)).setText(setMapsList.get(i).get("day") + " " + setMapsList.get(i).get("time"));
                        ((TextView) artistTile.findViewById(R.id.artistText)).setText(setMapsList.get(i).get("artist"));
                        if(!(setMapsList.get(i).get("artistimageURL").equals("null"))) {
                            setImageBackground(amazonS3Url + setMapsList.get(i).get("artistimageURL"), (ImageView) artistTile.findViewById(R.id.artistImage));
                        }
                        ((ViewGroup) lineupContainer).addView(artistTile);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setImageBackground(String imageUrl, ImageView imageView) {
        if(imageCache.getBitmapFromMemCache(imageUrl) == null) {
            DownloadIconTask imageTask = new DownloadIconTask(context, imageCache, imageView, this);
            imageTask.execute(imageUrl);
        }
        else {
            Bitmap image = imageCache.getBitmapFromMemCache(imageUrl);
            onImageDownloaded(imageView, image);
        }
    }

    public void onImageDownloaded(ImageView imageView, Bitmap image) {
        imageView.setImageBitmap(image);
    }

    public JSONArray getSetsFromJson(JSONObject json) {
        JSONObject payload;
        JSONObject festival = null;
        JSONArray sets = null;
        try {
            payload = json.getJSONObject("payload");
            festival = payload.getJSONObject("festival");
            sets = festival.getJSONArray("sets");
            return sets;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public JSONArray getLineupFromJson(JSONObject json) {
        JSONObject payload;
        JSONArray lineup = null;
        try {
            payload = json.getJSONObject("payload");
            lineup = payload.getJSONArray("lineup");
            return lineup;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDayFromDate(String date, Integer day) {
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("E");
        Date startDate = stringToDate(date);
        String dayOfWeek = dayOfWeekFormat.format(startDate);
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DAY_OF_MONTH, day-1);
        return dayOfWeekFormat.format(c.getTime());

    }
    public Date stringToDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
        try {
            Date date = format.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
