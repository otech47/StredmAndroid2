package com.stredm.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;


public class EventPagerAdapter extends FragmentPagerAdapter {

    public List<EventPageFragment> eventPageFragments = new ArrayList<EventPageFragment>();
    public EventPageFragment currentFragment;
    public int currentFragmentPosition;
    public FragmentManager fm;
    public int detailFragmentPosition = -1;

    public EventPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int i) {
        EventPageFragment eventPageFragment = new EventPageFragment();
        Bundle args = new Bundle();
        args.putInt(EventPageFragment.ARG_OBJECT, i + 1);
        eventPageFragment.setArguments(args);
        return eventPageFragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

//    @Override
//    public void setPrimaryItem(ViewGroup container, int position, Object object) {
//        if(fm.findFragmentByTag("eventDetailFragment") == null && position != detailFragmentPosition) {
//            object = eventPageFragments.get(position);
//        }
//        else {
//            object = fm.findFragmentByTag("eventDetailFragment");
//        }
//        Log.v("object", object.toString());
//        super.setPrimaryItem(container, position, object);
//        currentFragment = eventPageFragments.get(position);
//        currentFragmentPosition = position;
//    }
//
//    @Override
//    public Object instantiateItem(ViewGroup container, int position) {
//        if(fm.findFragmentByTag("eventDetailFragment") == null && position != detailFragmentPosition) {
//            EventPageFragment object = eventPageFragments.get(position);
//            Log.v("NULL?", object.getActivity().toString());
//            container.addView(object.onCreateView(object.getActivity().getLayoutInflater(), container, null));
//            finishUpdate((ViewGroup)container);
//            return object;
//        }
//        else {
//            EventDetailFragment object = (EventDetailFragment)fm.findFragmentByTag("eventDetailFragment");
//            Log.v("eventdetail", object.toString());
//            container.addView(object.onCreateView(object.getActivity().getLayoutInflater(), container, null));
//            finishUpdate((ViewGroup)container);
//            return object;
//        }
//    }
}