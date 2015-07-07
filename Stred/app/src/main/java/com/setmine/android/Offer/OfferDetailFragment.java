package com.setmine.android.Offer;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.setmine.android.OfferInstructionsContainer;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.api.SetMineApiGetRequestAsyncTask;
import com.setmine.android.image.ImageUtils;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.set.Set;
import com.setmine.android.user.User;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by ryan on 6/21/2015.
 */
public class OfferDetailFragment extends Fragment implements ApiCaller {

    // Statics
    private static final String TAG = "OfferDetailFragment";

    // Views
    public View mapContainer;
    public View rootView;
    public View offerButton1;
    public View unlockedLayout;
    public View loader;
    public TextView artistNameText;
    public TextView offerVenueText;
    public TextView offerEventText;
    public TextView distanceText;
    public TextView offerButtonText1;
    public TextView redemptionText;
    public TextView vendorMessageText;
    public TextView addressText;


    // Models
    public User registeredUser;
    public Offer currentOffer;
    private Set unlockedContent;

    public float distance;
    public SetMineMainActivity activity;
    private Location userLocation;
    private int timeID;
    private DisplayImageOptions options;
    public OfferInstructionsContainer offerInstContainer;

    final Handler offerHandler = new Handler();

    final Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            populateOfferDetails();
            assignClickListeners();

            // Check if unlockedSet of currentOffer is null
            if(checkUnlockStatus().equals("locked")) {
                getStaticMap();
            } else {
                prepareUnlockedOffer();
            }
        }
    };

    @Override
    public void onApiResponseReceived(final JSONObject jsonObject, String identifier) {
        final JSONObject finalJsonObject = jsonObject;


        final String finalIdentifier = identifier;
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (finalIdentifier.equals("offers")) {
                    try {
                        createOffer(finalJsonObject.getJSONObject("payload").getJSONObject("offer"));
                        offerHandler.post(updateUI);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");

    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.offer_detail, container, false);

        // Show loader until data is ready
        loader = rootView.findViewById(R.id.centered_loader_container);
        loader.setVisibility(View.VISIBLE);

        //feature detail containers
        mapContainer = rootView.findViewById(R.id.mapContainer);
        addressText = (TextView)rootView.findViewById(R.id.locationText);

        //feature buttons
        offerButton1 = rootView.findViewById(R.id.howToUnlockLayout);

        //text views
        artistNameText = (TextView) rootView.findViewById(R.id.offerDetailArtistName);
        offerVenueText = (TextView) rootView.findViewById(R.id.offerVenueText);
        offerEventText = (TextView) rootView.findViewById(R.id.offerEventText);

        distanceText = (TextView) rootView.findViewById(R.id.distanceText);
        offerButtonText1 = (TextView) rootView.findViewById(R.id.offerText1);
        redemptionText = (TextView) rootView.findViewById(R.id.redemptionText);

        if (savedInstanceState == null) {
            Log.v(TAG, "savedInstanceState is null");
            Bundle arguments = getArguments();
            String currentOfferId = arguments.getString("currentOffer");
            User registeredUser = ((SetMineMainActivity)getActivity()).user;
            String query = "offers/id/" + currentOfferId;
            if(registeredUser.isRegistered()) {
                query += "/user/" + registeredUser.getId();
            }
            Log.d(TAG, query);
            new SetMineApiGetRequestAsyncTask((SetMineMainActivity) getActivity(), this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                            query, "offers");

        } else {
            Log.v(TAG, "savedInstanceState is not null");

            try {
                currentOffer = new Offer(new JSONObject(savedInstanceState.getString("currentOffer")));
                new Thread(updateUI).start();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentOffer", currentOffer.jsonModelString);
    }

    public void createOffer(JSONObject jsonObject) {
        Log.d(TAG, "createOffer");
        currentOffer = new Offer(jsonObject);
    }

    public void getStaticMap() {
        Location currentLocation = ((SetMineMainActivity) getActivity()).currentLocation;
        Location venueLocation = new Location("venue");
        venueLocation.setLatitude(Double.parseDouble(currentOffer.getVenue().getLatitude()));
        venueLocation.setLongitude(Double.parseDouble(currentOffer.getVenue().getLongitude()));
        String venueLatitude = currentOffer.getVenue().getLatitude();
        String venueLongitude = currentOffer.getVenue().getLongitude();

        String staticMapUrlFinal = "https://maps.googleapis.com/maps/api/staticmap?";
        String staticMapSize = "size=450x450";

        if (currentLocation != null) {
            //distanceTo() is returned in meters
            distance = currentLocation.distanceTo(venueLocation);
            Log.d(TAG, Float.toString(distance));
            distance = distance * 3.2808399f; //convert to feet
            if(distance <= 528){
                BigDecimal bd = new BigDecimal(Float.toString(distance));
                bd = bd.setScale(0,BigDecimal.ROUND_HALF_UP);
                distanceText.setText(String.valueOf(bd)+" feet" );
            }else if(distance> 528 ){
                distance = distance/5280;
                BigDecimal bd = new BigDecimal(Float.toString(distance));
                bd = bd.setScale(1,BigDecimal.ROUND_HALF_UP);
                distanceText.setText(String.valueOf(bd)+" miles" );
            }
            distanceText.setVisibility(View.VISIBLE);

            Double currentLatitude = currentLocation.getLatitude();
            Double currentLongitude = currentLocation.getLongitude();
            String staticMapVenueLocation = "&markers=color:0xAA48CB%7Clabel:B%7C" + venueLatitude + "," + venueLongitude;
            String staticMapCurrentLocation = "&markers=color:0x4A87F4%7Clabel:A%7C" + currentLatitude.toString() + "," + currentLongitude.toString();
            String staticMapPath = "&path=color:0x808080|weight:5|" + currentLatitude.toString() + "," + currentLongitude.toString() + "|" + venueLatitude + "," + venueLongitude;
            staticMapUrlFinal = staticMapUrlFinal + staticMapSize + staticMapCurrentLocation + staticMapVenueLocation + staticMapPath;
        } else {
            staticMapUrlFinal = staticMapUrlFinal + staticMapSize + "&markers=color:0xAA48CB%7C" + venueLatitude + "," + venueLongitude;
        }

        final String STATIC_MAP_API_ENDPOINT = staticMapUrlFinal;

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .considerExifParams(true)
                .build();

        ImageLoader.getInstance().loadImage(STATIC_MAP_API_ENDPOINT, options, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {
                Log.i(TAG, "Loading started");
                super.onLoadingStarted(imageUri, view);
                loader.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Log.d(TAG, imageUri);
                Bitmap roundedBitmap = ImageUtils.getRoundedCornerBitmap(loadedImage, 400);

                ImageView mapView = (ImageView) rootView.findViewById(R.id.offer_center_image);
                mapView.setImageDrawable(new BitmapDrawable(getActivity().getResources(), roundedBitmap));

                loader.setVisibility(View.GONE);
                rootView.findViewById(R.id.circle).setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                Log.e(TAG, "imageLoad Failed");
                loader.setVisibility(View.GONE);
                rootView.findViewById(R.id.circle).setVisibility(View.VISIBLE);

            }
        });

    }

    public void populateOfferDetails() {

        Date juDate = new Date();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        DateTime currentTime = new DateTime(juDate);

        if(currentOffer.getDateExpired().equals("null")) {
            redemptionText.setVisibility(View.GONE);
        } else {
            DateTime expirationTime = fmt.parseDateTime(currentOffer.getDateExpired());
            Period period = new Period(currentTime, expirationTime);
            int hoursInt = period.getHours();
            int minutesInt = period.getMinutes();
            int daysInt = period.getDays();


            if (daysInt < 1 && hoursInt < 24 && hoursInt > 1) {
                redemptionText.setText("This offer expires in " + hoursInt + " hours and " + minutesInt + " minutes.");
            } else if (hoursInt == 1) {
                redemptionText.setText("This offer expires in 1 hour and " + minutesInt + " minutes.");
            } else if (hoursInt > 24) {
                int hours = hoursInt % 24;
                if (hours > 1) {
                    redemptionText.setText("This offer expires in " + daysInt + " days and " + hours + " hours.");
                } else if (hours == 1) {
                    redemptionText.setText("This offer expires in " + daysInt + " days and 1 hour.");
                } else if (hours < 1) {
                    redemptionText.setText("This offer expires in " + daysInt + " days.");
                }
            } else if (Seconds.secondsBetween(currentTime, expirationTime) == null) {
                redemptionText.setText("This offer has expired.");
            }
        }

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        ImageLoader.getInstance().loadImage(currentOffer.getArtist().getImageUrl(), options, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Bitmap blurredBitmap = ImageUtils.fastblur(loadedImage, 5);
                ImageView mBackgroundImage = (ImageView)rootView.findViewById(R.id.offerBackgroundImage);
                mBackgroundImage.setImageDrawable(new BitmapDrawable(getActivity().getResources(), blurredBitmap));

            }
        });

        //changing text views
        artistNameText.setText(currentOffer.getArtist().getArtist());
        artistNameText.setTypeface(null, Typeface.BOLD);
        offerVenueText.setText(currentOffer.getVenue().getVenueName());
        offerEventText.setText(currentOffer.getEventName());

        addressText.setText(currentOffer.getVenue().getAddress());

    }

    public void assignClickListeners() {
        offerButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOfferInstructionsContainer(0);
            }
        });

        mapContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SetMineMainActivity)getActivity()).googleMapsAddressLookup(v.findViewById(R.id.locationText));
            }
        });

    }

    private void openOfferInstructionsContainer(int pageToScrollTo) {
        Log.d(TAG, "openOfferInstructionsContainer");
        offerInstContainer = new OfferInstructionsContainer();
        Bundle args = new Bundle();
        args.putInt("page", pageToScrollTo);
        offerInstContainer.setArguments(args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.offer_detail, offerInstContainer);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack("offerInstructions");
        transaction.commitAllowingStateLoss();
    }

    public String checkUnlockStatus() {
        if(currentOffer.getUnlockedSet() == null) {
            return "locked";
        } else {
            return "unlocked";
        }
    }

    public void prepareUnlockedOffer() {
        ImageLoader.getInstance().loadImage(currentOffer.getUnlockedSet().getEventImage(), options, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {
                Log.i(TAG, "Loading started");
                super.onLoadingStarted(imageUri, view);
                loader.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                ImageView mapView = (ImageView) rootView.findViewById(R.id.offer_center_image);
                Bitmap roundedBitmap = ImageUtils.getRoundedCornerBitmap(loadedImage, 400);

                mapView.setImageDrawable(new BitmapDrawable(getActivity().getResources(), roundedBitmap));
                loader.setVisibility(View.GONE);
                rootView.findViewById(R.id.circle).setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                Log.e(TAG, "imageLoad Failed");
                loader.setVisibility(View.GONE);
                rootView.findViewById(R.id.circle).setVisibility(View.VISIBLE);

            }
        });
        rootView.findViewById(R.id.play_overlay).setVisibility(View.VISIBLE);
        mapContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity = (SetMineMainActivity) getActivity();
                if(activity.playerService != null) {
                    if(activity.playerService.playerManager.getSelectedSet() != null) {
                        if (activity.playerService.playerManager.getSelectedSet().getId().equals(currentOffer.getUnlockedSet().getId())) {
                            activity.startPlayerFragment();
                        } else {
                            activity.playerService.playerManager.addToPlaylist(currentOffer.getUnlockedSet());
                            activity.playerService.playerManager.selectSetById(currentOffer.getUnlockedSet().getId());
                            activity.startPlayerFragment();
                            activity.playSelectedSet();
                        }
                    } else {
                        activity.playerService.playerManager.addToPlaylist(currentOffer.getUnlockedSet());
                        activity.playerService.playerManager.selectSetById(currentOffer.getUnlockedSet().getId());
                        activity.startPlayerFragment();
                        activity.playSelectedSet();
                    }
                }

            }
        });
        distanceText.setText("Click to Play");
        distanceText.setVisibility(View.VISIBLE);
        ((TextView)offerButton1.findViewById(R.id.offerText1)).setText("Redeem Extra Content");
        ((ImageView)offerButton1.findViewById(R.id.lockIcon)).setImageResource(R.drawable.reward);
        offerButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OfferInstructionsFragment redeemOffer = new OfferInstructionsFragment();
                Bundle args = new Bundle();
                if(currentOffer.getImageURL().equals("null")) {
                    Log.d(TAG, "image null");

                    args.putString("image", currentOffer.getVenue().getIconImageUrl());
                } else {
                    Log.d(TAG, "image not null");
                    Log.d(TAG, currentOffer.getImageURL());


                    args.putString("image", currentOffer.getImageURL());
                }
                if(currentOffer.getLink().equals("null")) {
                    Log.d(TAG, "link null");
                    args.putString("link", null);
                } else {
                    Log.d(TAG, "link not null");
                    Log.d(TAG, currentOffer.getLink());

                    args.putString("link", currentOffer.getLink());
                }
                args.putString("message", currentOffer.getMessage());
                args.putInt("page", 3);
                redeemOffer.setArguments(args);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.add(R.id.offer_detail, redeemOffer);
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                transaction.addToBackStack("redeemOffer");
                transaction.commitAllowingStateLoss();
            }
        });

    }

    public void refreshUnlockStatus() {
        String query = "offers/id/" + currentOffer.getOfferId();
        if(registeredUser != null && registeredUser.isRegistered()) {
            query += "/user/" + registeredUser.getId();
        }
        new SetMineApiGetRequestAsyncTask((SetMineMainActivity) getActivity(), this)
                .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                        query, "offers");
    }

}
