package com.setmine.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class EventPagerAdapter extends FragmentPagerAdapter {

    public List<EventPageFragment> eventPageFragments = new ArrayList<EventPageFragment>();
    public EventPageFragment currentFragment;
    public int currentFragmentPosition;
    public FragmentManager fm;

    public EventPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int i) {
        EventPageFragment eventPageFragment = new EventPageFragment();
        Bundle args = new Bundle();
        args.putInt(EventPageFragment.ARG_OBJECT, i + 1);
        Log.v("args", args.toString());
        eventPageFragment.setArguments(args);
        return eventPageFragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        currentFragmentPosition = position;
        currentFragment = (EventPageFragment)object;
    }

}