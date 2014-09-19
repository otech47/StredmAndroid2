package com.stredm.android;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.stredm.android.task.ApiResponseCache;

public class EventPagerActivity extends FragmentActivity {

    public EventPagerAdapter mEventPagerAdapter;
    public ViewPager eventViewPager;
    public ApiResponseCache cache;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        mEventPagerAdapter = new EventPagerAdapter(getSupportFragmentManager());
        eventViewPager = (ViewPager) findViewById(R.id.eventpager);
        eventViewPager.setAdapter(mEventPagerAdapter);
    }



}
