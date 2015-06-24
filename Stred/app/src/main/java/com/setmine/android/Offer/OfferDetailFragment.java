package com.setmine.android.Offer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.setmine.android.Constants;
import com.setmine.android.ModelsContentProvider;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.api.SetMineApiGetRequestAsyncTask;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.user.User;
import com.setmine.android.util.DateUtils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
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
    public View offerButton2;
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

    public void getStaticMap(){
       final String STATIC_MAP_API_ENDPOINT = "https://maps.googleapis.com/maps/api/staticmap?center="+currentOffer.getVenue().getLatitude()+","+currentOffer.getVenue().getLongitude()+"&zoom=13&size=300x300";
        AsyncTask<Void, Void, Bitmap> setImageFromUrl = new AsyncTask<Void, Void, Bitmap>(){
            @Override
            protected Bitmap doInBackground(Void... params) {
                Bitmap bmp = null;
                HttpClient httpclient = new DefaultHttpClient();
                HttpGet request = new HttpGet(STATIC_MAP_API_ENDPOINT);

                InputStream in = null;
                try {
                    in = httpclient.execute(request).getEntity().getContent();
                    bmp = BitmapFactory.decodeStream(in);
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bmp;
            }
            protected void onPostExecute(Bitmap bmp) {
                if (bmp != null) {
                    final ImageView iv = (ImageView) rootView.findViewById(R.id.mapImage);
                    iv.setImageBitmap(bmp);
                }
            }
        };
        setImageFromUrl.execute();
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

        }


    }


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        Log.d(TAG,"onCreateView");
        rootView = inflater.inflate(R.layout.offer_detail, container, false);

        //feature detail containers
        mapContainer= rootView.findViewById(R.id.mapContainer);

        //feature buttons
        offerButton1 = rootView.findViewById(R.id.offerButton1);

        //text views
        artistNameText = (TextView)rootView.findViewById(R.id.offerDetailArtistName);
        offerVenueText = (TextView)rootView.findViewById(R.id.offerVenueText);
        distanceText = (TextView)rootView.findViewById(R.id.distanceText);
        offerButtonText1 = (TextView)rootView.findViewById(R.id.offerText1);
        redemptionText = (TextView)rootView.findViewById(R.id.redemptionText);
        vendorMessageText =(TextView)rootView.findViewById(R.id.vendorMessage);
        addressText1 = (TextView) rootView.findViewById(R.id.addressText1);
        addressText2 = (TextView)rootView.findViewById(R.id.addressText2);

        //image views
        vendorImage = (ImageView)rootView.findViewById(R.id.vendorIcon);
        ImageLoader.getInstance().displayImage(Constants.CLOUDFRONT_URL_FOR_IMAGES+ currentOffer.getVenue().getIconImageUrl(), vendorImage);


        //update time left
        Date juDate = new Date();
        DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
        DateTime currentTime = new DateTime(juDate);
        DateTime expirationTime = fmt.parseDateTime(currentOffer.getDateExpired());
        String hoursString = Hours.hoursBetween(currentTime,expirationTime).toString();
        String daysString = Days.daysBetween(currentTime, expirationTime).toString();
        String minutesString= Minutes.minutesBetween(currentTime, expirationTime).toString();
        int hoursInt = Integer.parseInt(hoursString);
        int daysInt = Integer.parseInt(daysString);
        int minutesInt = Integer.parseInt(minutesString);

        if(hoursInt<24 &&hoursInt>1){
            redemptionText.setText("This offer expires in " + hoursString + " hours and " + minutesInt + " minutes.");
        }else if(hoursInt==1){
            redemptionText.setText("This offer expires in 1 hour and " + minutesInt + " minutes.");
        }
        else if(hoursInt>24){
            int hours = hoursInt%24;
            if(hours >1) {
                redemptionText.setText("This offer expires in " + daysInt + " days and " + hours + " hours.");
            }else if(hours == 1){
                redemptionText.setText("This offer expires in " + daysInt + " days and 1 hour.");
            }else if(hours <1){
                redemptionText.setText("This offer expires in " + daysInt + " days.");
            }
        }else if(Seconds.secondsBetween(currentTime, expirationTime) == null){
            redemptionText.setText("This offer has expired.");
        }

        //changing text views
        artistNameText.setText(currentOffer.getArtist().getArtist());
        offerButtonText1.setText("Exclusive set.");
        vendorMessageText.setText(currentOffer.getMessage());
        if(currentOffer.getVenue().getAddress() != null){
            String[] addressArray = currentOffer.getVenue().getAddress().split(",");
            addressText1.setText(addressArray[0]);
            addressText2.setText(addressArray[1]+addressArray[2]);
        }

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
