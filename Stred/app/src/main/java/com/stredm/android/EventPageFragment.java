package com.stredm.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stredm.android.task.ApiCallTask;

public class EventPageFragment extends Fragment {

    public static final String ARG_OBJECT = "page";
    public ApiResponse res;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String featured = "featured";
        ApiCallTask artistCall = new ApiCallTask(getActivity().getApplicationContext(), this);
        artistCall.execute(featured);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        int page = args.getInt(ARG_OBJECT);
        View rootView = null;
        if(page == 3) {
            rootView = inflater.inflate(R.layout.events_finder, container, false);
        }
        else {
            rootView = inflater.inflate(R.layout.events_scroll_view, container, false);
        }
        return rootView;
    }

}
