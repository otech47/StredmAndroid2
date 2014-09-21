package com.stredm.android;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

import com.stredm.android.task.ApiResponseCache;
import com.stredm.android.task.TileGenerator;

public class EventPagerActivity extends FragmentActivity {

    public EventPagerAdapter mEventPagerAdapter;
    public ViewPager eventViewPager;
    public ApiResponseCache cache;
    public ModelsContentProvider modelsCP;
    public TileGenerator tileGen;
    public Integer screenHeight;
    public Integer screenWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelsCP = new ModelsContentProvider();
        calculateScreenSize();
        setContentView(R.layout.fragment_main);
        mEventPagerAdapter = new EventPagerAdapter(getSupportFragmentManager());
        eventViewPager = (ViewPager) findViewById(R.id.eventpager);
        tileGen = new TileGenerator(getApplicationContext(), eventViewPager);
        eventViewPager.setAdapter(mEventPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        return true;
    }

    public void calculateScreenSize() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        screenHeight = size.y;
        screenWidth = size.x;
        Log.v("Height", screenHeight.toString());
        Log.v("Width", screenWidth.toString());
    }

}
