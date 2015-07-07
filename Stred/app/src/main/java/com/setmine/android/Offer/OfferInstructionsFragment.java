package com.setmine.android.Offer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
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
    public static final String ARG_OBJECT = "page";

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
            mainImage.setImageResource(R.drawable.locked_offer);
            instructions.setText("Click the map and navigate to the unlock location.");
        } else if(page == 1) {
            header.setText("Enter");
            mainImage.setImageResource(R.drawable.offer_instructions_2);
            instructions.setText("Check out the place your favorite artist sent you to unlock their music.");
        } else if(page == 2) {
            header.setText("Enjoy");
            mainImage.setImageResource(R.drawable.unlocked_offer);
            instructions.setText("Check your notifications to receive full access to the artist's content and enjoy exclusive discounts from the retailer.");
        } else {
            header.setText("Redeem");

            mainImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(getArguments().get("link") != null) {
                        Uri ticketUrl = Uri.parse(getArguments().getString("link"));
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, ticketUrl);
                        startActivity(launchBrowser);
                    }

                }
            });
            Log.d(TAG, getArguments().toString());

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.logo_small)
                    .showImageForEmptyUri(R.drawable.logo_small)
                    .showImageOnFail(R.drawable.logo_small)
                    .cacheInMemory(false)
                    .cacheOnDisk(false)
                    .considerExifParams(true)
                    .build();

            ImageLoader.getInstance().loadImage(getArguments().getString("image"), options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    Log.d(TAG, imageUri);
                    mainImage.setImageDrawable(new BitmapDrawable(getActivity().getResources(), loadedImage));
                }
            });
            instructions.setText(getArguments().getString("message"));
        }

    }

}
