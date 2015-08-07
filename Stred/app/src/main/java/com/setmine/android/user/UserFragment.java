package com.setmine.android.user;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.setmine.android.Constants;
import com.setmine.android.MainPagerContainerFragment;
import com.setmine.android.ModelsContentProvider;
import com.setmine.android.Offer.Offer;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.api.Activity;
import com.setmine.android.api.SetMineApiGetRequestAsyncTask;
import com.setmine.android.api.SetMineApiPostRequestAsyncTask;
import com.setmine.android.artist.Artist;
import com.setmine.android.event.Event;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.set.Set;
import com.setmine.android.util.DateUtils;
import com.setmine.android.util.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by oscarlafarga on 12/12/14.
 */

public class UserFragment extends Fragment implements ApiCaller {

    // Statics
    public static String ARG_OBJECT = "page";
    private static final String TAG = "UserFragment";
    private static final List<String> PERMISSIONS = Arrays.asList(
            "public_profile", "email", "user_friends");

    // Views
    public View rootView;

    // Models
    public  List<Activity> userActivities;
    public JSONObject jsonUser;
    public User registeredUser;

    // Locals
    public DateUtils dateUtils;
    public SetMineMainActivity activity;
    private int timeID;
    public DisplayImageOptions options;

    // Empty constructor required for Fragments in a ViewPager

    public UserFragment() {}

    // Facebook Integration - Control the UI depending on Facebook Login Status

    private UiLifecycleHelper facebookUiHelper;
    private Session.StatusCallback facebookCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState sessionState, Exception e) {
            onSessionStateChange(session, sessionState, e);
        }
    };

    // For handling the successfully retrieved registered SetMine Useru

    final Handler userHandler = new Handler();
    private final UserFragment runnableUserFragmentTarget = this;

    final Runnable updateActivities = new Runnable() {
        @Override
        public void run() {
            populateActivities();
        }
    };

    final Runnable updateNewSets = new Runnable() {
        @Override
        public void run() {
            populateNewSets();
        }
    };

    final Runnable updateNewOffers = new Runnable() {
        @Override
        public void run() {
            kickOffNewSetsQuery();
            populateNewOffers();
        }
    };

    final Runnable updateMyNextEvent = new Runnable() {
        @Override
        public void run() {
            populateMyNextEvent();
        }
    };

    final Runnable updateMySets = new Runnable() {
        @Override
        public void run() {
            populateMySets();
        }
    };

    final Runnable handleRegisteredUser = new Runnable() {
        public void run() {
            activity = (SetMineMainActivity)getActivity();
            activity.user = registeredUser;
            Log.d(TAG, "handleRegisteredUser");
            registerMixpanelUser();
            generateUserHomePage();
            rootView.findViewById(R.id.centered_loader_container).setVisibility(View.GONE);
            if(activity.offerDetailFragment != null) {
                activity.offerDetailFragment.refreshUnlockStatus();
            }
        }
    };

    @Override
    public void onApiResponseReceived(JSONObject jsonObject, String identifier) {
        final JSONObject finalJsonObject = jsonObject;
        final String finalIdentifier = identifier;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onApiResponseReceived: " + finalIdentifier);
                if (finalIdentifier.equals("updateUserSets")) {
                    try {
                        registeredUser.setFavoriteSets(finalJsonObject.getJSONObject("payload").getJSONObject("user"));
                        userHandler.post(updateMySets);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (finalIdentifier.equals("myNextEvent")) {
                    try {
                        registeredUser.setNextEvent(finalJsonObject.getJSONObject("payload").getJSONObject("user"));
                        userHandler.post(updateMyNextEvent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (finalIdentifier.equals("newSets")) {
                    try {
                        registeredUser.setNewSets(finalJsonObject.getJSONObject("payload").getJSONArray("user"));
                        userHandler.post(updateNewSets);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (finalIdentifier.equals("newOffers")) {
                    try {
                        registeredUser.setNewOffers(finalJsonObject.getJSONObject("payload").getJSONArray("offer"));
                        userHandler.post(updateNewOffers);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (finalIdentifier.equals("activities")) {
                    userActivities = ModelsContentProvider.createModel(finalJsonObject, finalIdentifier);
                    userHandler.post(updateActivities);
                }
            }
        }).start();
        
    }

    @Override
    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);
        this.activity = (SetMineMainActivity) activity;
        this.activity.userFragment = this;
        timeID = getResources().getIdentifier("com.setmine.android:drawable/recent_icon", null, null);
    }

    // Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        facebookUiHelper = new UiLifecycleHelper(getActivity(), facebookCallback);
        facebookUiHelper.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
//            userLocation = new Location("default");
            registeredUser = new User();

        } else {
//            Location userLocation = new Location("default");
//            userLocation.setLatitude(savedInstanceState.getDouble("latitude"));
//            userLocation.setLongitude(savedInstanceState.getDouble("longitude"));
            String userModel = savedInstanceState.getString("user");

            try {
                JSONObject jsonUser = new JSONObject(userModel);
                registeredUser = new User(jsonUser);
//                registeredUser.setLocation(userLocation);
            } catch (Exception e) {

            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_user, container, false);
        rootView.findViewById(R.id.centered_loader_container).setVisibility(View.VISIBLE);

        // Set permissions necessary for Facebook Login Prompt

        ((LoginButton) rootView.findViewById(R.id.facebookLoginButton)).setReadPermissions(PERMISSIONS);

        if(!registeredUser.isRegistered()) {
            generateLoginPage();
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        facebookUiHelper.onResume();
        Log.d(TAG, "onResume");
        Session currentSession = Session.getActiveSession();
        if (currentSession != null &&
                (currentSession.isOpened() || currentSession.isClosed())) {
            onSessionStateChange(currentSession, currentSession.getState(), null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookUiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        facebookUiHelper.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        facebookUiHelper.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        facebookUiHelper.onSaveInstanceState(outState);
        outState.putString("user", registeredUser.jsonModelString);
//        outState.putDouble("latitude", userLocation.getLatitude());
//        outState.putDouble("longitude", userLocation.getLongitude());

    }

    // Facebook Session Handling

    private void onSessionStateChange(Session session, SessionState state, Exception e) {
        Log.d(TAG, "onSessionStateChange");
        if (state.isOpened() && !registeredUser.isRegistered()) {
            Log.d(TAG, "Logged in.");
            rootView.findViewById(R.id.centered_loader_container).setVisibility(View.VISIBLE);
            authenticateFacebookUser();
            ((MainPagerContainerFragment)getParentFragment()).mViewPager.setCurrentItem(0);
        } else if (state.isClosed()) {
            Log.d(TAG, "Logged out.");
            jsonUser = null;
            registeredUser = new User();
            activity.user = new User();
            generateLoginPage();
        }
    }

    // Authenticate SetMine User with Facebook credentials

    public void authenticateFacebookUser() {
        new Thread(new Runnable() {
            public void run() {
                try {

                    Log.d(TAG, "authenticating");

                    // Build the JSON data for the POST request

                    String token = Session.getActiveSession().getAccessToken();
                    JSONObject jsonFBToken = new JSONObject();
                    JSONObject jsonUserData = new JSONObject();
                    JSONObject jsonPostData = new JSONObject();
                    jsonFBToken.put("accessToken", token);
                    jsonFBToken.put("userID", "No FB ID Provided");
                    jsonUserData.put("FB_TOKEN", jsonFBToken);
                    jsonPostData.put("userData", jsonUserData);
                    String jsonPostDataString = jsonPostData.toString();
                    String route = "user/facebookRegister";

                    // Create utility for sending the request

                    HttpUtils apiCallerUtil =
                            new HttpUtils(getActivity(), Constants.API_ROOT_URL);
                    String jsonString = apiCallerUtil.postApiRequest(route, jsonPostDataString);
                    Log.d(TAG, jsonString);
                    JSONObject jsonResponseObject = new JSONObject(jsonString);
                    if (jsonResponseObject.get("status").equals("success")) {
                        jsonUser = jsonResponseObject
                                .getJSONObject("payload")
                                .getJSONObject("user");
                        registeredUser = new User(jsonUser);
                        userHandler.post(handleRegisteredUser);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void registerMixpanelUser() {
        MixpanelAPI.People people = ((SetMineMainActivity)getActivity()).mixpanel.getPeople();
        ((SetMineMainActivity)getActivity()).mixpanel.identify(registeredUser.getFacebookID());
        people.identify(registeredUser.getFacebookID());
        people.setOnce("First Name", registeredUser.getFirstName());
        people.setOnce("Last Name", registeredUser.getLastName());
        people.setOnce("Facebook ID", registeredUser.getFacebookID());
        people.setOnce("$email", registeredUser.getEmail());
    }

    // Generate Home/Login pages with user information

    public void generateUserHomePage() {
        Log.d(TAG, "generateUserHomePage");

        // Move the newly converted Facebook UI Button to logout container
        LoginButton loginButton = (LoginButton) rootView.findViewById(R.id.facebookLoginButton);
        ViewGroup parent = (ViewGroup) loginButton.getParent();
        parent.removeView(loginButton);
        ViewGroup newParent = (ViewGroup) rootView.findViewById(R.id.facebookLogoutContainer);
        newParent.addView(loginButton);

        // Toggle to home container view

        rootView.findViewById(R.id.loginContainer).setVisibility(View.GONE);
        rootView.findViewById(R.id.homeContainer).setVisibility(View.VISIBLE);

        // For changing the title from "Login" to "Home"

        ((MainPagerContainerFragment) getParentFragment()).mMainPagerAdapter.TITLES[0] = "Home";

        assignClickListeners();
        rootView.findViewById(R.id.centered_loader_container).setVisibility(View.GONE);

    }

    public void generateLoginPage() {
        Log.d(TAG, "generateLoginHomePage");

        // Move the newly converted Facebook UI Button to login container
        LoginButton loginButton = (LoginButton) rootView.findViewById(R.id.facebookLoginButton);
        ViewGroup parent = (ViewGroup) loginButton.getParent();
        parent.removeView(loginButton);
        ViewGroup newParent = (ViewGroup) rootView.findViewById(R.id.facebookLoginContainer);
        if(newParent != null) {
            Log.d(TAG, "not null newParent");
            newParent.addView(loginButton);

        }

        // Toggle to login container view

        rootView.findViewById(R.id.homeContainer).setVisibility(View.GONE);
        rootView.findViewById(R.id.loginContainer).setVisibility(View.VISIBLE);

        ((MainPagerContainerFragment) getParentFragment()).mMainPagerAdapter.TITLES[0] = "Login";

        rootView.findViewById(R.id.centered_loader_container).setVisibility(View.GONE);

    }

    public void assignClickListeners() {
        rootView.findViewById(R.id.mySetsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when play is clicked show stop button and hide play button
                rootView.findViewById(R.id.iconsLayout).setVisibility(View.GONE);
                rootView.findViewById(R.id.mySetsDetail).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                populateMySets();

            }
        });
        rootView.findViewById(R.id.newSetsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show New Sets
                rootView.findViewById(R.id.iconsLayout).setVisibility(View.GONE);
                rootView.findViewById(R.id.newSetsDetail).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                kickOffNewOffersQuery();

            }
        });
        rootView.findViewById(R.id.activitiesButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show Activities
                rootView.findViewById(R.id.iconsLayout).setVisibility(View.GONE);
                rootView.findViewById(R.id.activitiesDetail).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                kickOffActivitiesQuery();

            }
        });
        rootView.findViewById(R.id.myNextEventsButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show My Next Events
                rootView.findViewById(R.id.iconsLayout).setVisibility(View.GONE);
                rootView.findViewById(R.id.myEventsDetail).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                kickOffNextEventQuery();


            }
        });
    }

    // Create the Activity tiles after activities have been stored in the Models Content Provider

    public void populateActivities() {

        if (userActivities != null) {


            // Get the inflater for inflating XML files into Views

            LayoutInflater inflater = LayoutInflater.from(activity);

            // Remove all views inside the layout container

            ((ViewGroup) rootView.findViewById(R.id.activitiesTileContainer)).removeAllViews();

            // Remove the loader

            rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);

            // Inflate a activity tile for every activity

            for (int i = 0; i < userActivities.size(); i++) {
                final List<Set> activitySets = userActivities.get(i).getSets();
                final View activityTile = inflater.inflate(R.layout.activity_tile, null);
                ((TextView) activityTile.findViewById(R.id.activityName))
                        .setText(userActivities.get(i).getActivityName());

                // Set the click listener for playing a random set

                activityTile.findViewById(R.id.activityPlay).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Random rand = new Random();
                        int randomIndex = rand.nextInt(activitySets.size());
                        activity.playerService.playerManager.setPlaylist(activitySets);
                        activity.playerService.playerManager.selectSetByIndex(randomIndex);
                        activity.startPlayerFragment();
                        activity.playSelectedSet();
                    }
                });

                // Set the click listener for browsing all the sets in an activity

                activityTile.findViewById(R.id.activityViewAllSets).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.openPlaylistFragment(activitySets);
//                        activity.playlistFragment.updatePlaylist();
//                        activity.playerService.playerContainerFragment.mViewPager.setCurrentItem(0);
                    }
                });

                // Options for ImageLoader

                options = new DisplayImageOptions.Builder()
                        .showImageOnLoading(R.drawable.logo_small)
                        .showImageForEmptyUri(R.drawable.logo_small)
                        .showImageOnFail(R.drawable.logo_small)
                        .cacheInMemory(false)
                        .cacheOnDisk(false)
                        .considerExifParams(true)
                        .build();

                // Load the activity image

                final ImageView activityImage = ((ImageView) activityTile.findViewById(R.id.activityImage));
                ImageLoader.getInstance()
                        .loadImage(userActivities.get(i).getImageURL(),
                                options, new SimpleImageLoadingListener() {
                                    @Override
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                        activityImage.setImageDrawable(new BitmapDrawable(activity.getResources(), loadedImage));
                                    }
                                });

                // Add each activity tile to the parent container

                ((ViewGroup) rootView.findViewById(R.id.activitiesTileContainer)).addView(activityTile);
            }
        }

    }

    private void kickOffActivitiesQuery() {
        String activitiesQuery = "activity?all=true";

        new SetMineApiGetRequestAsyncTask(activity, runnableUserFragmentTarget)
                .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                        activitiesQuery, "activities");
    }

    private void kickOffNextEventQuery() {
        String myNextEventQuery = "user/myNextEvent";
        myNextEventQuery += "?userID=" + registeredUser.getId();

//        if (userLocation != null) {
//            myNextEventQuery += "&latitude=" + userLocation.getLatitude();
//            myNextEventQuery += "&longitude=" + userLocation.getLongitude();
//        }
        new SetMineApiGetRequestAsyncTask(activity, runnableUserFragmentTarget)
                .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                        myNextEventQuery, "myNextEvent");
    }

    private void kickOffNewSetsQuery() {
        String myNewSetsQuery = "user/newSets?userID=" + registeredUser.getId();
        new SetMineApiGetRequestAsyncTask(activity, runnableUserFragmentTarget)
                .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                        myNewSetsQuery, "newSets");
    }

    private void kickOffNewOffersQuery() {
        String myNewOffersQuery = "offers/user/1";

//        String myNewOffersQuery = "offers/id/1"+registeredUser.getId();

        new SetMineApiGetRequestAsyncTask(activity, runnableUserFragmentTarget)
                .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                        myNewOffersQuery, "newOffers");
    }

    // Create the My Next Event Tile after upcomingEvents have been stored in Models CP

    public void populateMyNextEvent() {
        final Event myNextEvent = registeredUser.getNextEvent();

        // Get the inflater for inflating XML files into Views

        LayoutInflater inflater = LayoutInflater.from(activity);

        // Remove all views inside the layout container

        ((ViewGroup) rootView.findViewById(R.id.myEventsTileContainer)).removeAllViews();

        // Remove the loader

        rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);

        // Inflate an upcoming event tile for My Next Event

        View myNextEventTile = inflater.inflate(R.layout.event_tile_upcoming, null);

        dateUtils = new DateUtils();


        // Set the text on all the relevant information of the event

        ((TextView) myNextEventTile.findViewById(R.id.city))
                .setText(dateUtils.getCityStateFromAddress(myNextEvent.getAddress()));
        ((TextView) myNextEventTile.findViewById(R.id.event))
                .setText(myNextEvent.getEvent());
        ((TextView) myNextEventTile.findViewById(R.id.date))
                .setText(dateUtils.formatDateText(myNextEvent.getStartDate(), myNextEvent.getEndDate()));
        myNextEventTile.findViewById(R.id.node).setVisibility(View.GONE);

        // Set the click listener for opening the event detail page

        myNextEventTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openEventDetailPage(myNextEvent.getId(), "upcoming");
            }
        });

        // Options for ImageLoader

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .considerExifParams(true)
                .build();

        // Load the event image

        final ImageView myNextEventImage = ((ImageView) myNextEventTile.findViewById(R.id.image));
        ImageLoader.getInstance()
                .loadImage(myNextEvent.getMainImageUrl(), options, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        myNextEventImage.setImageDrawable(new BitmapDrawable(activity.getResources(), loadedImage));
                    }
                });

        // Add the event tile to the container

        ((ViewGroup) rootView.findViewById(R.id.myEventsTileContainer)).addView(myNextEventTile);

    }

    // Create the My Sets tiles after the user has been authenticated and created

    public void populateMySets() {
        final List<Set> favoriteSets = registeredUser.getFavoriteSets();

        if(favoriteSets != null) {
            // Get the inflater for inflating XML files into Views

            LayoutInflater inflater = LayoutInflater.from(activity);

            // Remove all views inside the layout container

            ((ViewGroup)rootView.findViewById(R.id.mySetsTilesContainer)).removeAllViews();

            // Remove the loader

            rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);

            // If the user has not favorited any sets

            if(favoriteSets.size() == 0) {
                Log.d(TAG, "No Favorite Sets");
                TextView noFavoriteSets = (TextView) inflater.inflate(R.layout.no_results_tile, null);
                noFavoriteSets.setText("You haven't favorited any sets yet! To add one to the list, search for a set and click the star icon while it's playing.");
                ((ViewGroup) rootView.findViewById(R.id.mySetsTilesContainer)).addView(noFavoriteSets);
            }
            for(int i = 0 ; i < favoriteSets.size() ; i++) {
                Set set = favoriteSets.get(i);
                View mySetTile = inflater.inflate(R.layout.set_tile, null);

                ((TextView) mySetTile.findViewById(R.id.artistText))
                        .setText(set.getArtist());
                ((TextView) mySetTile.findViewById(R.id.eventText))
                        .setText(set.getEvent());
                ((TextView) mySetTile.findViewById(R.id.playCount))
                        .setText(set.getPopularity() + " plays");
                ((TextView) mySetTile.findViewById(R.id.setLength))
                        .setText(set.getSetLength());

                mySetTile.setTag(set);

                mySetTile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.playerService.playerManager.setPlaylist(favoriteSets);
                        activity.playerService.playerManager.selectSetById(((Set) v.getTag()).getId());
                        activity.startPlayerFragment();
                        activity.playSelectedSet();
                    }
                });

                final ImageView artistImage = (ImageView) mySetTile.findViewById(R.id.artistImage);

                // Options for ImageLoader

                options = new DisplayImageOptions.Builder()
                        .showImageOnLoading(R.drawable.logo_small)
                        .showImageForEmptyUri(R.drawable.logo_small)
                        .showImageOnFail(R.drawable.logo_small)
                        .cacheInMemory(false)
                        .cacheOnDisk(false)
                        .considerExifParams(true)
                        .build();

                ImageLoader.getInstance()
                        .loadImage(set.getArtistImage(),
                                options, new SimpleImageLoadingListener() {
                                    @Override
                                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                        artistImage.setImageDrawable(new BitmapDrawable(activity.getResources(), loadedImage));
                                    }
                                });

                ((ViewGroup)rootView.findViewById(R.id.mySetsTilesContainer)).addView(mySetTile);
            }
        }


    }

    // Add or Remove a set from My Sets

    public void updateFavoriteSets(String setID) {
        try {
            JSONObject jsonUserData = new JSONObject();
            JSONObject jsonPostData = new JSONObject();
            jsonUserData.put("username", registeredUser.getId());
            jsonUserData.put("setId", setID);
            jsonPostData.put("userData", jsonUserData);
            SetMineApiPostRequestAsyncTask updateFavoriteSetsTask =
                    new SetMineApiPostRequestAsyncTask(activity, this);
            updateFavoriteSetsTask
                    .executeOnExecutor(SetMineApiPostRequestAsyncTask.THREAD_POOL_EXECUTOR,
                            "user/updateFavoriteSets", jsonPostData.toString(), "updateUserSets");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void populateNewSets() {
        final List<Set> newSets = registeredUser.getNewSets();

        // Get the inflater for inflating XML files into Views

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        // Remove all views inside the layout container

 //       ((ViewGroup) rootView.findViewById(R.id.newSetsDetail)).removeAllViews();


        // If the user has not favorited any sets

        if (newSets.size() == 0) {
            Log.d(TAG, "No New Sets");
            TextView noNewSets = (TextView) inflater.inflate(R.layout.no_results_tile, null);
            noNewSets.setText("You have no new sets yet... Favorite some sets so we know what you like!");
            ((ViewGroup) rootView.findViewById(R.id.newSetsDetail)).addView(noNewSets);
        }
        for (int i = 0; i < newSets.size(); i++) {
            Set set = newSets.get(i);
            View mySetTile = inflater.inflate(R.layout.set_tile, null);

            ((TextView) mySetTile.findViewById(R.id.artistText))
                    .setText(set.getArtist());
            ((TextView) mySetTile.findViewById(R.id.eventText))
                    .setText(set.getEvent());
            ((TextView) mySetTile.findViewById(R.id.playCount))
                    .setText((new DateUtils()).convertDateToDaysAgo(set.getDatetime()));
            ((TextView) mySetTile.findViewById(R.id.setLength))
                    .setText(set.getSetLength());


            ((ImageView) mySetTile.findViewById(R.id.playsIcon)).setImageResource(timeID);

            mySetTile.setTag(set);

            mySetTile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.playerService.playerManager.setPlaylist(newSets);
                    activity.playerService.playerManager.selectSetById(((Set) v.getTag()).getId());
                    activity.startPlayerFragment();
                    activity.playSelectedSet();
                }
            });

            final ImageView artistImage = (ImageView) mySetTile.findViewById(R.id.artistImage);

            // Options for ImageLoader

            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.logo_small)
                    .showImageForEmptyUri(R.drawable.logo_small)
                    .showImageOnFail(R.drawable.logo_small)
                    .cacheInMemory(false)
                    .cacheOnDisk(false)
                    .considerExifParams(true)
                    .build();

            ImageLoader.getInstance()
                    .loadImage(set.getArtistImage(),
                            options, new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    artistImage.setImageDrawable(new BitmapDrawable(activity.getResources(), loadedImage));
                                }
                            });


            ((ViewGroup) rootView.findViewById(R.id.newSetsDetail)).addView(mySetTile);


            // Remove the loader
            rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
        }


    }


    public void populateNewOffers() {
        final List<Offer> newOffers = registeredUser.getNewOffers();

        Log.d(TAG, Integer.toString(newOffers.size()));

        // Get the inflater for inflating XML files into Views

        LayoutInflater inflater = LayoutInflater.from(getActivity());

        // Remove all views inside the layout container


        if (newOffers.size() == 0) {
            Log.d(TAG, "No New Offers");
        }
        for (int j = 0; j < newOffers.size(); j++) {
            final Offer offer = newOffers.get(j);
            View myOfferTile = inflater.inflate(R.layout.offer_tile, null);

            Artist offerArtist = offer.getArtist();

            TextView offerTileArtist = ((TextView) myOfferTile.findViewById(R.id.offerTileArtist));
            final ImageView artistImage = ((ImageView) myOfferTile.findViewById(R.id.offerArtistImage));

            offerTileArtist.setText(offerArtist.getArtist());
            offerTileArtist.setTag(offerArtist.getArtist());
            View.OnClickListener goToArtistDetail = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SetMineMainActivity)getActivity()).openArtistDetailPage((String)v.getTag());
                }
            };
            offerTileArtist.setOnClickListener(goToArtistDetail);
            Log.d(TAG, offer.toString());
            ((TextView) myOfferTile.findViewById(R.id.offerVenueText))
                    .setText(offer.getVenues().get(0).getVenueName());

            artistImage.setTag(offerArtist);
            artistImage.setOnClickListener(goToArtistDetail);
            // Options for ImageLoader

            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.logo_small)
                    .showImageForEmptyUri(R.drawable.logo_small)
                    .showImageOnFail(R.drawable.logo_small)
                    .cacheInMemory(false)
                    .cacheOnDisk(false)
                    .considerExifParams(true)
                    .build();
            ImageLoader.getInstance()
                    .loadImage(offerArtist.getImageUrl(), options, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            artistImage.setImageDrawable(new BitmapDrawable(getActivity().getResources(), loadedImage));
                        }
                    });

            myOfferTile.setTag(offer);

            myOfferTile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((SetMineMainActivity)getActivity()).openOfferDetailFragment(offer.getOfferId());

                }
            });

            ((ViewGroup) rootView.findViewById(R.id.newSetsDetail)).addView(myOfferTile);
        }
        // Remove the loader
        rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

}
