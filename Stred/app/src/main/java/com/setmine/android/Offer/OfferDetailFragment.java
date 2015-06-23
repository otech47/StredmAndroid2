package com.setmine.android.Offer;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.setmine.android.ModelsContentProvider;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.api.SetMineApiGetRequestAsyncTask;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.user.User;
import com.setmine.android.util.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ryan on 6/21/2015.
 */
public class OfferDetailFragment extends Fragment implements ApiCaller {

    public ModelsContentProvider modelsCP;
    public DateUtils dateUtils;
    public View mapContainer;
    public User registeredUser;
    public View rootView;
    public View offerButton1;
    public View offerButton2;
    public Offer currentOffer;
    public String offerId;
    public String artistId;
    public String setId;
    public String venueId;
    public String dateReleased;
    public String dateExpired;
    public String totalRevenue;
    public String totalConvergences;
    public TextView artistNameText;
    public TextView exclusiveContentText;
    public TextView distanceText;
    public TextView offerButtonText1;
    public TextView offerButtonText2;
    public TextView redemptionText;
    public TextView minutesLeftText;
    public TextView vendorMessageText;



    private Location userLocation;
    private int timeID;

    private static final String TAG = "OfferDetailFragment";

    @Override
    public void onApiResponseReceived(final JSONObject jsonObject, String identifier) {
        final JSONObject finalJsonObject = jsonObject;
        final String finalIdentifier = identifier;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(finalIdentifier == "offers"){
                    createOffer(finalJsonObject);
                }
            }
        }).start();
    }

    public void createOffer(JSONObject jsonObject){
       currentOffer= new Offer(jsonObject);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if(savedInstanceState==null){
            Bundle arguments = getArguments();
            String currentOfferId = arguments.getString("currentOffer");
            new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                            "offers/" + Uri.encode(currentOfferId), "offers");


        }

        else{
            try {
                currentOffer = new Offer(new JSONObject(savedInstanceState.getString("currentOffer")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            offerId = savedInstanceState.getString("offerId");
            setId = savedInstanceState.getString("setId");
            venueId = savedInstanceState.getString("venueId");
            dateReleased = savedInstanceState.getString("dateReleased");
            dateExpired = savedInstanceState.getString("dateExpired");
            totalRevenue = savedInstanceState.getString("totalRevenue");
            totalConvergences = savedInstanceState.getString("totalConvergences");
            artistId = savedInstanceState.getString("artistId");
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

        //text views
        artistNameText = (TextView)rootView.findViewById(R.id.offerDetailArtistName);
        exclusiveContentText= (TextView)rootView.findViewById(R.id.exclusiveContentText);
        distanceText = (TextView)rootView.findViewById(R.id.distanceText);
        offerButtonText1 = (TextView)rootView.findViewById(R.id.offerText1);
        offerButtonText2 = (TextView)rootView.findViewById(R.id.offerText2);
        redemptionText = (TextView)rootView.findViewById(R.id.redemptionText);
        minutesLeftText = (TextView)rootView.findViewById(R.id.minutesLeftText);
        vendorMessageText =(TextView)rootView.findViewById(R.id.vendorMessage);







        assignClickListeners();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentOffer", currentOffer.jsonModelString);



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
