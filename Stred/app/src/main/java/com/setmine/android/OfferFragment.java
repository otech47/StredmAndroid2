package com.setmine.android;

import android.app.Fragment;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.setmine.android.api.SetMineApiGetRequestAsyncTask;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.user.User;
import com.setmine.android.util.DateUtils;

import org.json.JSONObject;

/**
 * Created by ryan on 6/21/2015.
 */
public class OfferFragment  extends Fragment implements ApiCaller{

    public ModelsContentProvider modelsCP;
    public DateUtils dateUtils;
    public View mapContainer;
    public User registeredUser;
    public View rootView;
    public View offerButton1;
    public View offerButton2;


    private Location userLocation;
    private int timeID;

    private static final String TAG = "OfferFragment";

    @Override
    public void onApiResponseReceived(JSONObject jsonObject, String identifier) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if(savedInstanceState==null){
            new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                            "festival/search/" + Uri.encode(currentEvent.getEvent()), "sets");
            return;
        }
        else{

        }

        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);



        if(modelsCP == null) {
            modelsCP = new ModelsContentProvider();
        }


        userLocation = new Location("default");
        userLocation.setLatitude(savedInstanceState.getDouble("latitude"));
        userLocation.setLongitude(savedInstanceState.getDouble("longitude"));
        String activitiesModel = savedInstanceState.getString("activities");
        String userModel = savedInstanceState.getString("user");

        try {
            JSONObject jsonModel = new JSONObject(activitiesModel);
            JSONObject jsonUser = new JSONObject(userModel);

            modelsCP.setModel(jsonModel, "activities");
            registeredUser = new User(jsonUser);
        } catch(Exception e) {

        }


    }


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        Log.d(TAG,"onCreateView");
        rootView = inflater.inflate(R.layout.offer_detail, container, false);

        //feature detail containers
        mapContainer= rootView.findViewById(R.id.mapContainer);

        //feature buttons
        offerButton1 = rootView.findViewById(R.id.offerButton1);
        offerButton2 = rootView.findViewById(R.id.offerButton1);

        assignClickListeners();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void assignClickListeners() {
        offerButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
        offerButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
        mapContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

}
