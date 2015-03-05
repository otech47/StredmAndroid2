package com.setmine.android.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.setmine.android.SetMineMainActivity;
import com.setmine.android.fragment.EventPageFragment;
import com.setmine.android.fragment.UserFragment;


public class MainPagerAdapter extends FragmentStatePagerAdapter {

    public Fragment currentFragment;
    public int currentFragmentPosition;
    public FragmentManager fm;
    public SetMineMainActivity activity;
    public String[] TITLES = new String[] {
            "Login",
            "Events",
            "Sets",
            "Explore"
    };

    public int NUM_TITLES = TITLES.length;

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }

    // The View Pager calls this method when loading the pages in the pager

    @Override
    public Fragment getItem(int i) {

        // First page is the UserFragment
        // Next three pages are EventPageFragments
        // See respective classes for documentation

        if(i == 0) {
            UserFragment userFragment = new UserFragment();
            Bundle args = new Bundle();
            args.putInt(UserFragment.ARG_OBJECT, 0);
            userFragment.setArguments(args);
            return userFragment;
        } else {
            EventPageFragment eventPageFragment = new EventPageFragment();
            Bundle args = new Bundle();
            args.putInt(EventPageFragment.ARG_OBJECT, i + 1);
            eventPageFragment.setArguments(args);
            return eventPageFragment;
        }

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