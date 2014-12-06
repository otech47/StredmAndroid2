package com.setmine.android.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;

/**
 * Created by oscarlafarga on 11/30/14.
 */
public class LoginFragment extends Fragment {

    private View rootView;
    public SetMineMainActivity activity;

    private static final String TAG = "LoginFragment";

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
        Log.d(TAG, "ONCREATE");
        activity = (SetMineMainActivity)getActivity();
        facebookUiHelper = new UiLifecycleHelper(activity, facebookCallback);
        facebookUiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_login, container, false);
        Log.d(TAG, "ONCREATEVIEW");
        LoginButton authButton = (LoginButton) rootView.findViewById(R.id.facebookLoginButton);
        authButton.setFragment(this);
        authButton.setReadPermissions("public_profile", "email", "user_friends");
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "ONRESUME");
        Session session = Session.getActiveSession();
        Log.d(TAG, session.toString());
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }
        facebookUiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "ONACTIVITYRESULT");
        facebookUiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "ONPAUSE");
        facebookUiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ONDESTROY");
        facebookUiHelper.onDestroy();
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
            activity.openUserHomePage();
        } else if(state.isClosed()) {
            Log.d(TAG, "Logged out.");
        }
    }

}
