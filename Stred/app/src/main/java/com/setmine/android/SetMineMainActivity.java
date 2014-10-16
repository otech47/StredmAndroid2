package com.setmine.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.stredm.android.object.Event;
import com.stredm.android.task.InitialApiCallAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.ImageLoader;

public class SetMineMainActivity extends FragmentActivity implements InitialApiCaller, LineupsSetsApiCaller {

    public static final String MIXPANEL_TOKEN = "dfe92f3c1c49f37a7d8136a2eb1de219";
    public static final String APP_VERSION = "1.1.1";
    public static final String API_VERSION = "1";
    public static final String API_ROOT_URL = "http://setmine.com/api/v/" + API_VERSION + "/";
    public static final String PUBLIC_ROOT_URL = "http://setmine.com/";
    public static final String S3_ROOT_URL = "http://stredm.s3-website-us-east-1.amazonaws.com/namecheap/";

    public EventPagerAdapter mEventPagerAdapter;
    public ViewPager eventViewPager;
    public ModelsContentProvider modelsCP;
    public Integer screenHeight;
    public Integer screenWidth;
    public FragmentManager fragmentManager;
    public Menu menu;
    public SetsManager setsManager;
    public PlayerFragment playerFragment;
    public View playerFrame;
    public ViewPagerContainerFragment viewPagerContainerFragment;
    public View lastClickedPlayButton;
    public HashMap<String, List<View>> preloadedTiles = new HashMap<String, List<View>>();
    public InitialApiCallAsyncTask getLineupAsyncTask;
    public InitialApiCallAsyncTask asyncApiCaller;
    public MixpanelAPI mixpanel;
    public int asyncTasksInProgress;

    @Override
    public void onInitialResponseReceived(JSONObject jsonObject, String modelType) {
        this.modelsCP.setModel(jsonObject, modelType);
        if(modelsCP.upcomingEvents.size() > 1 && modelsCP.recentEvents.size() > 1 && modelsCP.searchEvents.size() > 1) {
            finishOnCreate();
        }
    }

    @Override
    public void onLineupsSetsReceived(JSONObject jsonObject, String identifier) {
        this.modelsCP.setModel(jsonObject, identifier);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);
        if(savedInstanceState == null) {
            JSONObject mixpanelProperties = new JSONObject();
            try {
                mixpanelProperties.put("App Version", "SetMine v" +APP_VERSION);
                mixpanel.track("Application Opened", mixpanelProperties);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.v("Application Opened Tracked", mixpanel.toString());
        }
        setsManager = new SetsManager();
        fragmentManager = getSupportFragmentManager();
        modelsCP = new ModelsContentProvider();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheExtraOptions(480, 800, null)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);
        setContentView(R.layout.fragment_main);
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL).executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "upcoming", "upcomingEvents");
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL).executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "featured", "recentEvents");
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL).executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "upcoming", "searchEvents");
    }

    public void finishOnCreate() {
        Log.v("Finishing onCreate", " Line 98 ");
        try {
            getWindow().findViewById(R.id.splash_loading).setVisibility(View.GONE);
            calculateScreenSize();
            viewPagerContainerFragment = new ViewPagerContainerFragment();
            playerFragment = new PlayerFragment();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(R.id.player_frame, playerFragment);
            ft.add(R.id.eventPagerContainer, viewPagerContainerFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            getWindow().findViewById(R.id.splash_loading).setVisibility(View.GONE);
            ft.commit();
        } catch (RejectedExecutionException r) {
        }

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
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragmentManager.findFragmentById(R.id.eventPagerContainer));
        transaction.show(fragmentManager.findFragmentById(R.id.player_frame));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack(null);
        transaction.commit();
        View parent = (View) v.getParent();
        int id = parent.getId();
        setsManager.selectSetById(Integer.toString(((View) v.getParent()).getId()));
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

    @Override
    protected void onDestroy() {
        mixpanel.flush();
        super.onDestroy();
    }

    public Bitmap fastblur(Bitmap sentBitmap, int radius) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

}
