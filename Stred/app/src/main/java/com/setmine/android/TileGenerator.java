package com.setmine.android;

import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.setmine.android.fragment.EventDetailFragment;
import com.setmine.android.object.Event;
import com.setmine.android.object.ImageViewChangeRequest;
import com.setmine.android.object.LineupSet;
import com.setmine.android.object.Set;
import com.setmine.android.task.GetImageAsyncTask;
import com.setmine.android.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarlafarga on 9/18/14.
 */
public class TileGenerator {

    private static final String serverRoot = "http://setmine.com/";
    private static final String mapsApiKey = "AIzaSyAUoaqRaUBFZISnQoVgkgSMKkg7XEwctxU";
    private static final String mapsAndroidApiKey = "AIzaSyANSSNDva7xtMp_VOaPPq925KiFvAdCPlY";
    private static final String googleMapsApiRoot = "https://maps.googleapis.com/maps/api/";
    private Context context;
    public ImageCache imageCache;
    public int imagesLoaded = 0;
    public com.setmine.android.fragment.EventPageFragment eventPageFragment;
    public List<String> formattedLocation = new ArrayList<String>();
    public SetMineMainActivity activity;
    public String lastEventDate;
    public DateUtils dateUtils;

    public TileGenerator(SetMineMainActivity activity, Context context, ImageCache imageCache) {
        this.activity = activity;
        this.context = context;
        this.imageCache = imageCache;
        this.dateUtils = new DateUtils();
    }

    public List<View> modelsToSetTiles(List<Set> models) {
        List<View> tiles = new ArrayList<View>();
        LayoutInflater inflater = activity.getLayoutInflater();
        for(Set s : models) {
            activity.setsManager.addToPlaylist(s);
            View artistTile = inflater.inflate(R.layout.artist_tile_recent, null);
            ((TextView) artistTile.findViewById(R.id.playCount)).setText(s.getPopularity() + " plays");
            ((TextView) artistTile.findViewById(R.id.artistText)).setText(s.getArtist());
            if(!(s.getArtistImage().equals("null"))) {
                if(models.size() < 90) {
                    new GetImageAsyncTask(activity, imageCache, activity.S3_ROOT_URL)
                            .executeOnExecutor(GetImageAsyncTask.THREAD_POOL_EXECUTOR, new ImageViewChangeRequest(s.getArtistImage(), (ImageView) artistTile.findViewById(R.id.artistImage)));
                }
                else {
                    new GetImageAsyncTask(activity, imageCache, activity.S3_ROOT_URL)
                            .executeOnExecutor(GetImageAsyncTask.SERIAL_EXECUTOR, new ImageViewChangeRequest(s.getArtistImage(), (ImageView) artistTile.findViewById(R.id.artistImage)));
                }
            }
            artistTile.setTag(s.getId());
            tiles.add(artistTile);
        }
        return tiles;
    }

    public List<View> modelsToLineupTiles(List<LineupSet> models) {
        List<View> tiles = new ArrayList<View>();
        LayoutInflater inflater = activity.getLayoutInflater();
        for(LineupSet l : models) {
            View artistTile = inflater.inflate(R.layout.artist_tile_upcoming, null);
            ((TextView) artistTile.findViewById(R.id.setTime)).setText(dateUtils.getDayFromDate(lastEventDate, l.getDay()) + " " + l.getTime());
            ((TextView) artistTile.findViewById(R.id.artistText)).setText(l.getArtist());
            if(!(l.getArtistImage().equals("null"))) {
                if(models.size() < 60) {
                    new GetImageAsyncTask(activity, imageCache, activity.S3_ROOT_URL)
                            .executeOnExecutor(GetImageAsyncTask.THREAD_POOL_EXECUTOR, new ImageViewChangeRequest(l.getArtistImage(), (ImageView) artistTile.findViewById(R.id.artistImage)));
                }
                else {
                    new GetImageAsyncTask(activity, imageCache, activity.S3_ROOT_URL)
                            .executeOnExecutor(GetImageAsyncTask.SERIAL_EXECUTOR, new ImageViewChangeRequest(l.getArtistImage(), (ImageView) artistTile.findViewById(R.id.artistImage)));
                }
            }
            artistTile.setTag(l.getArtist());
            tiles.add(artistTile);
        }
        return tiles;
    }

    public List<View> modelsToUpcomingEventTiles(List<Event> models) {
        List<View> tiles = new ArrayList<View>();
        LayoutInflater inflater = activity.getLayoutInflater();
        for(Event element : models) {
            View eventTile = inflater.inflate(R.layout.event_tile_upcoming, null);
            String imageUrl = element.mainImageUrl;
            ImageView imageView = ((ImageView) eventTile.findViewById(R.id.image));
            new GetImageAsyncTask(activity, imageCache, activity.PUBLIC_ROOT_URL + "images/").executeOnExecutor(GetImageAsyncTask.THREAD_POOL_EXECUTOR, new ImageViewChangeRequest(imageUrl, imageView));
            final String eName = element.event;
            final String eDate = dateUtils.formatDateText(element.startDate, element.endDate);
            final String eCity = dateUtils.getCityStateFromAddress(element.address);
            final String eImage = imageUrl;
            final String eId = element.id;
            final String eDateUnformatted = element.startDate;
            ((TextView) eventTile.findViewById(R.id.event)).setText(eName.toUpperCase());
            ((TextView) eventTile.findViewById(R.id.date)).setText(eDate);
            ((TextView) eventTile.findViewById(R.id.city)).setText(eCity);
            eventTile.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EventDetailFragment eventDetailFragment = new EventDetailFragment();
                    eventDetailFragment.EVENT_ID = eId;
                    eventDetailFragment.EVENT_NAME = eName;
                    eventDetailFragment.EVENT_DATE_FORMATTED = eDate;
                    eventDetailFragment.EVENT_START_DATE_UNFORMATTED = eDateUnformatted;
                    eventDetailFragment.EVENT_ADDRESS = eCity;
                    eventDetailFragment.EVENT_IMAGE = eImage;
                    eventDetailFragment.EVENT_TYPE = "upcoming";
                    lastEventDate = eDateUnformatted;
                    FragmentTransaction transaction = activity.fragmentManager.beginTransaction();
                    transaction.replace(R.id.eventPagerContainer, eventDetailFragment, "eventDetailFragment");
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                }
            });
            tiles.add(eventTile);
        }
        return tiles;
    }

    public List<View> modelsToRecentEventTiles(List<Event> models) {
        List<View> tiles = new ArrayList<View>();
        LayoutInflater inflater = activity.getLayoutInflater();
        for(Event element : models) {
            View eventTile = inflater.inflate(R.layout.event_tile_recent, null);
            String imageUrl = element.mainImageUrl;
            ImageView imageView = ((ImageView) eventTile.findViewById(R.id.image));
            new GetImageAsyncTask(activity, imageCache, activity.PUBLIC_ROOT_URL + "images/").executeOnExecutor(GetImageAsyncTask.THREAD_POOL_EXECUTOR, new ImageViewChangeRequest(imageUrl, imageView));
            final String eName = element.event;
            final String eDate = dateUtils.formatDateText(element.startDate, element.endDate);
            final String eCity = dateUtils.getCityStateFromAddress(element.address);
            final String eImage = imageUrl;
            final String eId = element.id;
            final String eDateUnformatted = element.startDate;
            ((TextView) eventTile.findViewById(R.id.event)).setText(eName.toUpperCase());
            ((TextView) eventTile.findViewById(R.id.date)).setText(eDate);
            ((TextView) eventTile.findViewById(R.id.city)).setText(eCity);
            eventTile.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EventDetailFragment eventDetailFragment = new EventDetailFragment();
                    eventDetailFragment.EVENT_ID = eId;
                    eventDetailFragment.EVENT_NAME = eName;
                    eventDetailFragment.EVENT_DATE_FORMATTED = eDate;
                    eventDetailFragment.EVENT_START_DATE_UNFORMATTED = eDateUnformatted;
                    eventDetailFragment.EVENT_ADDRESS = eCity;
                    eventDetailFragment.EVENT_IMAGE = eImage;
                    eventDetailFragment.EVENT_TYPE = "recent";
                    lastEventDate = eDateUnformatted;
                    FragmentTransaction transaction = activity.fragmentManager.beginTransaction();
                    transaction.replace(R.id.eventPagerContainer, eventDetailFragment, "eventDetailFragment");
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                    activity.setsManager.clearPlaylist();
                }
            });
            tiles.add(eventTile);
        }
        return tiles;
    }

    public List<View> modelsToEventSearchTiles(List<Event> models) {
        List<View> tiles = new ArrayList<View>();
        LayoutInflater inflater = activity.getLayoutInflater();
        for(Event element : models) {
            View eventTile = inflater.inflate(R.layout.event_search_tile, null);
            String imageUrl = element.mainImageUrl;
            ImageView imageView = ((ImageView) eventTile.findViewById(R.id.image));
            new GetImageAsyncTask(activity, imageCache, activity.PUBLIC_ROOT_URL + "images/").executeOnExecutor(GetImageAsyncTask.THREAD_POOL_EXECUTOR, new ImageViewChangeRequest(imageUrl, imageView));
            final String eName = element.event;
            final String eDate = dateUtils.formatDateText(element.startDate, element.endDate);
            final String eCity = dateUtils.getCityStateFromAddress(element.address);
            final String eImage = imageUrl;
            final String eId = element.id;
            final String eDateUnformatted = element.startDate;
            ((TextView) eventTile.findViewById(R.id.eventText)).setText(element.event);
            ((TextView) eventTile.findViewById(R.id.dateText)).setText(eDate);
            ((TextView) eventTile.findViewById(R.id.locationText)).setText(eCity);
            eventTile.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EventDetailFragment eventDetailFragment = new EventDetailFragment();
                    eventDetailFragment.EVENT_ID = eId;
                    eventDetailFragment.EVENT_NAME = eName;
                    eventDetailFragment.EVENT_DATE_FORMATTED = eDate;
                    eventDetailFragment.EVENT_START_DATE_UNFORMATTED = eDateUnformatted;
                    eventDetailFragment.EVENT_ADDRESS = eCity;
                    eventDetailFragment.EVENT_IMAGE = eImage;
                    eventDetailFragment.EVENT_TYPE = "upcoming";
                    lastEventDate = eDateUnformatted;
                    FragmentTransaction transaction = activity.fragmentManager.beginTransaction();
                    transaction.replace(R.id.eventPagerContainer, eventDetailFragment, "eventDetailFragment");
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                }
            });
            tiles.add(eventTile);
        }
        return tiles;
    }


//    public String formatLocationFromLatLong(String address) {
//        String cityState = "";
//        String apiCall = "place/textsearch/json?query=" + Uri.encode(address) + "&key=" + mapsApiKey;
//        JsonApiCallAsyncTask jsonTask = new JsonApiCallAsyncTask(context, googleMapsApiRoot, this);
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
//                JsonApiCallAsyncTask jsonTask = new JsonApiCallAsyncTask(context, googleMapsApiRoot, this);
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
