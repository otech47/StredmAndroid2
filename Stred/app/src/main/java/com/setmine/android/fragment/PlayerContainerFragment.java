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
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.adapter.EventPagerAdapter;
import com.setmine.android.adapter.PlayerPagerAdapter;

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
        Log.v("PlayerPager onCreateView", container.toString());
        View root = inflater.inflate(R.layout.player_pager_container, container, false);
        fragmentManager = getChildFragmentManager();
        mViewPager = (ViewPager)root.findViewById(R.id.playerpager);
        mPlayerPagerAdapter = new PlayerPagerAdapter(fragmentManager, getActivity());
        mViewPager.setAdapter(mPlayerPagerAdapter);
        ((SetMineMainActivity) getActivity()).mPlayerPagerAdapter = mPlayerPagerAdapter;
        mViewPager.setOffscreenPageLimit(2);
        return root;
    }
}
