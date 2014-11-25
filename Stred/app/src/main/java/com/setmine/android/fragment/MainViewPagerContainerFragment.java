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
import com.setmine.android.adapter.EventPagerAdapter;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * Created by oscarlafarga on 9/25/14.
 */
public class MainViewPagerContainerFragment extends Fragment {

    public ViewPager mViewPager;
    public EventPagerAdapter mEventPagerAdapter;
    public FragmentManager fragmentManager;


    public MainViewPagerContainerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.event_pager_container, container, false);
        fragmentManager = getChildFragmentManager();
        mViewPager = (ViewPager)root.findViewById(R.id.eventpager);
        mEventPagerAdapter = new EventPagerAdapter(fragmentManager);
        mViewPager.setAdapter(mEventPagerAdapter);
        final TitlePageIndicator titlePageIndicator = (TitlePageIndicator)root.findViewById(R.id.titleTabs);
        titlePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int i, float v, int i2) {}
            public void onPageScrollStateChanged(int i) {}

            @Override
            public void onPageSelected(int i) {
                if(i == 0) {
                    titlePageIndicator.setFooterColor(getResources().getColor(R.color.setmine_purple));
                } else if(i == 1) {
                    titlePageIndicator.setFooterColor(getResources().getColor(R.color.setmine_blue));
                } else {
                    titlePageIndicator.setFooterColor(getResources().getColor(R.color.setmine_gray));
                }

            }
        });
        titlePageIndicator.setViewPager(mViewPager);
        ((SetMineMainActivity) getActivity()).eventViewPager = mViewPager;
        mViewPager.setOffscreenPageLimit(3);
        ((SetMineMainActivity)getActivity()).eventViewPager = mViewPager;
        ((SetMineMainActivity)getActivity()).actionBar.getCustomView().setVisibility(View.VISIBLE);
        return root;
    }
}
