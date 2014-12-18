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

/**
 * Created by oscarlafarga on 12/12/14.
 */
public class UserFragment extends Fragment {

    public static final String ARG_OBJECT = "page";
    public View rootView;
    public ModelsContentProvider modelsCP;
    public ViewPager eventViewPager;
    public DateUtils dateUtils;
    public DisplayImageOptions options;
    public SetMineMainActivity activity;

    private static final String TAG = "UserFragment";

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (SetMineMainActivity)getActivity();

        facebookUiHelper = new UiLifecycleHelper(activity, facebookCallback);
        facebookUiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(activity.userIsRegistered) {
            rootView = inflater.inflate(R.layout.fragment_home, container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_login, container, false);
            LoginButton authButton = (LoginButton) rootView.findViewById(R.id.facebookLoginButton);
            authButton.setFragment(this);
            authButton.setReadPermissions("public_profile", "email", "user_friends");
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        facebookUiHelper.onResume();
        Log.d(TAG, "ONRESUME");
        Session session = Session.getActiveSession();
        Log.d(TAG, session.toString());
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(activity, requestCode, resultCode, data);
        facebookUiHelper.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "ONACTIVITYRESULT");
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

    private void onSessionStateChange(Session session, SessionState state, Exception e) {
        Log.d("SessionStateChanged", state.toString());
        if(state.isOpened()) {
            Log.d(TAG, "Logged in.");
        } else if(state.isClosed()) {
            Log.d(TAG, "Logged out.");
        }
    }
}
