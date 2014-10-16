package com.setmine.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by oscarlafarga on 9/25/14.
 */
public class ViewPagerContainerFragment extends Fragment {

    public ViewPager mViewPager;
    public EventPagerAdapter mEventPagerAdapter;
    public FragmentManager fragmentManager;


    public ViewPagerContainerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v("ViewPager onCreateView", container.toString());
        View root = inflater.inflate(R.layout.event_pager_container, container, false);
        fragmentManager = getChildFragmentManager();
        mViewPager = (ViewPager)root.findViewById(R.id.eventpager);
        mEventPagerAdapter = new EventPagerAdapter(fragmentManager);
        mViewPager.setAdapter(mEventPagerAdapter);
        ((SetMineMainActivity) getActivity()).eventViewPager = mViewPager;
        mViewPager.setOffscreenPageLimit(3);
        return root;
    }
}
