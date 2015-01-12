package com.setmine.android.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.setmine.android.ApiCaller;
import com.setmine.android.ModelsContentProvider;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.object.Activity;
import com.setmine.android.object.Set;
import com.setmine.android.object.User;
import com.setmine.android.task.JsonApiCallAsyncTask;
import com.setmine.android.util.DateUtils;
import com.setmine.android.util.HttpUtils;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by oscarlafarga on 12/12/14.
 */
public class UserFragment extends Fragment implements ApiCaller {

    public static final String ARG_OBJECT = "page";

    public LoginButton loginButton;
    public ModelsContentProvider modelsCP;
    public ViewPager eventViewPager;
    public DateUtils dateUtils;
    public SetMineMainActivity activity;
    public JSONObject jsonUser;
    public User registeredUser;

    final Handler userHandler = new Handler();

    final Runnable handleRegisteredUser = new Runnable() {
        public void run() {
            populateMySets();
        }
    };

    public boolean loggedIn = false;


    public View homeView;
    public View loginView;
    public View rootView;
    public View activitiesContainer;
    public View mySetsContainer;


    public DisplayImageOptions options;

    private static final String TAG = "UserFragment";
    private static final List<String> PERMISSIONS = Arrays.asList(
            "public_profile", "email", "user_friends");

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

    // Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        activity = (SetMineMainActivity)getActivity();
        activity.userFragment = this;
        facebookUiHelper = new UiLifecycleHelper(activity, facebookCallback);
        facebookUiHelper.onCreate(savedInstanceState);

        // Options for ImageLoader

        options =  new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_user, container, false);
        activitiesContainer = rootView.findViewById(R.id.activityTilesContainer);
        mySetsContainer = rootView.findViewById(R.id.mySetTilesContainer);

        loginButton = (LoginButton)rootView.findViewById(R.id.facebookLoginButton);
        loginButton.setReadPermissions(PERMISSIONS);
        if(activity.modelsCP.getActivities().size() == 0) {
            new JsonApiCallAsyncTask(activity, this)
                    .executeOnExecutor(JsonApiCallAsyncTask.THREAD_POOL_EXECUTOR, "activity?all=true", "activities");
        }
        else {
            populateActivities();
        }
        if(activity.userIsRegistered) {
            Log.d(TAG, "registered");
            rootView.findViewById(R.id.loginContainer).setVisibility(View.GONE);
            rootView.findViewById(R.id.homeContainer).setVisibility(View.VISIBLE);
        }
        else {
            rootView.findViewById(R.id.homeContainer).setVisibility(View.GONE);
            rootView.findViewById(R.id.loginContainer).setVisibility(View.VISIBLE);
            Log.d(TAG, "not registered");
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        facebookUiHelper.onResume();
        Log.d(TAG, "onResume");
        Session currentSession = Session.getActiveSession();
        Log.d(TAG, currentSession.toString());
        if (currentSession != null &&
                (currentSession.isOpened() || currentSession.isClosed()) ) {
            onSessionStateChange(currentSession, currentSession.getState(), null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: "+Session.getActiveSession().getPermissions());
        super.onActivityResult(requestCode, resultCode, data);
        facebookUiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        facebookUiHelper.onPause();
        Log.d(TAG, "ONPAUSE");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        facebookUiHelper.onDestroy();
        Log.d(TAG, "ONDESTROY");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        facebookUiHelper.onSaveInstanceState(outState);
    }

    // Facebook Session Handling

    private void onSessionStateChange(Session session, SessionState state, Exception e) {

        if(state.isOpened() && !loggedIn) {
            Log.d(TAG, "Logged in.");
            loggedIn = true;
            if(jsonUser == null) {
                authenticateFacebookUser();
                generateUserHomePage();
            }
        } else if(state.isClosed()) {
            Log.d(TAG, "Logged out.");
            jsonUser = null;
            generateLoginPage();
        }
    }

    // Authenticate SetMine User with Facebook credentials

    public void authenticateFacebookUser() {
        new Thread(new Runnable() {
            public void run() {
                try {

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
                            new HttpUtils(activity.getApplicationContext(), activity.API_ROOT_URL);
                    String jsonString = apiCallerUtil.postApiRequest(route, jsonPostDataString);
                    JSONObject jsonResponseObject = new JSONObject(jsonString);
                    if(jsonResponseObject.get("status").equals("success")) {
                        jsonUser = jsonResponseObject
                                .getJSONObject("payload")
                                .getJSONObject("user");
                        registeredUser = new User(jsonUser);
                        userHandler.post(handleRegisteredUser);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    // Generate Home/Login pages with user information

    public void generateUserHomePage() {
        ViewGroup parent = (ViewGroup)loginButton.getParent();
        parent.removeView(loginButton);
        ViewGroup newParent = (ViewGroup)rootView.findViewById(R.id.facebookLogoutContainer);
        newParent.addView(loginButton);

        rootView.findViewById(R.id.loginContainer).setVisibility(View.GONE);
        rootView.findViewById(R.id.homeContainer).setVisibility(View.VISIBLE);

        activity.mainViewPagerContainerFragment.mMainPagerAdapter.TITLES[0] = "Home";
    }

    public void generateLoginPage() {
        ViewGroup parent = (ViewGroup)loginButton.getParent();
        parent.removeView(loginButton);
        ViewGroup newParent = (ViewGroup)rootView.findViewById(R.id.facebookLoginContainer);
        newParent.addView(loginButton);

        rootView.findViewById(R.id.homeContainer).setVisibility(View.GONE);
        rootView.findViewById(R.id.loginContainer).setVisibility(View.VISIBLE);

        Log.d(TAG, "Titles: " + activity.mainViewPagerContainerFragment.mMainPagerAdapter.TITLES[0]);

        activity.mainViewPagerContainerFragment.mMainPagerAdapter.TITLES[0] = "Login";

        Log.d(TAG, "Titles: " + activity.mainViewPagerContainerFragment.mMainPagerAdapter.TITLES[0]);


    }

    // Create the activity tiles after activities have been stored in the Models Content Provider

    public void populateActivities() {
        final List<Activity> userActivities = activity.modelsCP.getActivities();
        LayoutInflater inflater = LayoutInflater.from(activity);
        ((ViewGroup)activitiesContainer).removeAllViews();
        for(int i = 0 ; i < userActivities.size() ; i++) {
            final List<Set> activitySets = userActivities.get(i).getSets();
            final View activityTile = inflater.inflate(R.layout.activity_tile, null);
            ((TextView)activityTile.findViewById(R.id.activityName))
                    .setText(userActivities.get(i).getActivityName());

            activityTile.findViewById(R.id.activityPlay).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.setsManager.setPlaylist(activitySets);
                    activity.playlistFragment.updatePlaylist();
                    Random r = new Random();
                    int randomInt = r.nextInt(activity.setsManager.getPlaylist().size() - 1);
                    Set s = activity.setsManager.getPlaylist().get(randomInt);
                    activity.startPlayerFragment(Integer.parseInt(s.getId()));
                }
            });

            activityTile.findViewById(R.id.activityViewAllSets).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Activity View All");
                }
            });

            final ImageView activityImage = ((ImageView)activityTile.findViewById(R.id.activityImage));
            ImageLoader.getInstance()
                    .loadImage(userActivities.get(i).getImageURL(),
                            options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    activityImage.setImageDrawable(new BitmapDrawable(activity.getResources(), loadedImage));
                }
            });

            ((ViewGroup)activitiesContainer).addView(activityTile);
        }
    }

    public void populateMySets() {
        final List<Set> favoriteSets = registeredUser.getFavoriteSets();
        LayoutInflater inflater = LayoutInflater.from(activity);
        ((ViewGroup)mySetsContainer).removeAllViews();
        if(favoriteSets.size() == 0) {
            Log.d(TAG, "No Favorite Sets");
            TextView noFavoriteSets = (TextView) inflater.inflate(R.layout.no_results_tile, null);
            noFavoriteSets.setText("You haven't favorited any sets yet! Add some using the star icon.");
            ((ViewGroup) mySetsContainer).addView(noFavoriteSets);
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

            mySetTile.setTag(set);

            mySetTile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.setsManager.setPlaylist(favoriteSets);
                    activity.playlistFragment.updatePlaylist();
                    activity.startPlayerFragment(Integer.parseInt(((Set) v.getTag()).getId()));
                }
            });

            final ImageView artistImage = (ImageView) mySetTile.findViewById(R.id.artistImage);

            ImageLoader.getInstance()
                    .loadImage(SetMineMainActivity.S3_ROOT_URL + set.getArtistImage(),
                            options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    artistImage.setImageDrawable(new BitmapDrawable(activity.getResources(), loadedImage));
                }
            });

            ((ViewGroup)mySetsContainer).addView(mySetTile);
        }
    }

    @Override
    public void onApiResponseReceived(JSONObject jsonObject, String identifier) {
        if(identifier == "activities") {
            activity.modelsCP.setModel(jsonObject, "activities");
            rootView.findViewById(R.id.activitiesLoading).setVisibility(View.GONE);
            populateActivities();
        }
    }
}
