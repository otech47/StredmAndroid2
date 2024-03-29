package com.setmine.android;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.gimbal.android.BeaconEventListener;
import com.gimbal.android.BeaconManager;
import com.gimbal.android.BeaconSighting;
import com.gimbal.android.Communication;
import com.gimbal.android.CommunicationListener;
import com.gimbal.android.CommunicationManager;
import com.gimbal.android.Gimbal;
import com.gimbal.android.PlaceEventListener;
import com.gimbal.android.PlaceManager;
import com.gimbal.android.Push;
import com.gimbal.android.Visit;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.setmine.android.Offer.Offer;
import com.setmine.android.Offer.OfferDetailFragment;
import com.setmine.android.api.SetMineApiGetRequestAsyncTask;
import com.setmine.android.artist.Artist;
import com.setmine.android.artist.ArtistDetailFragment;
import com.setmine.android.event.EventDetailFragment;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.player.PlayerContainerFragment;
import com.setmine.android.player.PlayerService;
import com.setmine.android.player.PlaylistFragment;
import com.setmine.android.search.SearchSetsFragment;
import com.setmine.android.set.Set;
import com.setmine.android.user.User;
import com.setmine.android.user.UserFragment;
import com.setmine.android.util.HttpUtils;

import net.danlew.android.joda.JodaTimeAndroid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    public MainPagerContainerFragment mainPagerContainerFragment;
    public PlayerContainerFragment playerContainerFragment;
    public SearchSetsFragment searchSetsFragment;
    public ArtistDetailFragment artistDetailFragment;
    public EventDetailFragment eventDetailFragment;
    public UserFragment userFragment;
    public OfferDetailFragment offerDetailFragment;

    public PlayerService playerService;
    public boolean serviceBound = false;
    public Set selectedSet;

    public User user;
    public boolean userIsRegistered;

    public int asyncTasksInProgress;

    public MixpanelAPI mixpanel;
    public LocationClient locationClient;
    public Location currentLocation;

    public Menu menu;

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
                } else if(finalIdentifier.equals("offers/venue")) {
                    Log.d(TAG, finalJsonObject.toString());
                    try {
                        JSONArray availableOffersJson = finalJsonObject.getJSONObject("payload")
                                .getJSONArray("offer");
                        if(availableOffersJson.length() > 0) {
                            unlockOffers(availableOffersJson);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (finalIdentifier.equals("offers/unlock")) {
                    Log.d(TAG, finalJsonObject.toString());
                    Log.d(TAG, "offers/unlock");
                    try {
                        JSONObject payload = finalJsonObject.getJSONObject("payload");
                        if (payload.getString("unlock_status").equals("success")) {
                            Offer unlockedOffer = new Offer(payload.getJSONObject("offer"));
                            showUnlockedOfferNotification(unlockedOffer);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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

    // Track Application Open Mixpanel Event and Identify People properties

    public void initializeMixpanel() {
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
    }

    // Gimbal Functionality

    private PlaceEventListener placeEventListener;
    private CommunicationListener communicationListener;
    private BeaconEventListener beaconSightingListener;
    private BeaconManager beaconManager;

    private boolean verifiedPlaceID;

    public void initializeGimbal() {
        Gimbal.setApiKey(this.getApplication(), "c06f63bf-ef5f-45e9-9edf-4f600c86c67a");
        Gimbal.registerForPush("699004373125");

        verifiedPlaceID = false;

        createPlaceListener();
        createCommunicationListener();
        createBeaconListener();

        Log.i(TAG, "initializeGimbal");


        PlaceManager.getInstance().startMonitoring();
        beaconManager.startListening();
        CommunicationManager.getInstance().startReceivingCommunications();
    }

    public void createPlaceListener() {
        final SetMineMainActivity activity = this;
        placeEventListener = new PlaceEventListener() {
            @Override
            public void onVisitStart(Visit visit) {
                // This will be invoked when a place is entered. Example below shows a simple log upon enter
                Log.d(TAG, "Enter: " + visit.getPlace().getName() + ", at: " + new Date(visit.getArrivalTimeInMillis()));
            }

            @Override
            public void onVisitEnd(Visit visit) {
                // This will be invoked when a place is exited. Example below shows a simple log upon exit
                Log.d(TAG, "Exit: " + visit.getPlace().getName() + ", at: " + new Date(visit.getDepartureTimeInMillis()));
            }

            @Override
            public void onBeaconSighting(BeaconSighting beaconSighting, List<Visit> list) {
                Log.d(TAG, beaconSighting.toString());
                Log.d(TAG, Float.toString(beaconSighting.getRSSI()));

                Log.d(TAG, Boolean.toString(verifiedPlaceID));
                Log.d(TAG, Integer.toString(list.size()));

                if(beaconSighting.getRSSI() > -45 && !verifiedPlaceID) {
                    verifiedPlaceID = true;
                    Log.i(TAG, beaconSighting.toString());
                    if(list.size() > 0) {
                        for(int i = 0; i < list.size(); i++) {

                            // Gimbal SHIT broken
//                            String placeName = list.get(i).getPlace().getName();
//                            String placeID = list.get(i).getPlace().getIdentifier();
//                            Log.d(TAG, placeName);
//
//                            // Send place identifier to server to verify Place with offers to unlock
//
//                            new SetMineApiGetRequestAsyncTask(activity, activity)
//                                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
//                                            , "offers/venue/" + placeID, "offers/venue");
//
//                            if(placeName.equals("Sethau5")) {
//                                Log.d(TAG, "Sethau5 detected");
//                                Log.d(TAG, placeID);
//                            }


                            String beaconID = beaconSighting.getBeacon().getIdentifier();
                            Log.d(TAG, beaconID);

                            new SetMineApiGetRequestAsyncTask(activity, activity)
                                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                                            , "offers/venue/" + beaconID, "offers/venue");
                        }
                    } else {
                        String beaconID = beaconSighting.getBeacon().getIdentifier();
                        Log.d(TAG, beaconID);

                        new SetMineApiGetRequestAsyncTask(activity, activity)
                                .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                                        , "offers/venue/" + beaconID, "offers/venue");
                    }

                }
            }
        };
        PlaceManager.getInstance().addListener(placeEventListener);
    }

    public void createCommunicationListener() {
        communicationListener = new CommunicationListener() {
            @Override
            public Collection<Communication> presentNotificationForCommunications(Collection<Communication> communications, Visit visit) {
                for (Communication comm : communications) {
                    Log.i("INFO", "Place Communication: " + visit.getPlace().getName() + ", message: " + comm.getTitle());
                }
                //allow Gimbal to show the notification for all communications
                return communications;
            }

            @Override
            public Collection<Communication> presentNotificationForCommunications(Collection<Communication> communications, Push push) {
                for (Communication comm : communications) {
                    Log.i("INFO", "Received a Push Communication with message: " + comm.getTitle());
                }
                //allow Gimbal to show the notification for all communications
                return communications;
            }

            @Override
            public void onNotificationClicked(List<Communication> communications) {
                Log.i("INFO", "Notification was clicked on");
            }
        };
        CommunicationManager.getInstance().addListener(communicationListener);
    }

    public void createBeaconListener() {
        beaconSightingListener = new BeaconEventListener() {
            @Override
            public void onBeaconSighting(BeaconSighting sighting) {
                Log.i("INFO", sighting.toString());
            }
        };
        beaconManager = new BeaconManager();
        beaconManager.addListener(beaconSightingListener);
    }

    public void showUnlockedOfferNotification(Offer unlockedOffer) {
        Log.d(TAG, "showUnlockedOfferNotification");

        Artist offerArtist = unlockedOffer.getArtist();

        // reusable variables
        PendingIntent pendingIntent;
        Intent intent;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.logo_small)
                        .setContentTitle("Offer Unlocked!")
                        .setContentText("You've unlocked a new set by " + offerArtist.getArtist())
                        .setAutoCancel(true);

        intent = new Intent(getApplicationContext(), SetMineMainActivity.class)
                .setAction("OFFER_UNLOCKED");
        intent.putExtra("offer_id", unlockedOffer.getOfferId());
        pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        // Show the notification in the notification bar.

        try {
            NotificationManager mNotifyManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyManager.notify(001, mBuilder.build());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void unlockOffers(JSONArray availableOffersJson) {
        Log.d(TAG, "unlockOffers");
        Log.d(TAG, availableOffersJson.toString());

        try {
            for(int i = 0; i < availableOffersJson.length(); i++) {
                JSONObject availableOffer = availableOffersJson.getJSONObject(0);
                Offer unlockedOffer = new Offer(availableOffer);
                Log.d(TAG, "offers/unlock/" + unlockedOffer.getOfferId() + "/user/" + user.getId());
                new SetMineApiGetRequestAsyncTask(this, this)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                                , "offers/unlock/" + unlockedOffer.getOfferId() + "/user/" + user.getId(), "offers/unlock");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // Activity Handling

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // Initialize Date Utils


        JodaTimeAndroid.init(this);

        // For event-based metrics on Mixpanel.com

        initializeMixpanel();

        // Initialize user

        user = new User();

        initializeGimbal();

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

        // checkModelsVersion();

        // Fragment Manager handles all fragments and the navigation between them



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
                currentLocation= null;
            }


        } else {
            Log.d(TAG, "getting instance state");
            userIsRegistered = savedInstanceState.getBoolean("userIsRegistered");
            currentLocation = savedInstanceState.getParcelable("currentLocation");
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Get Application Version from build.gradle

        try {
            APP_VERSION = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Image utilities for smoothly loading and caching images

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheExtraOptions(480, 800, null)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .build();
        ImageLoader.getInstance().init(config);

        // Sets the Activity view for container fragments

        setContentView(R.layout.fragment_main);

        // Custom Action Bar styles

        applyCustomViewStyles();

        // Handle deep links and default view

        handleIntent(getIntent());

        // Remove the loader (may not be necessary anymore)

        getWindow().findViewById(R.id.splash_loading).setVisibility(View.INVISIBLE);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        userIsRegistered = outState.getBoolean("userIsRegistered");
        currentLocation = outState.getParcelable("currentLocation");
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

        } catch (RejectedExecutionException r) {
            r.printStackTrace();
        }
    }

    // Add any initial view changes here

    public void applyCustomViewStyles() {
        LayoutInflater inflater = LayoutInflater.from(this);

        ActionBar actionBar = getActionBar();

        // Use a custom action bar view

        View customView = inflater.inflate(R.layout.custom_action_bar, null);
        actionBar.setCustomView(customView);
        actionBar.setDisplayShowCustomEnabled(true);

        // Only allow keyboard pop up on EditText click

        Log.d(TAG, "applyCustomViewStyles");

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // Required for applying a global custom font style

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    // Click Function for the Home button on the Action Bar

    public void homeButtonPress(View v) {
        openMainViewPager(-1);
    }

    // Click Function for the Back button on the Action Bar

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Log.d(TAG, ((Integer)fragmentManager.getBackStackEntryCount()).toString());
        if(fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
        Log.d(TAG, ((Integer)fragmentManager.getBackStackEntryCount()).toString());

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
        if(playerService.playerManager.getSelectedSet() == null) {
            playSetWithSetID("random");
        } else {
            startPlayerFragment();
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
                playerService.playerManager.selectSetByIndex(0);
                startPlayerFragment();
                playSelectedSet();
            }
        };
        final HttpUtils httpUtil =
                new HttpUtils(this.getApplicationContext(), Constants.API_ROOT_URL);
        final String finalSetId = setID;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String apiRequest = "sets/id/" + finalSetId;
                    String jsonString = httpUtil.getJSONStringFromURL(apiRequest);
                    JSONObject jsonResponse = new JSONObject(jsonString);
                    if (jsonResponse.get("status").equals("success")) {
                        JSONObject setJson = jsonResponse
                                .getJSONObject("payload")
                                .getJSONArray("set")
                                .getJSONObject(0);
                        selectedSet = new Set(setJson);
                        playHandler.post(playSet);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void playSelectedSet() {
        Log.d(TAG, "playSelectedSet");
        sendIntentToService("START_ALL");
    }

    public void openMainViewPager(int pageToScrollTo) {
        Log.d(TAG, "openMainViewPager");
        mainPagerContainerFragment = null;
        mainPagerContainerFragment = new MainPagerContainerFragment();
        Bundle args = new Bundle();
        args.putInt("page", pageToScrollTo);
        mainPagerContainerFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.currentFragmentContainer, mainPagerContainerFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack("mainPagerContainer");
        transaction.commitAllowingStateLoss();
    }

    public void openPlaylistFragment(List<Set> playlist) {
        Log.d(TAG, "openPlaylistFragment");
        PlaylistFragment playlistFragment = new PlaylistFragment();
        Bundle args = new Bundle();
        ArrayList<Set> playlistBundle = new ArrayList<Set>(playlist);
        args.putParcelableArrayList("playlist", playlistBundle);
        playlistFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.currentFragmentContainer, playlistFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack("playlist");
        transaction.commitAllowingStateLoss();
    }

    // Open Player Fragment from anywhere in the app

    public void startPlayerFragment() {
        Log.d(TAG, "startPlayerFragment");
        playerContainerFragment = null;
        playerContainerFragment = new PlayerContainerFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.currentFragmentContainer, playerContainerFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack("player");
        transaction.commitAllowingStateLoss();
    }

    // Open Search Fragment from anywhere in the app

    public void startSearchFragment(View v) {
        Log.d(TAG, "startSearchFragment");
        searchSetsFragment = null;
        searchSetsFragment = new SearchSetsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.currentFragmentContainer, searchSetsFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack("searchSets");
        transaction.commitAllowingStateLoss();
    }

    // Open Artist Detail Fragment from anywhere in the app given a valid Artist object

    public void openArtistDetailPage(String artistName) {
        artistDetailFragment = null;
        artistDetailFragment = new ArtistDetailFragment();
        Bundle args = new Bundle();
        args.putString("currentArtist", artistName);
        artistDetailFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.currentFragmentContainer, artistDetailFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack("artistDetail");
        transaction.commitAllowingStateLoss();
    }

    // Open Event Detail Fragment from anywhere in the app given a valid Offer ID

    public void openOfferDetailFragment(String offerId){
        Log.d(TAG, "openOfferDetailFragment");
        offerDetailFragment = null;
        offerDetailFragment = new OfferDetailFragment();
        Bundle args = new Bundle();
        args.putString("currentOffer", offerId);
        offerDetailFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.currentFragmentContainer, offerDetailFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack("offerDetail");
        transaction.commitAllowingStateLoss();
    }

    // Open Event Detail Fragment from anywhere in the app given a valid Event object

    public void openEventDetailPage(String eventID, String eventType) {
        eventDetailFragment = null;
        eventDetailFragment = new EventDetailFragment();
        Bundle args = new Bundle();
        args.putString("eventID", eventID);
        args.putString("eventType", (eventType.equals("search") ? "upcoming" : eventType));
        eventDetailFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.currentFragmentContainer, eventDetailFragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack("eventDetail");
        transaction.commitAllowingStateLoss();
    }

    // Required to intercept intents for handling

    @Override
    public void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    // Handles incoming intents for opening parts of the app

    public void handleIntent(Intent intent) {

        String command;
        String[] segments;
        try {
            command = intent.getDataString();
            command = Uri.decode(command);
            Log.d(TAG, command);
            segments = command.split("/", 0);
        } catch (Exception e) {
            segments = null;
        }

        // Intents for Playing Sets, Artist Details, Event Details, Remote Controls and the Notification player

        if (intent.getAction().equals("com.setmine.android.OPEN_PLAYER")) {
            startPlayerFragment();
        } else if(intent.getAction().equals("OFFER_UNLOCKED")) {
            Log.d(TAG, "OFFER_UNLOCKED");
            Log.d(TAG, intent.getStringExtra("offer_id"));

            openMainViewPager(-1);
            openOfferDetailFragment(intent.getStringExtra("offer_id"));
        } else if (intent.getAction().equals("android.intent.action.VIEW") && segments[segments.length - 2].equals("?play")) {

            Log.d("track id: ", segments[segments.length - 1]);

            if (segments[segments.length - 2].equals("?play")) {
                playSetWithSetID(segments[segments.length - 1]);
            }
        } else if (intent.getAction().equals("android.intent.action.VIEW") && segments[segments.length - 3].equals("?browse")) {
            if (segments[segments.length - 1].equals("artist")) {

                String artistName = segments[segments.length - 2];
                String[] artistNameArray = artistName.split("\\+", 0);
                artistName = "";
                for (int j = 0; j < artistNameArray.length - 1; j++) {
                    artistName = artistName + artistNameArray[j] + " ";
                }
                artistName = artistName + artistNameArray[artistNameArray.length - 1];
                openArtistDetailPage(artistName);

            } else if (segments[segments.length - 1].equals("festival")) {
                String eventName = segments[segments.length - 2];
                String[] eventNameArray = eventName.split("\\+", 0);
                eventName = "";
                for (int j = 0; j < eventNameArray.length - 1; j++) {
                    eventName = eventName + eventNameArray[j] + " ";
                }
                eventName = eventName + eventNameArray[eventNameArray.length - 1];
                openEventDetailPage(eventName, "recent");

            }
        } else if (intent.getAction().equals("android.intent.action.VIEW") && segments[segments.length - 2].equals("?event")) {
            String eventId = segments[segments.length - 1];
            openEventDetailPage(eventId, "upcoming");


        } else if(intent.getAction().equals("android.intent.action.VIEW") && segments[segments.length - 2].equals("offer")){
            String offerId = segments[segments.length-1];
            openOfferDetailFragment(offerId);
        }
        else {
            openMainViewPager(-1);
        }


    }

    // For communicating with PlayerService

    private void sendIntentToService(String intentAction) {
        Intent playIntent = new Intent(this, PlayerService.class);
        playIntent.setAction(intentAction);
        playIntent.putExtra(intentAction, true);
        sendIntentToService(playIntent);
    }

    public void sendIntentToService(final Intent intent) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                startService(intent);
            }
        }).start();
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
        if(userFragment != null) {
            userFragment.onActivityResult(requestCode, resultCode, data);
        }
        Log.d(TAG, "onActivityResult: " + Integer.toString(requestCode) + " " + Integer.toString(requestCode) + " " + data.toString());
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                }
        }
    }
    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */

    public boolean servicesConnected() {
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

    @Override
    public void onConnected(Bundle bundle) {
        if(locationClient.getLastLocation() != null) {
            currentLocation = locationClient.getLastLocation();
        }
        else {
            currentLocation = null;
        }
        locationClient.disconnect();

    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected");
        currentLocation = null;
    }

    // Google Play Services connection failed

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        currentLocation = null;
        locationClient.disconnect();
    }



}
