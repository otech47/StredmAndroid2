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
import android.widget.TextView;

import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.setmine.android.adapter.EventPagerAdapter;
import com.setmine.android.adapter.PlayerPagerAdapter;
import com.setmine.android.fragment.ArtistDetailFragment;
import com.setmine.android.fragment.EventDetailFragment;
import com.setmine.android.fragment.MainViewPagerContainerFragment;
import com.setmine.android.fragment.PlayerContainerFragment;
import com.setmine.android.fragment.PlayerFragment;
import com.setmine.android.fragment.PlaylistFragment;
import com.setmine.android.fragment.SearchSetsFragment;
import com.setmine.android.fragment.TracklistFragment;
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
        LineupsSetsApiCaller,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int
            FACEBOOK_LOGIN = 326350;

    public static final String MIXPANEL_TOKEN = "dfe92f3c1c49f37a7d8136a2eb1de219";
    public static String APP_VERSION;
    public static final String API_VERSION = "3";
    public static final String API_ROOT_URL = "http://setmine.com/api/v/" + API_VERSION + "/";
    public static final String PUBLIC_ROOT_URL = "http://setmine.com/";
    public static final String S3_ROOT_URL = "http://stredm.s3-website-us-east-1.amazonaws.com/namecheap/";

    private static final String TAG = "SetMineMainActivity";

    public EventPagerAdapter mEventPagerAdapter;
    public ViewPager eventViewPager;
    public PlayerPagerAdapter mPlayerPagerAdapter;

    public FragmentManager fragmentManager;
    public PlayerContainerFragment playerContainerFragment;
    public PlaylistFragment playlistFragment;
    public PlayerFragment playerFragment;
    public TracklistFragment tracklistFragment;
    public SearchSetsFragment searchSetsFragment;
    public MainViewPagerContainerFragment mainViewPagerContainerFragment;

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

        // Create utilities, get instances, and save common variables

        imageUtils = new ImageUtils();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheExtraOptions(480, 800, null)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);

        dateUtils = new DateUtils();
        fragmentManager = getSupportFragmentManager();
        mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);
        modelsCP = new ModelsContentProvider();
        setsManager = new SetsManager();
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

        // Mixpanel Initial Tracking

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
            people.set("SetMine Upgrade: ", "Yes");
            people.set("Client", "SetMine");
            people.set("Version", "SetMine v"+APP_VERSION);
            people.setOnce("date_tracked", nowAsISO);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.fragment_main);

        handleIntent(getIntent());

        applyCustomViewStyles();

        // Get All initial Data Models from SetMine API and store

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
        if(!serviceBound) {
            Intent intent = new Intent(this, PlayerService.class);
            startService(intent);
            bindService(intent, playerServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }



    // For Facebook SDK

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "ONPAUSE");

        // Logs 'app deactivate' App Event.

        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "ONRESUME");

        // Logs 'install' and 'app activate' App Events.

        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onDestroy() {
        mixpanel.flush();
        if(serviceBound) {
            unbindService(playerServiceConnection);
            serviceBound = false;
        }
        playerService.stopSelf();
        super.onDestroy();
    }

    // Implementing InitialApiCaller Interface

    @Override
    public void onInitialResponseReceived(JSONObject jsonObject, String modelType) {
        this.modelsCP.setModel(jsonObject, modelType);
        if(modelsCP.initialModelsReady) {
            finishOnCreate();
        }
    }

    // Implementing LineupSetsApiCaller Interface

    @Override
    public void onLineupsSetsReceived(JSONObject jsonObject, String identifier) {
        this.modelsCP.setModel(jsonObject, identifier);
    }

    public void finishOnCreate() {
        if(!finishedOnCreate) {
            try {
                calculateScreenSize();
                mainViewPagerContainerFragment = new MainViewPagerContainerFragment();
                playerContainerFragment = new PlayerContainerFragment();
                searchSetsFragment = new SearchSetsFragment();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.add(R.id.playerPagerContainer, playerContainerFragment);
                ft.add(R.id.eventPagerContainer, mainViewPagerContainerFragment);
                ft.add(R.id.searchSetsContainer, searchSetsFragment);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                getWindow().findViewById(R.id.splash_loading).setVisibility(View.GONE);
                ft.commit();
            } catch (RejectedExecutionException r) {
                    r.printStackTrace();
            }
            finishedOnCreate = true;
        }
    }

    public void applyCustomViewStyles() {
        LayoutInflater inflater = LayoutInflater.from(this);
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    public void homeButtonPress(View v) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragmentManager.findFragmentById(R.id.searchSetsContainer));
        transaction.hide(fragmentManager.findFragmentById(R.id.playerPagerContainer));
//        transaction.hide(fragmentManager.findFragmentById(R.id.loginContainer));
        transaction.show(fragmentManager.findFragmentById(R.id.eventPagerContainer));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void onBackPressed() {
        if(fragmentManager.getBackStackEntryCount() > 0)
            super.onBackPressed();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragmentManager.findFragmentById(R.id.searchSetsContainer));
        transaction.hide(fragmentManager.findFragmentById(R.id.playerPagerContainer));
//        transaction.hide(fragmentManager.findFragmentById(R.id.loginContainer));
        transaction.show(fragmentManager.findFragmentById(R.id.eventPagerContainer));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public void googleMapsAddressLookup(View v) {
        String address = ((TextView)((ViewGroup)v.getParent()).findViewById(R.id.locationText))
                .getText().toString();
        String url = "http://maps.google.com/maps?daddr="+address;
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
        startActivity(intent);
    }

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

    public void startPlayerFragment(int setId) {
        if(playerFragment == null) {
            playerFragment = new PlayerFragment();
        }
        openPlayer();
        playlistFragment.updatePlaylist();
        setsManager.selectSetById(Integer.toString(setId));
        playerContainerFragment.mViewPager.setCurrentItem(1);
        playerFragment.playSong(setsManager.selectedSetIndex);
    }

    public void openUserHomePage() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragmentManager.findFragmentById(R.id.playerPagerContainer));
        transaction.hide(fragmentManager.findFragmentById(R.id.searchSetsContainer));
        transaction.show(fragmentManager.findFragmentById(R.id.eventPagerContainer));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public void openLogin(View v) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragmentManager.findFragmentById(R.id.eventPagerContainer));
        transaction.hide(fragmentManager.findFragmentById(R.id.playerPagerContainer));
        transaction.hide(fragmentManager.findFragmentById(R.id.searchSetsContainer));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public void openSearch(View v) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragmentManager.findFragmentById(R.id.eventPagerContainer));
        transaction.hide(fragmentManager.findFragmentById(R.id.playerPagerContainer));
        transaction.show(fragmentManager.findFragmentById(R.id.searchSetsContainer));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public void openPlayer() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.hide(fragmentManager.findFragmentById(R.id.eventPagerContainer));
        transaction.hide(fragmentManager.findFragmentById(R.id.searchSetsContainer));
        transaction.show(fragmentManager.findFragmentById(R.id.playerPagerContainer));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commitAllowingStateLoss();
    }

    public void openArtistPage(Artist artist) {
        ArtistDetailFragment artistDetailFragment = new ArtistDetailFragment();
        artistDetailFragment.selectedArtist = artist;
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.eventPagerContainer, artistDetailFragment, "artistDetailFragment");
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public void openEventPage(Event event, String eventType) {
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

    public void closePlayer() {
        Log.v("Close ", "player");
    }

    public void handleIntent(Intent intent) {
        if(intent != null && intent.getAction() != null) {
            if(intent.getAction().equals("com.setmine.android.OPEN_PLAYER")) {
                openPlayer();
            } else if(intent.getAction().equals("com.setmine.android.VIEW")) {
                String command = intent.getDataString();
                Log.d(TAG, command);
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        handleIntent(intent);
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
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
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
        if(locationClient.getLastLocation() != null) {
            currentLocation = locationClient.getLastLocation();
        }
        else {
            currentLocation = new Location("default");
            currentLocation.setLatitude(29.652175);
            currentLocation.setLongitude(-82.325856);
        }
        locationClient.disconnect();
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

    // Implementing Failed Connection Listeners for Google Play Services

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
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

    // Blurring images for player, called by PlayerFragment


}
