package com.setmine.android.Offer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.setmine.android.R;
import com.setmine.android.user.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by oscarlafarga on 7/3/15.
 */
public class OfferInstructionsFragment extends Fragment {

    // Statics
    private static final String TAG = "OfferInstrFragment";
    private static final String ARG_OBJECT = "page";

    public static int page;

    // Views
    View rootView;
    TextView header;
    TextView instructions;
    ImageView mainImage;

    // Models
    public User registeredUser;
    public Offer currentOffer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");


        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            page = arguments.getInt("page");

        } else {
            page = savedInstanceState.getInt("page");
            try {
                currentOffer = new Offer(new JSONObject(savedInstanceState.getString("currentOffer")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.offer_instructions, container, false);

        header = (TextView)rootView.findViewById(R.id.header);
        instructions = (TextView)rootView.findViewById(R.id.instructions);
        mainImage = (ImageView) rootView.findViewById(R.id.mainImage);

        populateInstructionPages(page);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("page", page);
    }

    public void populateInstructionPages(int page) {
        if(page == 0) {
            header.setText("Find Location");
            mainImage.setImageResource(R.drawable.setmine_splash);
            instructions.setText("Click the map and navigate to the unlock location.");
        } else if(page == 1) {
            header.setText("Enter");
            mainImage.setImageResource(R.drawable.offer_instructions_2);
            instructions.setText("Check out the place your favorite artist sent you to unlock their music.");
        } else {
            header.setText("Enjoy");
            mainImage.setImageResource(R.drawable.setmine_splash);
            instructions.setText("Check your notifications to receive full access to the artist's content and enjoy exclusive discounts from the retailer.");
        }

    }

}
