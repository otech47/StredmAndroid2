package com.stredm.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Intent loadEventsPager = new Intent(this, EventPagerActivity.class);
            startActivity(loadEventsPager);
        }
    }

}
