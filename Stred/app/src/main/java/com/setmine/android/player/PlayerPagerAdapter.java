package com.setmine.android.player;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.setmine.android.event.EventPageFragment;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.player.PlayerFragment;
import com.setmine.android.player.PlaylistFragment;
import com.setmine.android.player.TracklistFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfonte on 10/16/2014.
 */
public class PlayerPagerAdapter extends FragmentStatePagerAdapter{

    private static final String TAG = "PlayerPagerAdapter";

    public PlayerFragment playerFragment;
    public TracklistFragment tracklistFragment;
//    public PlaylistFragment playListFragment;
    public SparseArray<Fragment> playerContainedFragments;
    public FragmentManager fm;
    public Context context;
    public final String[] TITLES = new String[] {
            "Player",
            "Tracklist"
    };
    public final int NUM_TITLES = TITLES.length;


    public PlayerPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
        playerContainedFragments = new SparseArray<Fragment>();
    }

    public PlayerPagerAdapter(FragmentManager fm, Context c) {
        this(fm);
        context = c;
    }
    @Override
    public Fragment getItem(int i) {
        Log.d(TAG, "getItem: "+ Integer.toString(i));
        if(i == 0) {
            playerFragment = new PlayerFragment();
            return playerFragment;
        } else {
            tracklistFragment = new TracklistFragment();
            Bundle args = new Bundle();
            tracklistFragment.setArguments(args);
            return tracklistFragment;
        }
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        super.finishUpdate(container);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position % NUM_TITLES];
    }



}