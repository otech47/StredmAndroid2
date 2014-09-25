package com.stredm.android;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.stredm.android.task.ApiResponseCache;
import com.stredm.android.task.ImageCache;
import com.stredm.android.task.TileGenerator;
import com.stredm.android.task.ViewPagerContainerFragment;

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
    public SetsManager setsManager;
    public ImageCache imageCache;
    public PlayerFragment playerFragment;
    public View playerFrame;
    public ViewPagerContainerFragment viewPagerContainerFragment;
    public View lastClickedPlayButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelsCP = new ModelsContentProvider();
        calculateScreenSize();
        setContentView(R.layout.fragment_main);
        playerFrame = findViewById(R.id.player_frame);
        imageCache = new ImageCache();
        setsManager = new SetsManager();
        fragmentManager = getSupportFragmentManager();
        playerFragment = new PlayerFragment();
        viewPagerContainerFragment = new ViewPagerContainerFragment();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.player_frame, playerFragment);
        ft.add(R.id.eventPagerContainer, viewPagerContainerFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
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
        Log.v("FRAGMENT MANAGER", fragmentManager.getFragments().toString());
        fragmentManager.popBackStack();
        Log.v("FRAGMENT MANAGER after pop", fragmentManager.getFragments().toString());

    }

    public void startPlayerFragment(View v) {
        if(lastClickedPlayButton != null) {
            ((ImageView)lastClickedPlayButton).setImageResource(R.drawable.ic_action_play);
            lastClickedPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPlayerFragment(v);
                }
            });
        }
        if(playerFragment == null) {
            playerFragment = new PlayerFragment();
        }
        playerFragment.externalPlayControl = (ImageView)v;
        ((ImageView) v).setImageResource(R.drawable.ic_action_pause);
        playerFragment.setPlayListeners();
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        transaction.hide(viewPagerContainerFragment);
//        transaction.hide(fragmentManager.findFragmentByTag("eventDetailFragment"));
//        transaction.show(playerFragment);
//        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//        transaction.commit();
        setsManager.selectSetById((String) ((View) v.getParent()).getTag());
        playerFragment.playSong(setsManager.selectedSetIndex);
        lastClickedPlayButton = v;
    }

    public void openPlayer() {
        playerFrame.animate().translationY(0);
        Log.v("Open ", "player");
    }

    public void closePlayer() {
        Log.v("Close ", "player");
    }

}
