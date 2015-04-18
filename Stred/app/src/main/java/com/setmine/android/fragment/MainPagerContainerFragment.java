package com.setmine.android.fragment;

import android.app.Activity;
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
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.adapter.MainPagerAdapter;
import com.setmine.android.object.User;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * Created by oscarlafarga on 9/25/14.
 */
public class MainPagerContainerFragment extends Fragment {

    private static final String TAG = "MainViewPagerFragment";

    public ViewPager mViewPager;
    public MainPagerAdapter mMainPagerAdapter;
    public FragmentManager fragmentManager;
    private User user;

    public int purpleColorID;
    public int blueColorID;
    public int grayColorID;



    public MainPagerContainerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        // Child Fragment Managers are required when dealing with View Pagers

        fragmentManager = getChildFragmentManager();

        mMainPagerAdapter = new MainPagerAdapter(fragmentManager);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
        purpleColorID = getResources().getColor(R.color.setmine_purple);
        blueColorID = getResources().getColor(R.color.setmine_blue);
        grayColorID = getResources().getColor(R.color.setmine_gray);
        user = ((SetMineMainActivity)getActivity()).user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");

        View root = inflater.inflate(R.layout.main_pager_container, container, false);

        mViewPager = (ViewPager)root.findViewById(R.id.eventpager);

        // Store a reference to the Pager Adapter in the top level activity

        mMainPagerAdapter.activity = (SetMineMainActivity) getActivity();

        mViewPager.setAdapter(mMainPagerAdapter);

        // Set the title tabs at the top of the View Pager

        final TitlePageIndicator titlePageIndicator = (TitlePageIndicator)root.findViewById(R.id.titleTabs);
        titlePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int i, float v, int i2) {
            }

            public void onPageScrollStateChanged(int i) {
            }

            // Change the footer color of the title tabs

            @Override
            public void onPageSelected(int i) {
                if (i == 1) {
                    titlePageIndicator.setFooterColor(purpleColorID);
                } else if (i == 2) {
                    titlePageIndicator.setFooterColor(blueColorID);
                } else {
                    titlePageIndicator.setFooterColor(grayColorID);
                }

            }
        });

        // Bind title tabs to View Pager

        titlePageIndicator.setViewPager(mViewPager);

        // Makes sure all offscreen pages of the pager are loaded right away

        mViewPager.setOffscreenPageLimit(3);

        int pageToScrollTo = getArguments().getInt("page");

        if(savedInstanceState != null) {
            Log.d(TAG, "savedInstanceState");
            int lastPosition = savedInstanceState.getInt("lastPosition");
            mViewPager.setCurrentItem(lastPosition);
        } else {
            if(pageToScrollTo == -1) {
                if(user.isRegistered()) {
                    mViewPager.setCurrentItem(0);
                } else {
                    mViewPager.setCurrentItem(2);
                }
            } else {
                mViewPager.setCurrentItem(pageToScrollTo);
            }
        }


        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Show the action bar after View has been created

        ((SetMineMainActivity)getActivity()).actionBar.getCustomView().setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("lastPosition", mViewPager.getCurrentItem());
    }
}
