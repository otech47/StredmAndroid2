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
import com.setmine.android.adapter.MainPagerAdapter;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * Created by oscarlafarga on 9/25/14.
 */
public class MainViewPagerContainerFragment extends Fragment {

    public ViewPager mViewPager;
    public MainPagerAdapter mMainPagerAdapter;
    public FragmentManager fragmentManager;


    public MainViewPagerContainerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.event_pager_container, container, false);

        // Child Fragment Managers are required when dealing with View Pagers

        fragmentManager = getChildFragmentManager();

        mViewPager = (ViewPager)root.findViewById(R.id.eventpager);
        mMainPagerAdapter = new MainPagerAdapter(fragmentManager);

        // Store a reference to the Pager Adapter in the top level activity

        mMainPagerAdapter.activity = (SetMineMainActivity) getActivity();

        mViewPager.setAdapter(mMainPagerAdapter);

        // Set the title tabs at the top of the View Pager

        final TitlePageIndicator titlePageIndicator = (TitlePageIndicator)root.findViewById(R.id.titleTabs);
        titlePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int i, float v, int i2) {}
            public void onPageScrollStateChanged(int i) {}

            // Change the footer color of the title tabs

            @Override
            public void onPageSelected(int i) {
                if(i == 1) {
                    titlePageIndicator.setFooterColor(getResources().getColor(R.color.setmine_purple));
                } else if(i == 2) {
                    titlePageIndicator.setFooterColor(getResources().getColor(R.color.setmine_blue));
                } else {
                    titlePageIndicator.setFooterColor(getResources().getColor(R.color.setmine_gray));
                }

            }
        });

        // Bind title tabs to View Pager

        titlePageIndicator.setViewPager(mViewPager);

        // Store a reference to the View Pager in the top level activity

        ((SetMineMainActivity) getActivity()).eventViewPager = mViewPager;

        // Makes sure all offscreen pages of the pager are loaded right away

        mViewPager.setOffscreenPageLimit(3);

        mViewPager.setCurrentItem(1);

        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Show the action bar after View has been created

        ((SetMineMainActivity)getActivity()).actionBar.getCustomView().setVisibility(View.VISIBLE);
    }
}
