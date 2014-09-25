package com.stredm.android.task;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stredm.android.EventPagerActivity;
import com.stredm.android.EventPagerAdapter;
import com.stredm.android.R;

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
        View root = inflater.inflate(R.layout.event_pager_container, container, false);
        fragmentManager = getChildFragmentManager();
        mViewPager = (ViewPager)root.findViewById(R.id.eventpager);
        mEventPagerAdapter = new EventPagerAdapter(fragmentManager);
        mViewPager.setAdapter(mEventPagerAdapter);
        ((EventPagerActivity)getActivity()).tileGen = new TileGenerator(getActivity().getApplicationContext(), mViewPager, ((EventPagerActivity) getActivity()).imageCache);
        return root;
    }
}
