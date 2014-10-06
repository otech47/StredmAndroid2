package com.stredm.android;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stredm.android.task.EventApiCallTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventPageFragment extends Fragment {

    public static final String ARG_OBJECT = "page";
    public ApiResponse res = null;
    public Context context;
    public View rootView;
    public Integer page;
    public ImageCache imageCache;
    public ModelsContentProvider modelsCP;
    public TileGenerator tileGen;
    public ViewPager eventViewPager;
    public List<View> currentTiles;

    public EventPageFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle args = getArguments();
        page = args.getInt(ARG_OBJECT);
        if(page == 1)
            currentTiles = ((SetMineMainActivity)getActivity()).preloadedTiles.get("upcoming");
        else if(page == 2)
            currentTiles = ((SetMineMainActivity)getActivity()).preloadedTiles.get("featured");
        else if(page == 3)
            currentTiles = ((SetMineMainActivity)getActivity()).preloadedTiles.get("search");
        else
            currentTiles = null;
        Log.v("EPF Attached "+page.toString(), getActivity().toString());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetMineMainActivity rootActivity = (SetMineMainActivity)getActivity();
        this.tileGen = rootActivity.tileGen;
        this.imageCache = rootActivity.imageCache;
        this.context = rootActivity.getApplicationContext();
        this.modelsCP = rootActivity.modelsCP;
        this.eventViewPager = rootActivity.eventViewPager;
        Log.v("EPF Created "+page.toString(), getActivity().toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.v("page is ", page.toString());
        if(page == 1) {
            Log.v("page 1", " inflating");
            rootView = inflater.inflate(R.layout.events_scroll_view, container, false);
            for(View tile : currentTiles) {
                if(tile.getParent() == null) {
                    Log.v("currentTile parent is null", tile.toString());
                    ((ViewGroup)rootView.findViewById(R.id.eventsList)).addView(tile);
                }
                else {
                    Log.v("Tile already has a parent", tile.toString());
                    ((ViewGroup)tile.getParent()).removeView(tile);
                    ((ViewGroup)rootView.findViewById(R.id.eventsList)).addView(tile);
                }
            }
        }
        else if(page == 2) {
            Log.v("page 2", " inflating");
            rootView = inflater.inflate(R.layout.events_scroll_view_recent, container, false);
            for(View tile : currentTiles) {
                if(tile.getParent() == null) {
                    Log.v("currentTile parent is null", tile.toString());
                    ((ViewGroup)rootView.findViewById(R.id.eventsListRecent)).addView(tile);
                }
                else {
                    Log.v("Tile already has a parent", tile.toString());
                    ((ViewGroup)tile.getParent()).removeView(tile);
                    ((ViewGroup)rootView.findViewById(R.id.eventsListRecent)).addView(tile);
                }
            }
        }
        else if(page == 3) {
            Log.v("page 3", " inflating");
            rootView = inflater.inflate(R.layout.events_finder, container, false);
            rootView.findViewById(R.id.locationText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(!hasFocus) {
                        eventSearch(v);
                    }
                }
            });
            rootView.findViewById(R.id.dateText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(!hasFocus) {
                        eventSearch(v);
                    }
                }
            });
            for(View tile : currentTiles) {
                if(tile.getParent() == null) {
                    Log.v("currentTile parent is null", tile.toString());
                    ((ViewGroup)rootView.findViewById(R.id.searchResults)).addView(tile);
                }
                else {
                    Log.v("Tile already has a parent", tile.toString());
                    ((ViewGroup)tile.getParent()).removeView(tile);
                    ((ViewGroup)rootView.findViewById(R.id.searchResults)).addView(tile);
                }
            }
        }
        Log.v("EPF View Created ", page.toString());
        Log.v("rootview is ", rootView.toString());
        return rootView;
    }

    public void eventSearch(View v) {
        String location = ((TextView)((ViewGroup)v.getParent().getParent()).findViewById(R.id.locationText)).getText().toString();
        Log.v("location", location);
        String latitude = "33";
        String longitude = "-84";
        String date = ((TextView)((ViewGroup)v.getParent().getParent()).findViewById(R.id.dateText)).getText().toString();
        EventApiCallTask eventSearchTask = new EventApiCallTask(context, this);
        String route = "upcoming/?date=" + Uri.encode(date) + "&latitude=" + latitude + "&longitude=" + longitude;
        eventSearchTask.execute(route);
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

    public String getDayFromDate(String date, Integer day) {
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("E");
        Date startDate = stringToDate(date);
        String dayOfWeek = dayOfWeekFormat.format(startDate);
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DAY_OF_MONTH, day - 1);
        return dayOfWeekFormat.format(c.getTime());

    }

}
