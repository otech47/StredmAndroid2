package com.setmine.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.setmine.android.adapter.EventPagerAdapter;
import com.setmine.android.adapter.PlayerPagerAdapter;
import com.setmine.android.fragment.PlayerContainerFragment;
import com.setmine.android.fragment.PlayerFragment;
import com.setmine.android.fragment.TracklistFragment;
import com.setmine.android.fragment.ViewPagerContainerFragment;
import com.setmine.android.task.InitialApiCallAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SetMineMainActivity extends FragmentActivity implements
        InitialApiCaller,
        LineupsSetsApiCaller,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    public static final String MIXPANEL_TOKEN = "dfe92f3c1c49f37a7d8136a2eb1de219";
    public static final String APP_VERSION = "1.2";
    public static final String API_VERSION = "1";
    public static final String API_ROOT_URL = "http://setmine.com/api/v/" + API_VERSION + "/";
    public static final String PUBLIC_ROOT_URL = "http://setmine.com/";
    public static final String S3_ROOT_URL = "http://stredm.s3-website-us-east-1.amazonaws.com/namecheap/";
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public EventPagerAdapter mEventPagerAdapter;
    public ViewPager eventViewPager;
    public PlayerPagerAdapter mPlayerPagerAdapter;
    public ModelsContentProvider modelsCP;
    public Integer screenHeight;
    public Integer screenWidth;
    public FragmentManager fragmentManager;
    public Menu menu;
    public SetsManager setsManager;
    public PlayerContainerFragment playerContainerFragment;
    public PlayerFragment playerFragment;
    public TracklistFragment tracklistFragment;
    public View playerFrame;
    public ViewPagerContainerFragment viewPagerContainerFragment;
    public HashMap<String, List<View>> preloadedTiles = new HashMap<String, List<View>>();
    public InitialApiCallAsyncTask getLineupAsyncTask;
    public InitialApiCallAsyncTask asyncApiCaller;
    public MixpanelAPI mixpanel;
    public int asyncTasksInProgress;
    public LocationClient locationClient;
    public Location currentLocation;

    // Implementing InitialApiCaller Interface

    @Override
    public void onInitialResponseReceived(JSONObject jsonObject, String modelType) {
        this.modelsCP.setModel(jsonObject, modelType);
        if(modelsCP.upcomingEvents.size() > 1 && modelsCP.recentEvents.size() > 1 && modelsCP.searchEvents.size() > 1) {
            finishOnCreate();
        }
    }

    // Implementing LineupSetsApiCaller Interface

    @Override
    public void onLineupsSetsReceived(JSONObject jsonObject, String identifier) {
        this.modelsCP.setModel(jsonObject, identifier);
    }

    // Activity Handling

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
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            Log.v("LOCATION FOUND", "NAH");
            locationClient = new LocationClient(this, this, this);
            locationClient.connect();
        }
        else {
            Log.v("No", "Location");
            currentLocation = new Location("default");
            currentLocation.setLatitude(29.652175);
            currentLocation.setLongitude(-82.325856);
            String eventSearchUrl = "upcoming?latitude="+currentLocation.getLatitude()+"&longitude="
                    +currentLocation.getLongitude();
            new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                    .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR,
                            eventSearchUrl,
                            "searchEvents");
        }
        setsManager = new SetsManager();
        fragmentManager = getSupportFragmentManager();
        modelsCP = new ModelsContentProvider();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheExtraOptions(480, 800, null)
                .diskCacheSize(60 * 1024 * 1024)
                .diskCacheFileCount(200)
                .build();
        ImageLoader.getInstance().init(config);
        setContentView(R.layout.fragment_main);

        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL).executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "upcoming", "upcomingEvents");
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL).executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "featured", "recentEvents");
    }

    public void finishOnCreate() {
        Log.v("Finishing onCreate", " Line 98 ");
        try {
            getWindow().findViewById(R.id.splash_loading).setVisibility(View.GONE);
            calculateScreenSize();
            viewPagerContainerFragment = new ViewPagerContainerFragment();
            playerContainerFragment = new PlayerContainerFragment();
//            playerFragment = new PlayerFragment();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(R.id.playerPagerContainer, playerContainerFragment);
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

    public void copyToClipboard(View v) {
        String address = ((TextView)((ViewGroup)v.getParent()).findViewById(R.id.locationText))
                .getText().toString();
        String url = "http://maps.google.com/maps?daddr="+address;
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
        startActivity(intent);
    }

    public void startPlayerFragment(int setId) {
        if(playerFragment == null) {
            playerFragment = new PlayerFragment();
        }
        playerFragment.setPlayListeners();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragmentManager.findFragmentById(R.id.eventPagerContainer));
        transaction.show(fragmentManager.findFragmentById(R.id.playerPagerContainer));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack(null);
        transaction.commit();
        setsManager.selectSetById(Integer.toString(setId));
        playerFragment.playSong(setsManager.selectedSetIndex);
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

    // Location Services

    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }
        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
                switch (resultCode) {
                    case Activity.RESULT_OK :
                        break;
                }
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.v("Location Updates", "Google Play services is available.");
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            if (errorDialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(),
                        "Location Updates");
            }
            return false;
        }
    }

    // Implementing ConnectionCallbacks for Google Play Services

    @Override
    public void onConnected(Bundle bundle) {
        currentLocation = locationClient.getLastLocation();
        locationClient.disconnect();
        String eventSearchUrl = "upcoming?latitude="+currentLocation.getLatitude()+"&longitude="
                +currentLocation.getLongitude();
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR,
                        eventSearchUrl,
                        "searchEvents");
    }

    @Override
    public void onDisconnected() {

    }

    // Implementing Failed Connection Listeners for Google Play Services

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // Blurring images for player, called by PlayerFragment

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

        try {

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

        } catch(ArrayIndexOutOfBoundsException e) {}

        return (bitmap);
    }

}
