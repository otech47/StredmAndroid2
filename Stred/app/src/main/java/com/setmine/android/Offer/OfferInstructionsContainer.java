package com.setmine.android;

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

import com.setmine.android.Offer.OfferPagerAdapter;
import com.setmine.android.user.User;
import com.viewpagerindicator.CirclePageIndicator;

/**
 * Created by oscarlafarga on 9/25/14.
 */
public class OfferInstructionsContainer extends Fragment {

    private static final String TAG = "OfferInstructionsConta";

    public ViewPager mViewPager;
    public OfferPagerAdapter mOfferPagerAdapter;
    public FragmentManager fragmentManager;
    private User user;

    public OfferInstructionsContainer() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
        user = ((SetMineMainActivity)getActivity()).user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");

        View root = inflater.inflate(R.layout.offer_instructions_pager, container, false);

        mViewPager = (ViewPager)root.findViewById(R.id.offer_instructions_pager);

        // Child Fragment Managers are required when dealing with View Pagers

        fragmentManager = getChildFragmentManager();
        mOfferPagerAdapter = new OfferPagerAdapter(fragmentManager);

        mViewPager.setAdapter(mOfferPagerAdapter);

        // Set the circle tabs at the bottom of the View Pager

        CirclePageIndicator circlePageIndicator = (CirclePageIndicator)root.findViewById(R.id.offer_instructions_title_tab);
        circlePageIndicator.setSnap(true);
        circlePageIndicator.setViewPager(mViewPager);

        // Makes sure all offscreen pages of the pager are loaded right away

        mViewPager.setOffscreenPageLimit(2);

        int pageToScrollTo = getArguments().getInt("page");

        if(savedInstanceState != null) {
            Log.d(TAG, "savedInstanceState");
            int lastPosition = savedInstanceState.getInt("lastPosition");
            mViewPager.setCurrentItem(lastPosition);
        } else {
            changePage(pageToScrollTo);
        }


        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("lastPosition", mViewPager.getCurrentItem());
    }


    public void changePage(int pageToScrollTo) {
        if(pageToScrollTo == -1) {
            mViewPager.setCurrentItem(0);
        } else {
            mViewPager.setCurrentItem(pageToScrollTo);
        }
    }
}
