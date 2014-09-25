package com.stredm.android.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.stredm.android.EventDetailFragment;
import com.stredm.android.EventPageFragment;
import com.stredm.android.EventPagerActivity;
import com.stredm.android.Model;
import com.stredm.android.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by oscarlafarga on 9/18/14.
 */
public class TileGenerator {

    private static final String serverRoot = "http://stredm.com/";
    private static final String mapsApiKey = "AIzaSyAUoaqRaUBFZISnQoVgkgSMKkg7XEwctxU";
    private static final String mapsAndroidApiKey = "AIzaSyANSSNDva7xtMp_VOaPPq925KiFvAdCPlY";
    private static final String googleMapsApiRoot = "https://maps.googleapis.com/maps/api/";
    private Context context;
    public ImageCache imageCache;
    public int imagesLoaded = 0;
    public ViewPager eventViewPager;
    public EventPageFragment eventPageFragment;
    public List<String> formattedLocation = new ArrayList<String>();

    public TileGenerator(Context context, ViewPager eventViewPager, ImageCache imageCache) {
        this.context = context;
        this.imageCache = imageCache;
        this.eventViewPager = eventViewPager;
    }

    public void setImageBackground(String imageUrl, ImageView imageView) {
        if(imageCache.getBitmapFromMemCache(imageUrl) == null) {
            DownloadImageTask imageTask = new DownloadImageTask(context, imageCache, imageView, this);
            imageTask.execute(imageUrl);
        }
        else {
            Bitmap image = imageCache.getBitmapFromMemCache(imageUrl);
            onDownloadImage(imageView, image);
        }
    }

    public void onDownloadImage(ImageView imageView, Bitmap image) {
        imagesLoaded++;
        if(image != null && imageView != null)
            imageView.setImageBitmap(image);
//        Log.v("setting image bitmap", image.toString());
        eventViewPager.setVisibility(View.VISIBLE);
    }


    public View modelsToUpcomingEventTiles(List<Model> models, View rootView) {
        List<View> views = new ArrayList<View>();
        View parentView = rootView.findViewById(R.id.eventsList);
        LayoutInflater inflater = ((EventPagerActivity) parentView.getContext()).getLayoutInflater();
        for(Model element : models) {
            View eventTile = inflater.inflate(R.layout.event_tile, null);
            String imageUrl = serverRoot + "images/" + element.landing_image;
            final String eName = element.event;
            final String eDate = formatDateText(element.start_date, element.end_date);
            final String eCity = formatLocationFromAddress(element.address);
            final String eImage = imageUrl;
            final Integer eId = element.id;
            final String eDateUnformatted = element.start_date;
            ImageView imageView = ((ImageView) eventTile.findViewById(R.id.image));
//            Log.v("Getting Image For ", element.event);
            setImageBackground(imageUrl, imageView);
            ((TextView) eventTile.findViewById(R.id.event)).setText(eName.toUpperCase());
            ((TextView) eventTile.findViewById(R.id.date)).setText(eDate);
            ((TextView) eventTile.findViewById(R.id.city)).setText(eCity);
            ((TextView) eventTile.findViewById(R.id.type)).setText("upcoming");
            (eventTile.findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EventDetailFragment eventDetailFragment = new EventDetailFragment();
                    eventDetailFragment.EVENT_ID = eId;
                    eventDetailFragment.EVENT_NAME = eName;
                    eventDetailFragment.EVENT_DATE = eDate;
                    eventDetailFragment.EVENT_DATE_UNFORMATTED = eDateUnformatted;
                    eventDetailFragment.EVENT_CITY = eCity;
                    eventDetailFragment.EVENT_IMAGE = eImage;
                    eventDetailFragment.EVENT_TYPE = "upcoming";
                    FragmentTransaction transaction = eventPageFragment.getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.eventPagerContainer, eventDetailFragment, "eventDetailFragment");
//                    (((EventPagerActivity) eventPageFragment.getActivity()).mEventPagerAdapter).detailFragmentPosition = 1;
//                    (((EventPagerActivity) eventPageFragment.getActivity()).mEventPagerAdapter).notifyDataSetChanged();
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                }
            });
            ((ViewGroup) parentView).addView(eventTile);
        }
        return parentView;
    }

    public View modelsToRecentEventTiles(List<Model> models, View rootView) {
        List<View> views = new ArrayList<View>();
        View parentView = rootView.findViewById(R.id.eventsList);
        LayoutInflater inflater = ((EventPagerActivity) parentView.getContext()).getLayoutInflater();
        for(Model element : models) {
            View eventTile = inflater.inflate(R.layout.event_tile, null);
            String imageUrl = serverRoot + "images/" + element.landing_image;
            final String eName = element.event;
            final String eDate = formatDateText(element.start_date, element.end_date);
            final String eDateUnformatted = element.start_date;
            final String eCity = formatLocationFromAddress(element.address);
            final String eImage = imageUrl;
            final Integer eId = element.id;
            ImageView imageView = ((ImageView) eventTile.findViewById(R.id.image));
//            Log.v("Getting Image For ", element.event);
            setImageBackground(imageUrl, imageView);
            ((TextView) eventTile.findViewById(R.id.event)).setText(eName.toUpperCase());
            ((TextView) eventTile.findViewById(R.id.date)).setText(eDate);
            ((TextView) eventTile.findViewById(R.id.city)).setText(eCity);
            ((TextView) eventTile.findViewById(R.id.type)).setText("recent");
            ((TextView) eventTile.findViewById(R.id.type)).setBackgroundResource(R.color.setmine_blue);
            Button button = (Button)eventTile.findViewById(R.id.button);
            button.setText("Hear Sets");
            button.setBackgroundResource(R.drawable.transparent_gradient_setmine_blue);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EventDetailFragment eventDetailFragment = new EventDetailFragment();
                    eventDetailFragment.EVENT_ID = eId;
                    eventDetailFragment.EVENT_NAME = eName;
                    eventDetailFragment.EVENT_DATE = eDate;
                    eventDetailFragment.EVENT_CITY = eCity;
                    eventDetailFragment.EVENT_DATE_UNFORMATTED = eDateUnformatted;
                    eventDetailFragment.EVENT_IMAGE = eImage;
                    eventDetailFragment.EVENT_TYPE = "recent";
                    FragmentTransaction transaction = eventPageFragment.getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.eventPagerContainer, eventDetailFragment, "eventDetailFragment");
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                    ((EventPagerActivity)eventPageFragment.getActivity()).setsManager.clearPlaylist();
                }
            });
            ((ViewGroup) parentView).addView(eventTile);
        }
        return parentView;
    }

    public View modelsToEventSearchTiles(List<Model> models, View rootView) {
        List<View> views = new ArrayList<View>();
        View parentView = rootView.findViewById(R.id.searchResults);
        LayoutInflater inflater = ((EventPagerActivity) parentView.getContext()).getLayoutInflater();
        for(Model element : models) {
            View eventTile = inflater.inflate(R.layout.event_search_tile, null);
            String imageUrl = serverRoot + "images/" + element.landing_image;
            final String eName = element.event;
            final String eDate = formatDateText(element.start_date, element.end_date);
            final String eDateUnformatted = element.start_date;
            final String eCity = formatLocationFromAddress(element.address);
            final String eImage = imageUrl;
            final Integer eId = element.id;
            ImageView imageView = ((ImageView) eventTile.findViewById(R.id.resultImage));
            setImageBackground(imageUrl, imageView);
            ((TextView) eventTile.findViewById(R.id.eventText)).setText(element.event);
            ((TextView) eventTile.findViewById(R.id.dateText)).setText(formatDateText(element.start_date, element.end_date));
            ((TextView) eventTile.findViewById(R.id.locationText)).setText(formatLocationFromAddress(element.address));
            eventTile.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EventDetailFragment eventDetailFragment = new EventDetailFragment();
                    eventDetailFragment.EVENT_ID = eId;
                    eventDetailFragment.EVENT_NAME = eName;
                    eventDetailFragment.EVENT_DATE = eDate;
                    eventDetailFragment.EVENT_CITY = eCity;
                    eventDetailFragment.EVENT_DATE_UNFORMATTED = eDateUnformatted;
                    eventDetailFragment.EVENT_IMAGE = eImage;
                    eventDetailFragment.EVENT_TYPE = "upcoming";
                    FragmentTransaction transaction = eventPageFragment.getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.eventPagerContainer, eventDetailFragment, "eventDetailFragment");
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                }
            });
            ((ViewGroup) parentView).addView(eventTile);
        }
        return parentView;
    }

    public Date stringToDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");
        try {
            Date date = format.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String formatDateText(String startDateString, String endDateString) {
        Date startDate = stringToDate(startDateString);
        Date endDate = stringToDate(endDateString);
        String formattedDateString;
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMM' 'd");
        SimpleDateFormat monthFormat = new SimpleDateFormat("M");
        SimpleDateFormat yearFormat = new SimpleDateFormat("y");
        SimpleDateFormat dayFormat = new SimpleDateFormat("d");
        String firstDayMonth = monthFormat.format(startDate);
        String lastDayMonth = monthFormat.format(endDate);
        String yearString = yearFormat.format(startDate);
        String firstDayString = monthDayFormat.format(startDate);
        String lastDayString = dayFormat.format(endDate);
        if(dayFormat.format(startDate).equals(lastDayString)) {
            formattedDateString = firstDayString + ", " + yearString;
        }
        else if(firstDayMonth.equals(lastDayMonth)) {
            formattedDateString = firstDayString + "-" + lastDayString + ", " + yearString;
        }
        else {
            lastDayString = monthDayFormat.format(endDate);
            formattedDateString = firstDayString + " - " + lastDayString + ", " + yearString;
        }
        return formattedDateString;
    }

    public String formatLocationFromAddress(String address) {
        int comma = address.lastIndexOf(",");
        String cityState = address.substring(0, comma);
        comma = cityState.lastIndexOf(",");
        cityState = address.substring(0, comma);
        comma = cityState.lastIndexOf(",");
        if(comma == -1)
            cityState = address.substring(0, cityState.length()+4);
        else {
            cityState = address.substring(comma+2, cityState.length()+4);
        }
        return cityState;
    }

//    public String formatLocationFromLatLong(String address) {
//        String cityState = "";
//        String apiCall = "place/textsearch/json?query=" + Uri.encode(address) + "&key=" + mapsApiKey;
//        JsonApiCallTask jsonTask = new JsonApiCallTask(context, googleMapsApiRoot, this);
//        jsonTask.execute(apiCall);
//        try {
//            JSONObject jsonResponse = jsonTask.get();
//            getDetails
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        while(formattedLocation.isEmpty()) {}
//        for(String i : formattedLocation) {
//            cityState += i + ", ";
//        }
//        cityState.trim();
//        return cityState.substring(0, cityState.length()-1);
//    }

//    @Override
//    public void onApiResponse(JSONObject json, boolean detail) {
//        if(detail) {
//            Log.v("getting detail", json.toString());
//            try {
//                JSONArray results = json.getJSONArray("results");
//                JSONObject result = results.getJSONObject(0);
//                String reference = result.getString("place_id");
//                String apiCall = "place/details/json?placeid=" + reference + "&key=" + mapsApiKey;
//                JsonApiCallTask jsonTask = new JsonApiCallTask(context, googleMapsApiRoot, this);
//                jsonTask.execute(apiCall, "detail");
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        else {
//            Log.v("parsing detail json", json.toString());
//            try {
//                if(json.getString("status").equals("OK")) {
//                    JSONObject detailResult = json.getJSONObject("result");
//                    Log.v("detailResult", detailResult.toString());
//                    JSONArray addressComponents = detailResult.getJSONArray("address_components");
//                    for(int i = 0 ; i < addressComponents.length() ; i++) {
//                        JSONObject component = addressComponents.getJSONObject(i);
//                        JSONArray types = component.getJSONArray("types");
//                        for(int j = 0 ; j < types.length() ; j++) {
//                            if(types.get(j).equals("political")) {
//                                formattedLocation.add(component.getString("long_name"));
//                            }
//                            else if(formattedLocation.size() > 0) {
//                                formattedLocation.add(component.getString("short_name"));
//                            }
//                        }
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
