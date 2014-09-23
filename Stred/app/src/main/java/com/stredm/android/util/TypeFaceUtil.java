package com.stredm.android.util;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by oscarlafarga on 9/20/14.
 */
public class TypeFaceUtil extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault("fonts/Roboto-Regular.ttf");
    }
}
