package com.setmine.android;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.setmine.android.adapter.MainPagerAdapter;
import com.setmine.android.adapter.PlayerPagerAdapter;
import com.setmine.android.fragment.ArtistDetailFragment;
import com.setmine.android.fragment.EventDetailFragment;
import com.setmine.android.fragment.MainViewPagerContainerFragment;
import com.setmine.android.fragment.PlayerContainerFragment;
import com.setmine.android.fragment.PlayerFragment;
import com.setmine.android.fragment.PlaylistFragment;
import com.setmine.android.fragment.SearchSetsFragment;
import com.setmine.android.fragment.TracklistFragment;
import com.setmine.android.fragment.UserFragment;
import com.setmine.android.object.Artist;
import com.setmine.android.object.Event;
import com.setmine.android.object.Set;
import com.setmine.android.task.InitialApiCallAsyncTask;
import com.setmine.android.util.DateUtils;
import com.setmine.android.util.ImageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.RejectedExecutionException;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SetMineMainActivity extends FragmentActivity implements
        InitialApiCaller,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int
            FACEBOOK_LOGIN = 195278;

    public static final String MIXPANEL_TOKEN = "dfe92f3c1c49f37a7d8136a2eb1de219";
    public static String APP_VERSION;
    public static final String API_VERSION = "3";
    public static final String API_ROOT_URL = "http://setmine.com/api/v/" + API_VERSION + "/";
    public static final String PUBLIC_ROOT_URL = "http://setmine.com/";
    public static final String S3_ROOT_URL = "http://stredm.s3-website-us-east-1.amazonaws.com/namecheap/";

    private static final String TAG = "SetMineMainActivity";

    public MainPagerAdapter mMainPagerAdapter;
    public ViewPager eventViewPager;
    public PlayerPagerAdapter mPlayerPagerAdapter;

    public FragmentManager fragmentManager;
    public PlayerContainerFragment playerContainerFragment;
    public PlaylistFragment playlistFragment;
    public PlayerFragment playerFragment;
    public TracklistFragment tracklistFragment;
    public SearchSetsFragment searchSetsFragment;
    public MainViewPagerContainerFragment mainViewPagerContainerFragment;
    public UserFragment userFragment;

    public ModelsContentProvider modelsCP;
    public SetsManager setsManager;
    public PlayerService playerService;
    public boolean serviceBound = false;
    public boolean userIsRegistered = false;

    public Integer screenHeight;
    public Integer screenWidth;
    public int asyncTasksInProgress;


    public MixpanelAPI mixpanel;
    public LocationClient locationClient;
    public Location currentLocation;

    public boolean finishedOnCreate = false;
    public Menu menu;
    public ActionBar actionBar;

    public ImageUtils imageUtils;
    public DateUtils dateUtils;

    // Create the Service Connection to start and bind at onStart()

    public ServiceConnection playerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            playerService = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    // Activity Handling

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get Application Version from build.gradle

        try {
            APP_VERSION = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Only allow keyboard pop up on EditText click

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Check for Google Play Services for Location
        // Use Gainesville, FL if not found, and don't use soonestEventsAroundMe

        if (servicesConnected()) {
            locationClient = new LocationClient(this, this, this);
            locationClient.connect();
        }
        else {
            currentLocation = new Location("default");
            currentLocation.setLatitude(29.652175);
            currentLocation.setLongitude(-82.325856);
            String eventSearchUrl = "upcoming?latitude="+currentLocation.getLatitude()+"&longitude="
                    +currentLocation.getLongitude();
            new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                    .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR,
                            eventSearchUrl,
                            "searchEvents");
            new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                    .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR,
                            "upcoming",
                            "upcomingEvents");
        }

        // Image utilities for smoothly loading and cachine images

        imageUtils = new ImageUtils();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheExtraOptions(480, 800, null)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);

        // See utils/DateUtils.java for documentation

        dateUtils = new DateUtils();

        // Fragment Manager handles all fragments and the navigation between them

        fragmentManager = getSupportFragmentManager();

        // A Content Provider for storing models returned from the SetMine API
        // It is not implemented as a traditional Android Content Provider (pending project)

        modelsCP = new ModelsContentProvider();

        // SetsManager handles updating the playlist and keeping track of set results

        setsManager = new SetsManager();

        // On every navigation change (backStackChanged), the Acton Bar hides or shows the back
        // button depending on if the user is at the top level

        actionBar = getActionBar();

        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    actionBar.getCustomView().findViewById(R.id.backButton).setVisibility(View.VISIBLE);
                } else {
                    actionBar.getCustomView().findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
                }
            }
        });

        // Mixpanel Instance for sending data to Mixpanel to analyze metrics

        mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);

        JSONObject mixpanelProperties = new JSONObject();
        try {
            mixpanelProperties.put("App Version", "SetMine v" +APP_VERSION);
            mixpanel.track("Application Opened", mixpanelProperties);
            MixpanelAPI.People people = mixpanel.getPeople();
            String id = mixpanel.getDistinctId();
            mixpanel.identify(id);
            people.identify(id);

            // Initialize Push Notifications with Google Sender ID

            people.initPushHandling("699004373125");

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            String nowAsISO = df.format(new Date());
            people.setOnce("user_id", id);
            people.set("SetMine Upgrade", "Yes");
            people.set("Client", "SetMine");
            people.set("Version", "SetMine v"+APP_VERSION);
            people.setOnce("date_tracked", nowAsISO);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Sets the Activity view where all fragment view containers are held

        setContentView(R.layout.fragment_main);

        // See each method for documentation

        handleIntent(getIntent());
        applyCustomViewStyles();

        // Get All initial Data Models from SetMine API and store it in the content provider

        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "featured", "recentEvents");
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "artist", "artists");
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "festival", "festivals");
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "mix", "mixes");
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "genre", "genres");
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "popular", "popularSets");
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "recent", "recentSets");
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, "artist?all=true", "allArtists");

        // See finishOnCreate method which is called after these tasks are finished executing
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start and Bind the PlayerService

        if(!serviceBound) {
            Intent intent = new Intent(this, PlayerService.class);
            startService(intent);
            bindService(intent, playerServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "ONPAUSE");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "ONRESUME");
    }

    @Override
    protected void onDestroy() {

        // Flush all data to the Mixpanel Server

        mixpanel.flush();

        // Stop and Unbind the PlayerService

        if(serviceBound) {
            unbindService(playerServiceConnection);
            serviceBound = false;
        }
        playerService.stopSelf();

        super.onDestroy();
    }

    // Implement InitialApiCaller Interface

    @Override
    public void onInitialResponseReceived(JSONObject jsonObject, String modelType) {
        modelsCP.setModel(jsonObject, modelType);
        if(modelsCP.initialModelsReady) {
            finishOnCreate();
        }
    }

    // Executed after all initial models are loaded

    public void finishOnCreate() {
        try {
            calculateScreenSize();

            // Create the 2nd Level Fragments

            mainViewPagerContainerFragment = new MainViewPagerContainerFragment();
            playerContainerFragment = new PlayerContainerFragment();
            searchSetsFragment = new SearchSetsFragment();

            // Add them to the activity's container fragment

            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.add(R.id.playerPagerContainer, playerContainerFragment);
            ft.add(R.id.eventPagerContainer, mainViewPagerContainerFragment);
            ft.add(R.id.searchSetsContainer, searchSetsFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();

            // Remove the splash loader

            getWindow().findViewById(R.id.splash_loading).setVisibility(View.GONE);

        } catch (RejectedExecutionException r) {
            r.printStackTrace();
        }
    }

    // Add any initial view changes here

    public void applyCustomViewStyles() {
        LayoutInflater inflater = LayoutInflater.from(this);

        // Use a custom action bar view

        View customView = inflater.inflate(R.layout.custom_action_bar, null);
        actionBar.setCustomView(customView);
        actionBar.setDisplayShowCustomEnabled(true);
    }

    public void calculateScreenSize() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        screenHeight = size.y;
        screenWidth = size.x;
    }

    // Required for applying a global custom font style

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    // Click Function for the Home button on the Action Bar

    public void homeButtonPress(View v) {

        // Hide Player and Search Sets
        // Show Main View Pager (Home, Events, Sets, Find)

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragmentManager.findFragmentById(R.id.searchSetsContainer));
        transaction.hide(fragmentManager.findFragmentById(R.id.playerPagerContainer));
        transaction.show(fragmentManager.findFragmentById(R.id.eventPagerContainer));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();

        // Add it to the Fragment Back Stack (see Android Dev for Documentation)

        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    // Click Function for the Back button on the Action Bar

    @Override
    public void onBackPressed() {
        if(fragmentManager.getBackStackEntryCount() > 0)
            super.onBackPressed();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragmentManager.findFragmentById(R.id.searchSetsContainer));
        transaction.hide(fragmentManager.findFragmentById(R.id.playerPagerContainer));
        transaction.show(fragmentManager.findFragmentById(R.id.eventPagerContainer));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    // Click function for Venue Map Button in Event Detail Pages

    public void googleMapsAddressLookup(View v) {
        String address = ((TextView)((ViewGroup)v.getParent()).findViewById(R.id.locationText))
                .getText().toString();

        // Use an Intent to launch the Google Maps activity

        String url = "http://maps.google.com/maps?daddr="+address;
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
        startActivity(intent);
    }

    // Click Function for the Play button on the Action Bar

    public void playNavigationClick(View v) {
        if(setsManager.getPlaylist().size() > 0) {
            openPlayer();
            if(setsManager.selectedSet == null) {
                Random r = new Random();
                int randomInt = r.nextInt(setsManager.getPlaylist().size() - 1);
                Set s = setsManager.getPlaylist().get(randomInt);
                startPlayerFragment(Integer.parseInt(s.getId()));
            }
        }
        else {
            Random r = new Random();
            int randomInt = r.nextInt(modelsCP.getPopularSets().size() - 1);
            Set s = modelsCP.getPopularSets().get(randomInt);
            List<Set> oneSetList = new ArrayList<Set>();
            oneSetList.add(s);
            setsManager.setPlaylist(oneSetList);
            startPlayerFragment(Integer.parseInt(s.getId()));
        }
    }

    // Start the Player Fragment from anywhere in the App given the set ID

    public void startPlayerFragment(int setId) {
        openPlayer();
        if(playerFragment == null) {
            playerFragment = new PlayerFragment();
        }
        playlistFragment.updatePlaylist();
        setsManager.selectSetById(Integer.toString(setId));
        playerContainerFragment.mViewPager.setCurrentItem(1);
        playerFragment.playSong();
    }

    // Open Search Fragment from anywhere in the app

    public void openSearch(View v) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragmentManager.findFragmentById(R.id.eventPagerContainer));
        transaction.hide(fragmentManager.findFragmentById(R.id.playerPagerContainer));
        transaction.show(fragmentManager.findFragmentById(R.id.searchSetsContainer));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    // Open Player Fragment from anywhere in the app

    public void openPlayer() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragmentManager.findFragmentById(R.id.eventPagerContainer));
        transaction.hide(fragmentManager.findFragmentById(R.id.searchSetsContainer));
        transaction.show(fragmentManager.findFragmentById(R.id.playerPagerContainer));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    // Open Artist Detail Fragment from anywhere in the app given a valid Artist object

    public void openArtistDetailPage(Artist artist) {
        ArtistDetailFragment artistDetailFragment = new ArtistDetailFragment();
        artistDetailFragment.selectedArtist = artist;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.eventPagerContainer, artistDetailFragment, "artistDetailFragment");
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    // Open Event Detail Fragment from anywhere in the app given a valid Event object

    public void openEventDetailPage(Event event, String eventType) {
        EventDetailFragment eventDetailFragment = new EventDetailFragment();
        eventDetailFragment.currentEvent = event;
        eventDetailFragment.EVENT_TYPE = (eventType.equals("search")?"upcoming":eventType);
        eventDetailFragment.onEventAssigned();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.eventPagerContainer, eventDetailFragment, "eventDetailFragment");
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }


    // Required to intercept intents for handling

    @Override
    public void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    // Handles incoming intents for opening parts of the app

    public void handleIntent(Intent intent) {
        if(intent != null && intent.getAction() != null) {

            // Remote Controls and the Notification player send in this intent

            if(intent.getAction().equals("com.setmine.android.OPEN_PLAYER")) {
                openPlayer();
            } else if(intent.getAction().equals("com.setmine.android.VIEW")) {

                // Intent for deep linking

                String command = intent.getDataString();
                Log.d(TAG, command);
            }
        }
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
        super.onActivityResult(requestCode, resultCode, data);
        userFragment.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult");
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

    // Google Play Services listeners

    // Google Play Services successfully connected

    @Override
    public void onConnected(Bundle bundle) {

        // Default to Gainesville coordinates if location not available and disconnect

        if(locationClient.getLastLocation() != null) {
            currentLocation = locationClient.getLastLocation();
        }
        else {
            currentLocation = new Location("default");
            currentLocation.setLatitude(29.652175);
            currentLocation.setLongitude(-82.325856);
        }
        locationClient.disconnect();

        // Get upcoming events from API based on location

        String eventSearchUrl = "upcoming?latitude="+currentLocation.getLatitude()+"&longitude="
                +currentLocation.getLongitude();
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR,
                        eventSearchUrl,
                        "searchEvents");
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR, eventSearchUrl, "upcomingEventsAroundMe");

    }

    @Override
    public void onDisconnected() {
    }

    // Google Play Services connection failed

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        // Use Gainesville as location

        locationClient.disconnect();
        currentLocation = new Location("default");
        currentLocation.setLatitude(29.652175);
        currentLocation.setLongitude(-82.325856);
        String eventSearchUrl = "upcoming?latitude="+currentLocation.getLatitude()+"&longitude="
                +currentLocation.getLongitude();
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR,
                        eventSearchUrl,
                        "searchEvents");
        new InitialApiCallAsyncTask(this, getApplicationContext(), API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR,
                        "upcoming",
                        "upcomingEvents");
    }

}
