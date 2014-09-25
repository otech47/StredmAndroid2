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
import com.stredm.android.task.ImageCache;
import com.stredm.android.task.TileGenerator;

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

    public EventPageFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventPagerActivity rootActivity = (EventPagerActivity)getActivity();
        this.tileGen = rootActivity.tileGen;
        this.imageCache = rootActivity.imageCache;
        this.context = rootActivity.getApplicationContext();
        this.modelsCP = rootActivity.modelsCP;
        this.eventViewPager = rootActivity.eventViewPager;
        Bundle args = getArguments();
        page = args.getInt(ARG_OBJECT);
        if(res == null) {
            String apiRoute = "upcoming";
            if(page == 2)
                apiRoute = "featured";
            EventApiCallTask apiCall = new EventApiCallTask(context, this);
            apiCall.execute(apiRoute);
        }
        Log.v("EPF Created "+page.toString(), getActivity().toString());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.v("EPV Created ", page.toString());
        if(rootView != null) {
            Log.v("rootview "+page+" is ", rootView.toString());
            Log.v("rootview "+page+" has tiles ", rootView.toString());
        }
        else {
            Log.v("rootview is null", " ");
        }
        if(page == 1) {
            rootView = inflater.inflate(R.layout.events_scroll_view, container, false);
        }
        else if(page == 2) {
            rootView = inflater.inflate(R.layout.events_scroll_view_recent, container, false);
        }
        else if(page == 3) {
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
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.v("EPF after view created", " ");
        if(res != null && rootView != null) {
            Log.v("Regenerating from res ", res.toString());
            if(page == 1) {
                modelsCP.setModel(((Payload<UpcomingModel>) res.payload).model.closestEvents, "upcomingEvents");
                tileGen.eventPageFragment = this;
                tileGen.modelsToUpcomingEventTiles(modelsCP.upcomingEvents, rootView);
            }
            else if(page == 2) {
                modelsCP.setModel(((Payload<List<Model>>)res.payload).model, "recentEvents");
                tileGen.eventPageFragment = this;
                tileGen.modelsToRecentEventTiles(modelsCP.recentEvents, rootView);
            }
            else if(page == 3) {
                modelsCP.setModel(((Payload<UpcomingModel>) res.payload).model.closestEvents, "searchEvents");
                tileGen.eventPageFragment = this;
                tileGen.modelsToEventSearchTiles(modelsCP.searchEvents, rootView);
            }
        }
        else {
            Log.v("res is null", " . ");
        }
        super.onViewCreated(view, savedInstanceState);
    }

    public void onApiResponse(ApiResponse apiResponse) {
        Log.v("api response received", page.toString());
        if(page == 1) {
            res = apiResponse;
            modelsCP.setModel(((Payload<UpcomingModel>) apiResponse.payload).model.closestEvents, "upcomingEvents");
            tileGen.eventPageFragment = this;
            tileGen.modelsToUpcomingEventTiles(modelsCP.upcomingEvents, rootView);
        }
        else if(page == 2) {
            res = apiResponse;
            modelsCP.setModel(((Payload<List<Model>>)apiResponse.payload).model, "recentEvents");
            tileGen.eventPageFragment = this;
            tileGen.modelsToRecentEventTiles(modelsCP.recentEvents, rootView);
        }
        else if(page == 3) {
            res = apiResponse;
            modelsCP.setModel(((Payload<UpcomingModel>) apiResponse.payload).model.closestEvents, "searchEvents");
            tileGen.eventPageFragment = this;
            tileGen.modelsToEventSearchTiles(modelsCP.searchEvents, rootView);
        }
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

}
