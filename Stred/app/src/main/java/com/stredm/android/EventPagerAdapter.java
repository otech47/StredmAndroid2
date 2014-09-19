package com.stredm.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;


public class EventPagerAdapter extends FragmentPagerAdapter {

    public EventPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        EventPageFragment eventFragment = new EventPageFragment();
        Bundle args = new Bundle();
        args.putInt(EventPageFragment.ARG_OBJECT, i + 1);
        eventFragment.setArguments(args);
        return eventFragment;
    }

    @Override
    public int getCount() {
        return 3;
    }
}