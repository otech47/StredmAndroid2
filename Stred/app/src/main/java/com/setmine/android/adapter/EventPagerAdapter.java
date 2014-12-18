package com.setmine.android.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.setmine.android.SetMineMainActivity;
import com.setmine.android.fragment.EventPageFragment;

//        Incomplete User Login Feature code is commented out

public class EventPagerAdapter extends FragmentPagerAdapter {

    public Fragment currentFragment;
    public int currentFragmentPosition;
    public FragmentManager fm;
    public SetMineMainActivity activity;
    public String[] TITLES = new String[] {
            "Events",
            "Sets",
            "Find"
    };
//    public String[] TITLES = new String[] {
//            "Login",
//            "Events",
//            "Sets",
//            "Find"
//    };

    public int NUM_TITLES = TITLES.length;

    public EventPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int i) {

//        if(i == 0) {
//            UserFragment userFragment = new UserFragment();
//            Bundle args = new Bundle();
//            args.putInt(UserFragment.ARG_OBJECT, 1);
//            userFragment.setArguments(args);
//            return userFragment;
//        } else {
            EventPageFragment eventPageFragment = new EventPageFragment();
            Bundle args = new Bundle();
            args.putInt(EventPageFragment.ARG_OBJECT, i + 2);
            eventPageFragment.setArguments(args);
            return eventPageFragment;
//        }

    }

    @Override
    public int getCount() {
        return NUM_TITLES;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        currentFragmentPosition = position;
        currentFragment = (Fragment)object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position % NUM_TITLES];
    }
}