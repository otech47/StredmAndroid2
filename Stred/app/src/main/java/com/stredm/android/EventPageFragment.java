package com.stredm.android;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stredm.android.task.LoadEventsTask;
import com.stredm.android.task.TileGenerator;

import java.util.List;

public class EventPageFragment extends Fragment {

    public static final String ARG_OBJECT = "page";
    public ApiResponse res = null;
    public Context context;
    public View rootView;
    public Integer page;
    public ModelsContentProvider modelsCP;
    public TileGenerator tileGen;
    public ViewPager eventViewPager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.tileGen = ((EventPagerActivity)getActivity()).tileGen;
        context = getActivity().getApplicationContext();
        this.modelsCP = ((EventPagerActivity)getActivity()).modelsCP;
        this.eventViewPager = ((EventPagerActivity)getActivity()).eventViewPager;
        Bundle args = getArguments();
        page = args.getInt(ARG_OBJECT);
        Log.v("LINE 33 ONCREATE", page.toString());
        if(savedInstanceState == null) {
            String apiRoute = "upcoming";
            if(page == 2)
                apiRoute = "featured";
            LoadEventsTask apiCall = new LoadEventsTask(context, this);
            apiCall.execute(apiRoute);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if(page == 1) {
            rootView = inflater.inflate(R.layout.events_scroll_view, container, false);
        }
        else if(page == 2) {
            rootView = inflater.inflate(R.layout.events_scroll_view, container, false);
        }
        else if(page == 3) {
            rootView = inflater.inflate(R.layout.events_finder, container, false);
        }
        return rootView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.v("restoring view state", page.toString());
        if(res != null) {
            if(page == 1) {
                Log.v("page 1 regenerate", modelsCP.upcomingEvents.toString());
                tileGen.eventPageFragment = this;
                tileGen.modelsToUpcomingEventTiles(modelsCP.upcomingEvents, rootView);
            }
            else if(page == 2) {
                Log.v("page 2 regenerate", modelsCP.recentEvents.toString());
                tileGen.eventPageFragment = this;
                tileGen.modelsToRecentEventTiles(modelsCP.recentEvents, rootView);
            }
            else if(page == 3) {
                Log.v("page 3 regenerate", modelsCP.recentEvents.toString());
                tileGen.eventPageFragment = this;
                tileGen.modelsToEventSearchTiles(modelsCP.upcomingEvents, rootView);
            }
        }
    }

    public void onApiResponse(ApiResponse apiResponse) {
        Log.v("api response received", page.toString());
        if(page == 1) {
            res = apiResponse;
            Log.v("page 1 generate", ((Payload<UpcomingModel>) apiResponse.payload).model.closestEvents.toString());
            modelsCP.setModel(((Payload<UpcomingModel>) apiResponse.payload).model.closestEvents, "upcomingEvents");
            tileGen.eventPageFragment = this;
            tileGen.modelsToUpcomingEventTiles(modelsCP.upcomingEvents, rootView);
        }
        else if(page == 2) {
            res = apiResponse;
            Log.v("page 2 generate", (((Payload<List<Model>>)apiResponse.payload).model.toString()));
            modelsCP.setModel(((Payload<List<Model>>)apiResponse.payload).model, "recentEvents");
            tileGen.modelsToRecentEventTiles(modelsCP.recentEvents, rootView);
        }
        else if(page == 3) {
            res = apiResponse;
            Log.v("page 3 generate", ((Payload<UpcomingModel>) apiResponse.payload).model.closestEvents.toString());
            modelsCP.setModel(((Payload<UpcomingModel>) apiResponse.payload).model.closestEvents, "upcomingEvents");
            tileGen.eventPageFragment = this;
            tileGen.modelsToEventSearchTiles(modelsCP.upcomingEvents, rootView);
        }
    }

}
