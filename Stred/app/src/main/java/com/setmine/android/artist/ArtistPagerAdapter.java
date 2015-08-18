package com.setmine.android.artist;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.setmine.android.set.DetailSetsFragment;
import com.setmine.android.event.DetailUpcomingEventsFragment;
import com.setmine.android.artist.Artist;

/**
 * Created by oscarlafarga on 11/20/14.
 */
public class ArtistPagerAdapter extends FragmentPagerAdapter {

    public FragmentManager fm;
    public Fragment currentFragment;
    public int currentPosition;
    public Artist currentArtist;
    public DetailUpcomingEventsFragment DUEFragment;
    public DetailSetsFragment DSFragment;
    public final String[] TITLES = new String[]{
            "Sets",
            "Events",
    };

    public final int NUM_TITLES = TITLES.length;

    public ArtistPagerAdapter(FragmentManager fm, Artist artist) {
        super(fm);
        this.fm = fm;
        currentArtist = artist;
    }


    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            DSFragment = new DetailSetsFragment();
            DSFragment.selectedArtist = currentArtist;
            return DSFragment;
        } else {
            DUEFragment = new DetailUpcomingEventsFragment();
            DUEFragment.selectedArtist = currentArtist;
            return DUEFragment;
        }
    }

    @Override
    public int getCount() {
        return NUM_TITLES;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        currentFragment = (Fragment)object;
        currentPosition = position;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position % NUM_TITLES];
    }
}
