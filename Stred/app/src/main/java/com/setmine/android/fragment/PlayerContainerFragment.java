package com.setmine.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.adapter.PlayerPagerAdapter;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * Created by jfonte on 10/16/2014.
 */
public class PlayerContainerFragment extends Fragment {

    public ViewPager mViewPager;
    public PlayerPagerAdapter mPlayerPagerAdapter;
    public FragmentManager fragmentManager;


    public PlayerContainerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_player_pager_container, container, false);
        fragmentManager = getChildFragmentManager();
        mViewPager = (ViewPager)root.findViewById(R.id.playerpager);
        mPlayerPagerAdapter = new PlayerPagerAdapter(fragmentManager, getActivity());
        mViewPager.setAdapter(mPlayerPagerAdapter);
        final TitlePageIndicator titlePageIndicator = (TitlePageIndicator)root.findViewById(R.id.titleTabs);
        titlePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int i, float v, int i2) {}
            public void onPageScrollStateChanged(int i) {}

            @Override
            public void onPageSelected(int i) { }
        });
        titlePageIndicator.setViewPager(mViewPager);
        ((SetMineMainActivity) getActivity()).mPlayerPagerAdapter = mPlayerPagerAdapter;
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setCurrentItem(1);
        return root;


    }
}
