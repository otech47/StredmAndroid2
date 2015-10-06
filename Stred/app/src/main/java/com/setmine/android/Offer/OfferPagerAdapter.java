package com.setmine.android.Offer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.setmine.android.SetMineMainActivity;

/**
 * Created by oscarlafarga on 7/3/15.
 */
public class OfferPagerAdapter extends FragmentStatePagerAdapter {

    public Fragment currentFragment;
    public int currentFragmentPosition;
    public FragmentManager fm;
    public SetMineMainActivity activity;
    public String[] TITLES = new String[] {
            "Navigate",
            "Enter",
            "Enjoy"
    };

    public int NUM_TITLES = TITLES.length;

    public OfferPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }

    // The View Pager calls this method when loading the pages in the pager

    @Override
    public Fragment getItem(int i) {

        // All pages are OfferInstructionsFragments

        OfferInstructionsFragment offerInstructionsFragment = new OfferInstructionsFragment();
        Bundle args = new Bundle();
        args.putInt(OfferInstructionsFragment.ARG_OBJECT, i);
        offerInstructionsFragment.setArguments(args);
        return offerInstructionsFragment;

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