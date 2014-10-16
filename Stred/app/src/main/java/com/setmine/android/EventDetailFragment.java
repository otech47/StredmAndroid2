package com.setmine.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.setmine.android.object.ImageViewChangeRequest;
import com.setmine.android.object.Lineup;
import com.setmine.android.object.Set;
import com.setmine.android.task.GetImageAsyncTask;
import com.setmine.android.task.LineupsSetsApiCallAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by oscarlafarga on 9/22/14.
 */
public class EventDetailFragment extends Fragment implements LineupsSetsApiCaller {

    public View rootView;
    private static final String amazonS3Url = "http://setmine.s3-website-us-east-1.amazonaws.com/namecheap/";
    public String EVENT_ID;
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
    public SetMineMainActivity activity;
    public List<View> currentTiles;

    @Override
    public void onLineupsSetsReceived(JSONObject jsonObject, String identifier) {
        Log.v("onLineupsSetsReceived", this.toString());
        if(identifier == "sets") {
            List<Set> setModels = new ArrayList<Set>();
            try {
                if(jsonObject.getString("status").equals("success")) {
                    JSONObject payload = jsonObject.getJSONObject("payload");
                    JSONObject festival = null;
                    festival = payload.getJSONObject("festival");
                    JSONArray sets = festival.getJSONArray("sets");
                    for(int i = 0 ; i < sets.length() ; i++) {
                        setModels.add(new Set(sets.getJSONObject(i)));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            currentTiles = activity.tileGen.modelsToSetTiles(setModels);
            lineupContainer.findViewById(R.id.loading).setVisibility(View.GONE);
            for(View v : currentTiles) {
                ((ViewGroup)lineupContainer).addView(v);
            }
        }
        if(identifier == "lineups") {
            Log.v("Lineup received ", identifier);
            activity.modelsCP.setModel(jsonObject, "lineups");
            currentTiles = activity.tileGen.modelsToLineupTiles(activity.modelsCP.getLastLineup().getLineup());
            lineupContainer.findViewById(R.id.loading).setVisibility(View.GONE);
            for(View v : currentTiles) {
                ((ViewGroup)lineupContainer).addView(v);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Query content provider for lineups or sets
//        If null, check async task status
//        If started, cancel all others, continue with task
//        If not started, launch task
//        Do this logic within Content Provider

        setMapsList = new ArrayList<HashMap<String, String>>();
        imageCache = ((SetMineMainActivity)getActivity()).imageCache;
        setsManager = ((SetMineMainActivity)getActivity()).setsManager;
        context = getActivity().getApplicationContext();
        activity = (SetMineMainActivity)getActivity();
        Log.v("EVENT TYPE", EVENT_TYPE.toString());
        if(EVENT_TYPE.equals("recent")) {
            new LineupsSetsApiCallAsyncTask(activity, context, activity.API_ROOT_URL, this)
                    .execute("festival?search=" + Uri.encode(EVENT_NAME), "sets");
        }
        else if(EVENT_TYPE.equals("upcoming")) {
            Lineup selectedLineup = null;
            if(activity.modelsCP.lineups.size() > 0) {
                for(int i = 0 ; i < activity.modelsCP.lineups.size() ; i++) {
                    if(activity.modelsCP.lineups.get(i).getEvent().equals(this.EVENT_NAME)) {
                        selectedLineup = (activity.modelsCP.lineups.get(i));
                        currentTiles = activity.tileGen.modelsToLineupTiles(selectedLineup.getLineup());
                        break;
                    }
                }
                if(selectedLineup == null) {
                    new LineupsSetsApiCallAsyncTask(activity, context, activity.API_ROOT_URL, this)
                            .execute("lineup/" + Uri.encode(EVENT_ID), "lineups");
                }
            }
            else {
                new LineupsSetsApiCallAsyncTask(activity, context, activity.API_ROOT_URL, this)
                        .execute("lineup/" + Uri.encode(EVENT_ID), "lineups");
            }
        }
        else {
            Log.v("Detail Fragment has no type", " ");
        }
        Log.v("Detail Fragment Created", "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.event_detail, container, false);
        ImageView eventImage = (ImageView)rootView.findViewById(R.id.eventImage);
        GetImageAsyncTask landingImageTask = new GetImageAsyncTask((SetMineMainActivity)getActivity(), imageCache, activity.PUBLIC_ROOT_URL + "images/");
        landingImageTask.execute(new ImageViewChangeRequest(EVENT_IMAGE, eventImage));
        ((TextView)rootView.findViewById(R.id.eventText)).setText(EVENT_NAME);
        if(EVENT_TYPE == "recent") {
            ((TextView)rootView.findViewById(R.id.eventText)).setBackgroundResource(R.color.setmine_blue);
            ((TextView)rootView.findViewById(R.id.lineupText)).setText("Sets");
        }
        else {
            ((TextView)rootView.findViewById(R.id.eventText)).setBackgroundResource(R.color.setmine_purple);
            if(EVENT_NAME.equals("Electro Chemical Show")) {
                Button buyTickets = (Button)rootView.findViewById(R.id.button_buy_tickets);
                buyTickets.setVisibility(View.VISIBLE);
                buyTickets.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            JSONObject mixpanelProperties = new JSONObject();
                            mixpanelProperties.put("id", EVENT_ID);
                            mixpanelProperties.put("event", EVENT_NAME);
                            activity.mixpanel.track("Ticket Link Clicked", mixpanelProperties);
                            Log.v("Ticket Link Click Tracked", mixpanelProperties.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Uri ticketUrl = Uri.parse("https://www.eventbrite.com/e/electro-chemical-show-tickets-12742221327");
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, ticketUrl);
                        startActivity(launchBrowser);
                    }
                });
            }
        }
        ((TextView)rootView.findViewById(R.id.dateText)).setText(EVENT_DATE);
        ((TextView)rootView.findViewById(R.id.locationText)).setText(EVENT_CITY);
        lineupContainer = rootView.findViewById(R.id.lineupContainer);
        if(currentTiles != null) {
            lineupContainer.findViewById(R.id.loading).setVisibility(View.GONE);
            for(View v : currentTiles) {
                ((ViewGroup)lineupContainer).addView(v);
            }
        }
        try {
            JSONObject mixpanelProperties = new JSONObject();
            mixpanelProperties.put("id", this.EVENT_ID);
            mixpanelProperties.put("event", this.EVENT_NAME);
            activity.mixpanel.track("Event Click Through", mixpanelProperties);
            Log.v("Event Click Through Tracked", mixpanelProperties.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("Detail Fragment View created", rootView.toString());
        return rootView;
    }

    @Override
    public void onDestroyView() {
        rootView = getView();
        super.onDestroyView();
    }

//    public void onApiResponse(JSONObject jsonResponse) {
//        savedApiResponse = jsonResponse;
//        GetImageAsyncTask getArtistImagesTask = new GetImageAsyncTask((SetMineMainActivity)getActivity(), imageCache, amazonS3Url);
//        try {
//            if(EVENT_TYPE.equals("recent")) {
//                if(jsonResponse.getString("status").equals("success")) {
//                    JSONArray setsJSON = getSetsFromJson(jsonResponse);
//                    LayoutInflater inflater = ((SetMineMainActivity)lineupContainer.getContext()).getLayoutInflater();
//                    setsManager.clearPlaylist();
//                    for (int i = 0; i < setsJSON.length(); i++) {
//                        JSONObject set = setsJSON.getJSONObject(i);
//                        setsManager.addToPlaylist(set);
//                        View artistTile = inflater.inflate(R.layout.artist_tile_recent, null);
//                        HashMap<String, String> setMap = new HashMap<String, String>();
//                        setMap.put("artist", set.getString("artist"));
//                        Log.v("image", set.getString("artistimageURL"));
//                        setMap.put("artistimageURL", set.getString("artistimageURL"));
////                setMap.put("tracklist", set.getString("tracklist"));
////                setMap.put("starttimes", set.getString("starttimes"));
//                        setMap.put("popularity", set.getString("popularity"));
////                setMap.put("songURL", set.getString("songURL"));
//                        setMapsList.add(setMap);
//                        ((TextView) artistTile.findViewById(R.id.playCount)).setText(setMapsList.get(i).get("popularity") + " plays");
//                        ((TextView) artistTile.findViewById(R.id.artistText)).setText(setMapsList.get(i).get("artist"));
//                        if(!(setMapsList.get(i).get("artistimageURL").equals("null"))) {
//                            getArtistImagesTask.execute(new ImageViewChangeRequest(setMapsList.get(i).get("artistimageURL"), (ImageView) artistTile.findViewById(R.id.artistImage)));
//                        }
//                        artistTile.setTag((String)set.getString("id"));
//                        ((ViewGroup) lineupContainer).addView(artistTile);
//                    }
//                }
//            }
//            else {
//                if(jsonResponse.getString("status").equals("success")) {
//                    JSONArray lineupJSON = getLineupFromJson(jsonResponse);
//                    LayoutInflater inflater = ((SetMineMainActivity)lineupContainer.getContext()).getLayoutInflater();
//                    for (int i = 0; i < lineupJSON.length(); i++) {
//                        JSONObject set = lineupJSON.getJSONObject(i);
//                        View artistTile = inflater.inflate(R.layout.artist_tile_upcoming, null);
//                        HashMap<String, String> setMap = new HashMap<String, String>();
//                        setMap.put("artist", set.getString("artist"));
//                        setMap.put("artistimageURL", set.getString("imageURL"));
//                        setMap.put("day", getDayFromDate(EVENT_DATE_UNFORMATTED, set.getInt("day")));
////                setMap.put("tracklist", set.getString("tracklist"));
////                setMap.put("starttimes", set.getString("starttimes"));
//                        setMap.put("time", set.getString("time").substring(0, set.getString("time").length()-3));
////                setMap.put("songURL", set.getString("songURL"));
//                        setMapsList.add(setMap);
//                        ((TextView) artistTile.findViewById(R.id.setTime)).setText(setMapsList.get(i).get("day") + " " + setMapsList.get(i).get("time"));
//                        ((TextView) artistTile.findViewById(R.id.artistText)).setText(setMapsList.get(i).get("artist"));
//                        if(!(setMapsList.get(i).get("artistimageURL").equals("null"))) {
//                            getArtistImagesTask.execute(new ImageViewChangeRequest(setMapsList.get(i).get("artistimageURL"), (ImageView) artistTile.findViewById(R.id.artistImage)));
//                        }
//                        ((ViewGroup) lineupContainer).addView(artistTile);
//                    }
//                }
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

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
}
