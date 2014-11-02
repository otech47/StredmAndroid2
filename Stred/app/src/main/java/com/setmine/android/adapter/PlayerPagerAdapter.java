package com.setmine.android.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.setmine.android.fragment.EventPageFragment;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.fragment.PlayerFragment;
import com.setmine.android.fragment.PlaylistFragment;
import com.setmine.android.fragment.TracklistFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfonte on 10/16/2014.
 */
public class PlayerPagerAdapter extends FragmentPagerAdapter{

    public List<EventPageFragment> eventPageFragments = new ArrayList<EventPageFragment>();
    public PlayerFragment playerFragment;
    public TracklistFragment tracklistFragment;
    public PlaylistFragment playListFragment;
    public int currentFragmentPosition;
    public FragmentManager fm;
    public Context context;

    public PlayerPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }

    public PlayerPagerAdapter(FragmentManager fm, Context c) {
        this(fm);
        context = c;
    }
    @Override
    public Fragment getItem(int i) {
        if(i == 0) {
            Log.d("getItem", "playlist");
            playListFragment = new PlaylistFragment();
            ((SetMineMainActivity)context).playlistFragment = playListFragment;
            return playListFragment;
        } else if(i == 1) {
            playerFragment = new PlayerFragment();
            ((SetMineMainActivity)context).playerFragment = playerFragment;
            return playerFragment;
        } else {
            tracklistFragment = new TracklistFragment();
            ((SetMineMainActivity)context).tracklistFragment = tracklistFragment;
            Bundle args = new Bundle();
            args.putInt(TracklistFragment.SONG_ARG_OBJECT, playerFragment.getCurrentSongIndex());
            args.putBoolean(TracklistFragment.SHUFFLE_ARG_OBJECT, playerFragment.getIsShuffle());
            Log.v("args", args.toString());
            tracklistFragment.setArguments(args);
            return tracklistFragment;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

}