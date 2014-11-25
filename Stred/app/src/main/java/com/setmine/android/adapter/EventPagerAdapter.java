package com.setmine.android.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.setmine.android.fragment.EventPageFragment;

import java.util.ArrayList;
import java.util.List;


public class EventPagerAdapter extends FragmentPagerAdapter {

    public List<EventPageFragment> eventPageFragments = new ArrayList<EventPageFragment>();
    public EventPageFragment currentFragment;
    public int currentFragmentPosition;
    public FragmentManager fm;
    public final String[] TITLES = new String[] {
            "Featured",
            "Recent",
            "Find"
    };

    public final int NUM_TITLES = TITLES.length;

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
        return NUM_TITLES;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        currentFragmentPosition = position;
        currentFragment = (EventPageFragment)object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position % NUM_TITLES];
    }
}