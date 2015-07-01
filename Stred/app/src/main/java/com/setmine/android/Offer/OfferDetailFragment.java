package com.setmine.android.Offer;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
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
import com.setmine.android.ModelsContentProvider;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.api.SetMineApiGetRequestAsyncTask;
import com.setmine.android.image.ImageUtils;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.user.User;
import com.setmine.android.util.DateUtils;

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

    public ModelsContentProvider modelsCP;
    public DateUtils dateUtils;
    public View mapContainer;
    public User registeredUser;
    public View rootView;
    public View offerButton1;
    public Offer currentOffer;
    public TextView artistNameText;
    public TextView offerVenueText;
    public TextView distanceText;
    public TextView offerButtonText1;
    public TextView redemptionText;
    public TextView vendorMessageText;
    public TextView addressText1;
    public TextView addressText2;
    public ImageView vendorImage;
    public float distance;
    public SetMineMainActivity activity;


    private Location userLocation;
    private int timeID;
    private DisplayImageOptions options;


    private static final String TAG = "OfferDetailFragment";

    @Override
    public void onApiResponseReceived(final JSONObject jsonObject, String identifier) {
        final JSONObject finalJsonObject = jsonObject;


        final String finalIdentifier = identifier;
        new Thread(new Runnable() {
            @Override
            public void run() {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (finalIdentifier == "offers") {
                            try {
                                createOffer(finalJsonObject.getJSONObject("payload").getJSONObject("offer"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });
            }
        }).start();
    }

    public void createOffer(JSONObject jsonObject) {
        currentOffer = new Offer(jsonObject);
        getStaticMap();
        ImageLoader.getInstance().displayImage(currentOffer.getVenue().getIconImageUrl(), vendorImage);
//update time left
        Date juDate = new Date();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        DateTime currentTime = new DateTime(juDate);

        if(currentOffer.getDateExpired() != "null") {
            DateTime expirationTime = fmt.parseDateTime(currentOffer.getDateExpired());
            Period period = new Period(currentTime, expirationTime);
            int hoursInt = period.getHours();
            int minutesInt = period.getMinutes();
            int daysInt = period.getDays();


            if (hoursInt < 24 && hoursInt > 1) {
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
        }else{
            redemptionText.setVisibility(View.GONE);
        }

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        ImageLoader.getInstance().loadImage(currentOffer.getVenue().getBannerImageUrl(), options, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                ImageUtils imageUtils = new ImageUtils();
                Bitmap blurredBitmap = imageUtils.fastblur(loadedImage, 5);
                ImageView mBackgroundImage = (ImageView)rootView.findViewById(R.id.offerBackgroundImage);
                mBackgroundImage.setImageDrawable(new BitmapDrawable(getActivity().getResources(), blurredBitmap));




            }
        });

        //changing text views
        artistNameText.setText(currentOffer.getArtist().getArtist());
        artistNameText.setTypeface(null, Typeface.BOLD);
        String test1= currentOffer.getArtist().getArtist();
        offerButtonText1.setText("Exclusive Set");
        vendorMessageText.setText(currentOffer.getMessage());
        String test = currentOffer.getVenue().getAddress();
        if (currentOffer.getVenue().getAddress() != null) {
            String[] addressArray = currentOffer.getVenue().getAddress().split(",");
            addressText1.setText(addressArray[0]);
            addressText2.setText(addressArray[1] + addressArray[2]);
        }
        offerVenueText.setText(currentOffer.getVenue().getVenueName());
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
        String test ="";

        if (currentLocation != null) {
            //distanceTo() is returned in meters
            distance = currentLocation.distanceTo(venueLocation);
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

            Double currentLatitude = currentLocation.getLatitude();
            Double currentLongitude = currentLocation.getLongitude();
            String staticMapVenueLocation = "&markers=color:0xAA48CB%7Clabel:B%7C" + venueLatitude + "," + venueLongitude;
            String staticMapCurrentLocation = "&markers=color:0x4A87F4%7Clabel:A%7C" + currentLatitude.toString() + "," + currentLongitude.toString();
            String staticMapPath = "&path=color:0x808080|weight:5|" + currentLatitude.toString() + "," + currentLongitude.toString() + "|" + venueLatitude + "," + venueLongitude;
            test = staticMapUrlFinal +staticMapSize+staticMapCurrentLocation+staticMapVenueLocation+staticMapPath;
            String toEncode =Uri.encode( staticMapCurrentLocation+ staticMapVenueLocation + staticMapPath);
            staticMapUrlFinal = staticMapUrlFinal + Uri.encode(staticMapSize)+ toEncode;


        } else {
            staticMapUrlFinal = staticMapUrlFinal +Uri.encode(staticMapSize)+ Uri.encode("&markers=color:0xAA48CB%7C" + venueLatitude + "," + venueLongitude);
        }

        final String STATIC_MAP_API_ENDPOINT = staticMapUrlFinal;



       DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

//        ImageLoader.getInstance().displayImage(test, (ImageView)rootView.findViewById(R.id.mapImage));

    ImageLoader.getInstance().loadImage(test, options, new SimpleImageLoadingListener() {

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            super.onLoadingStarted(imageUri, view);
            rootView.findViewById(R.id.centered_loader_container).setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ImageUtils imageUtils = new ImageUtils();
            Bitmap roundedBitmap = null;
            int count = 0;
            while (roundedBitmap == null) {
                roundedBitmap = imageUtils.getRoundedCornerBitmap(loadedImage, 400);
                count = 1+count;
            }
            ImageView mapView = (ImageView) rootView.findViewById(R.id.mapImage);
            mapView.setImageDrawable(new BitmapDrawable(getActivity().getResources(), roundedBitmap));
            mapView.setImageBitmap(roundedBitmap);

            TextView mapAddress = (TextView) rootView.findViewById(R.id.locationText);
            mapAddress.setText(addressText1.getText() + "," + addressText2.getText());
            mapContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.googleMapsAddressLookup(v);
                }
            });
            rootView.findViewById(R.id.centered_loader_container).setVisibility(View.GONE);
            rootView.findViewById(R.id.circle).setVisibility(View.VISIBLE);

            }
        });


    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");


        if (savedInstanceState == null) {
            this.activity = (SetMineMainActivity)getActivity();
            Bundle arguments = getArguments();
            String currentOfferId = arguments.getString("currentOffer");
            new SetMineApiGetRequestAsyncTask((SetMineMainActivity) getActivity(), this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                            "offers/id/" + Uri.encode(currentOfferId), "offers");


        } else {
            try {
                currentOffer = new Offer(new JSONObject(savedInstanceState.getString("currentOffer")));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.offer_detail, container, false);

        //feature detail containers
        mapContainer = rootView.findViewById(R.id.mapContainer);

        //feature buttons
        offerButton1 = rootView.findViewById(R.id.offerButton1);

        //text views
        artistNameText = (TextView) rootView.findViewById(R.id.offerDetailArtistName);
        offerVenueText = (TextView) rootView.findViewById(R.id.offerVenueText);
        distanceText = (TextView) rootView.findViewById(R.id.distanceText);
        offerButtonText1 = (TextView) rootView.findViewById(R.id.offerText1);
        redemptionText = (TextView) rootView.findViewById(R.id.redemptionText);
        vendorMessageText = (TextView) rootView.findViewById(R.id.vendorMessage);
        addressText1 = (TextView) rootView.findViewById(R.id.addressText1);
        addressText2 = (TextView) rootView.findViewById(R.id.addressText2);

        //image views
        vendorImage = (ImageView) rootView.findViewById(R.id.vendorIcon);


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

        mapContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

}
