package com.setmine.android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.setmine.android.ModelsContentProvider;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.util.DateUtils;
import com.setmine.android.util.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by oscarlafarga on 12/12/14.
 */
public class UserFragment extends Fragment {

    public static final String ARG_OBJECT = "page";

    public LoginButton loginButton;
    public ModelsContentProvider modelsCP;
    public ViewPager eventViewPager;
    public DateUtils dateUtils;
    public DisplayImageOptions options;
    public SetMineMainActivity activity;
    public JSONObject jsonUser;

    public View homeView;
    public View loginView;
    public View rootView;
    public View activitiesContainer;

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

    // Facebook Session Handling

    private void onSessionStateChange(Session session, SessionState state, Exception e) {
        Log.d(TAG, "onSessionStateChanged");
        Log.d(TAG, session.toString());
        Log.d(TAG, state.toString());
        if(state.isOpened()) {
            Log.d(TAG, "Logged in.");
            if(jsonUser == null) {
                authenticateFacebookUser();
                generateHomePage();
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

                    // Build JSON data for the POST request

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
                        Log.d(TAG, jsonUser.toString());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    // Generate Home/Login pages with user information

    public void generateHomePage() {
        ViewGroup parent = (ViewGroup)loginButton.getParent();
        parent.removeView(loginButton);
        ViewGroup newParent = (ViewGroup)rootView.findViewById(R.id.facebookLogoutContainer);
        newParent.addView(loginButton);

        rootView.findViewById(R.id.loginContainer).setVisibility(View.GONE);
        rootView.findViewById(R.id.homeContainer).setVisibility(View.VISIBLE);

        activity.mainViewPagerContainerFragment.mEventPagerAdapter.TITLES[0] = "Home";
        if(jsonUser != null) {
            populateMySets(jsonUser);
        }
    }

    public void generateLoginPage() {
        ViewGroup parent = (ViewGroup)loginButton.getParent();
        parent.removeView(loginButton);
        ViewGroup newParent = (ViewGroup)rootView.findViewById(R.id.facebookLoginContainer);
        newParent.addView(loginButton);

        rootView.findViewById(R.id.homeContainer).setVisibility(View.GONE);
        rootView.findViewById(R.id.loginContainer).setVisibility(View.VISIBLE);

        Log.d(TAG, "Titles: " + activity.mainViewPagerContainerFragment.mEventPagerAdapter.TITLES[0]);

        activity.mainViewPagerContainerFragment.mEventPagerAdapter.TITLES[0] = "Login";

        Log.d(TAG, "Titles: " + activity.mainViewPagerContainerFragment.mEventPagerAdapter.TITLES[0]);


    }

    // Get sets with set ids

    public void populateMySets(JSONObject registeredUser) {
        try {
            JSONArray favoriteSets = registeredUser.getJSONArray("favorite_sets_full");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Lifecycle Methods

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        activity = (SetMineMainActivity)getActivity();
        activity.userFragment = this;
        facebookUiHelper = new UiLifecycleHelper(activity, facebookCallback);
        facebookUiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_user, container, false);
        activitiesContainer = rootView.findViewById(R.id.activityTilesContainer);

        loginButton = (LoginButton)rootView.findViewById(R.id.facebookLoginButton);
        loginButton.setReadPermissions(PERMISSIONS);
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
}
