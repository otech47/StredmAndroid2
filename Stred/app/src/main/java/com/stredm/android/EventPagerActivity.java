package com.stredm.android;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.stredm.android.task.ApiResponseCache;
import com.stredm.android.task.TileGenerator;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class EventPagerActivity extends FragmentActivity {

    public EventPagerAdapter mEventPagerAdapter;
    public ViewPager eventViewPager;
    public ApiResponseCache cache;
    public ModelsContentProvider modelsCP;
    public TileGenerator tileGen;
    public Integer screenHeight;
    public Integer screenWidth;
    public FragmentManager fragmentManager;
    public Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelsCP = new ModelsContentProvider();
        calculateScreenSize();
        setContentView(R.layout.fragment_main);
        fragmentManager = getSupportFragmentManager();
        mEventPagerAdapter = new EventPagerAdapter(fragmentManager);
        eventViewPager = (ViewPager) findViewById(R.id.eventpager);
        tileGen = new TileGenerator(getApplicationContext(), eventViewPager);
        eventViewPager.setAdapter(mEventPagerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    public void backButtonPress(View v) {
        getSupportFragmentManager().popBackStack();
        v.findViewById(R.id.backButton).setVisibility(View.GONE);
    }

    public void eventSearch(View v) {
        String location = ((TextView)v.findViewById(R.id.locationText)).getText().toString();
        String date = ((TextView)v.findViewById(R.id.dateText)).getText().toString();
    }

}
