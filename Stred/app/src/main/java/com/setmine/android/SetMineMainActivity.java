package com.setmine.android;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.setmine.android.fragment.MainPagerContainerFragment;
import com.setmine.android.fragment.PlayerContainerFragment;
import com.setmine.android.fragment.PlayerFragment;
import com.setmine.android.fragment.PlaylistFragment;
import com.setmine.android.fragment.SearchSetsFragment;
import com.setmine.android.fragment.TracklistFragment;
import com.setmine.android.fragment.UserFragment;
import com.setmine.android.object.Artist;
import com.setmine.android.object.Constants;
import com.setmine.android.object.Event;
import com.setmine.android.object.Set;
import com.setmine.android.object.User;
import com.setmine.android.task.SetMineApiGetRequestAsyncTask;
import com.setmine.android.util.DateUtils;
import com.setmine.android.util.HttpUtils;
import com.setmine.android.util.ImageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.RejectedExecutionException;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SetMineMainActivity extends FragmentActivity implements
        ApiCaller,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final static int
            FACEBOOK_LOGIN = 64206;

    private static final String TAG = "SetMineMainActivity";

    public static String APP_VERSION;

    public String MODELS_VERSION;


    public MainPagerAdapter mMainPagerAdapter;
    public ViewPager eventViewPager;
    public PlayerPagerAdapter mPlayerPagerAdapter;

    public FragmentManager fragmentManager;
    public PlayerContainerFragment playerContainerFragment;
    public PlaylistFragment playlistFragment;
    public PlayerFragment playerFragment;
    public TracklistFragment tracklistFragment;
    public SearchSetsFragment searchSetsFragment;
    public MainPagerContainerFragment mainPagerContainerFragment;
    public UserFragment userFragment;
    public EventDetailFragment eventDetailFragment;
    public ArtistDetailFragment artistDetailFragment;


    public ModelsContentProvider modelsCP;
    public PlayerManager playerManager;
    public PlayerService playerService;
    public boolean serviceBound = false;
    public Set selectedSet;

    public User registeredUser;
    public boolean userIsRegistered;

    public int asyncTasksInProgress;


    public MixpanelAPI mixpanel;
    public LocationClient locationClient;
    public Location currentLocation;

    public Menu menu;
    public ActionBar actionBar;

    public ImageUtils imageUtils;
    public DateUtils dateUtils;


    // Create the Service Connection to start and bind at onStart()

    public ServiceConnection playerServiceConnection;

    final Handler handler = new Handler();

    final Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            finishOnCreate();
        }
    };

    @Override
    public void onApiResponseReceived(JSONObject jsonObject, String identifier) {
        final JSONObject finalJsonObject = jsonObject;
        final String finalIdentifier = identifier;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onApiResponseReceived: ");
                if(finalIdentifier.equals("modelsVersion")) {
                    try {
                        boolean modelsVersionMatches = finalJsonObject.getJSONObject("payload").getBoolean("version");
                        Log.d(TAG, Boolean.toString(modelsVersionMatches));
                        if(modelsVersionMatches) {}
                        else {
                            String newModelsVersion = finalJsonObject.getString("models_version");
                            Log.d(TAG, newModelsVersion);
                            updateModelsVersion(newModelsVersion);
                        }
                        getModels(modelsVersionMatches);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    modelsCP.setModel(finalJsonObject, finalIdentifier);
                    if(modelsCP.initialModelsReady) {
                        handler.post(updateUI);
                    }
                }
            }
        }).start();

    }

    public void updateModelsVersion(String newModelsVersion) {

        try {
            FileOutputStream fos = openFileOutput("models_version_file", Context.MODE_PRIVATE);
            fos.write(newModelsVersion.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getModels(boolean modelsAreStored) {

        Log.d(TAG, "getModels");

        // IF modelsVersionMatches is true, models are stored on device

        modelsCP = new ModelsContentProvider();
        if(modelsAreStored) {
            try {
                FileInputStream fis = openFileInput("stored_models_file");
                int c;
                String jsonString = "";
                while( (c = fis.read()) != -1){
                    jsonString = jsonString + Character.toString((char)c);
                }
                fis.close();
                Log.d(TAG, "stored models: " + jsonString);
            } catch (Exception e) {

            }
        } else {

            // Get All initial Data Models from SetMine API and store it in the content provider
            // See onApiResponse method


        }
    }

    public String getModelsVersion() {
        Log.d(TAG, "getModelsVersion");

        try {
            File file = getBaseContext().getFileStreamPath("models_version_file");
            if(file.exists()) {
                FileInputStream fis = openFileInput("models_version_file");
                int c;
                MODELS_VERSION = "";
                while( (c = fis.read()) != -1) {
                    MODELS_VERSION = MODELS_VERSION + Character.toString((char)c);
                }
                fis.close();
                Log.d(TAG, "MODELS_VERSION Found: " + MODELS_VERSION);
                return MODELS_VERSION;
            } else {
                return "0";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    public void checkModelsVersion() {
        Log.d(TAG, "checkModelsVersion");
        String modelsVersion = getModelsVersion();
        new SetMineApiGetRequestAsyncTask(this, this)
                .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                        , "version/" + modelsVersion, "modelsVersion");
    }

    // Activity Handling

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        mixpanel = MixpanelAPI.getInstance(this, Constants.MIXPANEL_TOKEN);

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

        // Create the ServiceConnection to the PlayerService every time the activity is created, regardless of savedInstanceState

        playerServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected");
                PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
                playerService = binder.getService();
                serviceBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected");
                serviceBound = false;
            }
        };

//        checkModelsVersion();

        // Fragment Manager handles all fragments and the navigation between them

        fragmentManager = getSupportFragmentManager();

        if(modelsCP == null) {
            modelsCP = new ModelsContentProvider();
        }

        // PlayerManager handles updating the playlist and keeping track of set results

//        if(playerManager == null) {
//            playerManager = new PlayerManager();
//        }

        if(savedInstanceState == null) {
            userIsRegistered = false;

            // Check for Google Play Services for Location
            // Use Gainesville, FL if not found, and don't use soonestEventsAroundMe

            if (servicesConnected()) {
                Log.d(TAG, "servicesConnected");
                locationClient = new LocationClient(this, this, this);
                locationClient.connect();
            }
            else {
                Log.d(TAG, "services NOT Connected");
                currentLocation = new Location("default");
                currentLocation.setLatitude(29.652175);
                currentLocation.setLongitude(-82.325856);
            }

            new SetMineApiGetRequestAsyncTask(this, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                            "featured", "recentEvents");
            new SetMineApiGetRequestAsyncTask(this, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "artist", "artists");
            new SetMineApiGetRequestAsyncTask(this, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "festival", "festivals");
            new SetMineApiGetRequestAsyncTask(this, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "mix", "mixes");
            new SetMineApiGetRequestAsyncTask(this, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "genre", "genres");
            new SetMineApiGetRequestAsyncTask(this, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "popular", "popularSets");
            new SetMineApiGetRequestAsyncTask(this, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "recent", "recentSets");
            new SetMineApiGetRequestAsyncTask(this, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "artist?all=true", "allArtists");

        } else {
            Log.d(TAG, "getting instance state");
            userIsRegistered = savedInstanceState.getBoolean("userIsRegistered");
            currentLocation = savedInstanceState.getParcelable("currentLocation");
        }

        // Get Application Version from build.gradle

        try {
            APP_VERSION = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Image utilities for smoothly loading and caching images

        imageUtils = new ImageUtils();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheExtraOptions(480, 800, null)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);

        // See utils/DateUtils.java for documentation

        dateUtils = new DateUtils();

        // On every navigation change (backStackChanged), the Acton Bar hides or shows the back
        // button depending on if the user is at the top level

        actionBar = getActionBar();

        // Handles showing or hiding the back button in the action bar

//        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
//            @Override
//            public void onBackStackChanged() {
//                if (fragmentManager.getBackStackEntryCount() > 0) {
//                    actionBar.getCustomView().findViewById(R.id.backButton).setVisibility(View.VISIBLE);
//                } else {
//                    actionBar.getCustomView().findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
//                }
//            }
//        });

        // Sets the Activity view for container fragments

        setContentView(R.layout.fragment_main);

        // See each method for documentation

        handleIntent(getIntent());
        applyCustomViewStyles();
//        createSecondLevelFragments();

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
        Log.d(TAG, "onStart");

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
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");

        // Flush all data to the Mixpanel Server

        mixpanel.flush();

        // Stop and Unbind the PlayerService

        if(serviceBound) {
            unbindService(playerServiceConnection);
            serviceBound = false;
        }
        playerService.stopSelf();

    }

    // Executed after all initial models are loaded

    public void finishOnCreate() {
        try {
            // Remove the splash loader

            getWindow().findViewById(R.id.splash_loading).setVisibility(View.GONE);

            // Initialize the MainViewPagerFragment

            mainPagerContainerFragment = new MainPagerContainerFragment();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.currentFragmentContainer, mainPagerContainerFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();

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

        // Only allow keyboard pop up on EditText click

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // Required for applying a global custom font style

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    // Click Function for the Home button on the Action Bar

    public void homeButtonPress(View v) {
        openMainViewPager();
    }

    // Click Function for the Back button on the Action Bar

    @Override
    public void onBackPressed() {
        if(fragmentManager.getBackStackEntryCount() > 0)
            super.onBackPressed();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
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
        if(playerService.mediaPlayer.isPlaying()) {
            startPlayerFragment();
        } else {
            playSetWithSetID("random");
        }
    }

    // Start the Player Fragment from anywhere in the App given the set ID

    public void playSetWithSetID(String setID) {
        final Handler playHandler = new Handler();
        final Runnable playSet = new Runnable() {
            @Override
            public void run() {
                playerService.playerManager.clearPlaylist();
                playerService.playerManager.addToPlaylist(selectedSet);
            }
        };
        final HttpUtils httpUtil =
                new HttpUtils(this.getApplicationContext(), Constants.API_ROOT_URL);
        final String finalSetId = setID;

        if(setID.equals("random")) {
            playerService.playerManager.setPlaylist(modelsCP.getPopularSets());
            Random r = new Random();
            int randomInt = r.nextInt(playerService.playerManager.getPlaylist().size() - 1);
            selectedSet = playerService.playerManager.getPlaylist().get(randomInt);
            setID = selectedSet.getId();
            playerService.playerManager.selectSetById(setID);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String apiRequest = "set/id/" + finalSetId;
                        String jsonString = httpUtil.getJSONStringFromURL(apiRequest);
                        JSONObject jsonResponse = new JSONObject(jsonString);
                        if(jsonResponse.get("status").equals("success")) {
                            JSONObject setJson = jsonResponse
                                    .getJSONObject("payload")
                                    .getJSONObject("set");
                            selectedSet = new Set(setJson);
                            playHandler.post(playSet);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        startPlayerFragment();
        playerContainerFragment.mPlayerPagerAdapter.playerFragment.playSong();
    }

    public void openMainViewPager() {
        Log.d(TAG, "openMainViewPager");
        mainPagerContainerFragment = new MainPagerContainerFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.currentFragmentContainer, mainPagerContainerFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    // Open Player Fragment from anywhere in the app

    public void startPlayerFragment() {
        Log.d(TAG, "startPlayerFragment");
        playerContainerFragment = new PlayerContainerFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.currentFragmentContainer, playerContainerFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    // Open Search Fragment from anywhere in the app

    public void startSearchFragment(View v) {
        Log.d(TAG, "startSearchFragment");
        searchSetsFragment =  new SearchSetsFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.currentFragmentContainer, searchSetsFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    // Open Artist Detail Fragment from anywhere in the app given a valid Artist object

    public void openArtistDetailPage(Artist artist) {
        artistDetailFragment = new ArtistDetailFragment();
        Bundle args = new Bundle();
        args.putString("currentArtist", artist.jsonModelString);
        artistDetailFragment.setArguments(args);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.currentFragmentContainer, artistDetailFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    // Open Event Detail Fragment from anywhere in the app given a valid Event object

    public void openEventDetailPage(Event event, String eventType) {
        eventDetailFragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putString("currentEvent", event.jsonModelString);
        args.putString("eventType", (eventType.equals("search") ? "upcoming" : eventType));
        eventDetailFragment.setArguments(args);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.currentFragmentContainer, eventDetailFragment);
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
                startPlayerFragment();
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
        Log.d(TAG, "onActivityResult: " + Integer.toString(requestCode) + " " + Integer.toString(requestCode) + " " + data.toString());
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

        String eventSearchUrl = "upcoming/?latitude=" + currentLocation.getLatitude();
        eventSearchUrl += "&longitude=" + currentLocation.getLongitude();
        new SetMineApiGetRequestAsyncTask(this, this)
                .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                        eventSearchUrl, "upcomingEvents");
        new SetMineApiGetRequestAsyncTask(this, this)
                .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                        eventSearchUrl, "searchEvents");

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
        new SetMineApiGetRequestAsyncTask(this, this)
                .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                        eventSearchUrl,
                        "searchEvents");
        new SetMineApiGetRequestAsyncTask(this, this)
                .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                        "upcoming",
                        "upcomingEvents");
    }

}
