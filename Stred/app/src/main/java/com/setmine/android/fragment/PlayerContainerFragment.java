package com.setmine.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.setmine.android.R;
import com.setmine.android.adapter.PlayerPagerAdapter;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * Created by jfonte on 10/16/2014.
 */
public class PlayerContainerFragment extends Fragment {

    private static final String TAG = "PlayerContainerFragment";

    public ViewPager mViewPager;
    public PlayerPagerAdapter mPlayerPagerAdapter;
    public FragmentManager fragmentManager;
    public PlayerFragment playerFragment;
    public TracklistFragment tracklistFragment;


    public PlayerContainerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        fragmentManager = getChildFragmentManager();
        mPlayerPagerAdapter = new PlayerPagerAdapter(fragmentManager, getActivity());
        Log.d(TAG, "mPlayerPagerAdapter set");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.fragment_player_pager_container, container, false);
        mViewPager = (ViewPager)root.findViewById(R.id.playerpager);
        mViewPager.setAdapter(mPlayerPagerAdapter);
        final TitlePageIndicator titlePageIndicator = (TitlePageIndicator)root.findViewById(R.id.titleTabs);
        titlePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int i, float v, int i2) {
            }

            public void onPageScrollStateChanged(int i) {
            }

            @Override
            public void onPageSelected(int i) { }
        });
        titlePageIndicator.setViewPager(mViewPager);
        mViewPager.setOffscreenPageLimit(2);
        return root;
    }

    public void configureViewPager() {
        if(mPlayerPagerAdapter == null) {

        }
    }
}
