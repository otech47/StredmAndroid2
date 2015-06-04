package com.setmine.android.artist;

/**
 * Created by oscarlafarga on 11/19/14.
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.ModelsContentProvider;
import com.setmine.android.player.PlayerManager;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.Constants;
import com.setmine.android.event.Event;
import com.setmine.android.set.Set;
import com.setmine.android.api.SetMineApiGetRequestAsyncTask;
import com.setmine.android.util.DateUtils;
import com.viewpagerindicator.TitlePageIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class ArtistDetailFragment extends Fragment implements ApiCaller {

    private static final String TAG = "ArtistDetailFragment";

    public Context context;
    public SetMineMainActivity activity;
    public ModelsContentProvider modelsCP;

    public FragmentManager fragmentManager;
    public ArtistPagerAdapter artistPagerAdapter;
    public ViewPager artistViewPager;

    public View rootView;
    public ImageView artistImageView;
    public TextView artistTextView;
    public View socialContainerView;
    public View upcomingEventsListView;
    public View setsListView;
    public View eventTile;

    public Artist currentArtist;
    public String artistName;
    public String artistImageUrl;

    public List<Set> detailSets;
    public List<Event> detailEvents;

    public boolean modelsReady;

    public List<HashMap<String, String>> setMapsList;
    public ListView lineupContainer;
    public PlayerManager playerManager;

    public DateUtils dateUtils;
    DisplayImageOptions options;

    // When the API Response is received, a new thread stores the data and the UI is updated
    // Using a separate thread increases performance and minimizes UI lag

    final Handler handler = new Handler();

    final Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            onModelsReady();
        }
    };

    @Override
    public void onApiResponseReceived(JSONObject jsonObject, String identifier) {
        final JSONObject finalJsonObject = jsonObject;
        final String finalIdentifier = identifier;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(modelsCP == null) {
                    modelsCP = new ModelsContentProvider();
                }
                modelsCP.setDetailSets(finalJsonObject);
                modelsCP.setDetailEvents(finalJsonObject);
                detailSets = modelsCP.getDetailSets(currentArtist.getArtist());
                detailEvents = modelsCP.getDetailEvents(currentArtist.getArtist());
                handler.post(updateUI);
            }
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modelsReady = false;

        // If savedInstance is null, the fragment is being generated directly from the activity
        // So it is safe to assume the activity has a ModelsContentProvider

        // Else, the activity may or may not be attached yet
        // Use the Bundle to re-generate artist data

        if(savedInstanceState == null) {
            this.activity = (SetMineMainActivity)getActivity();
            modelsCP = activity.modelsCP;
            Bundle arguments = getArguments();
            String currentArtistString = arguments.getString("currentArtist");
            try {
                JSONObject artistJson = new JSONObject(currentArtistString);
                currentArtist = new Artist(artistJson);
                artistName = currentArtist.getArtist();
                artistImageUrl = currentArtist.getImageUrl();
            } catch(Exception e) {
                e.printStackTrace();
            }
            new SetMineApiGetRequestAsyncTask(activity, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                            "artist/?search=" + Uri.encode(artistName), "sets");
        } else {
            String artistModel = savedInstanceState.getString("currentArtist");
            if(modelsCP == null) {
                modelsCP = new ModelsContentProvider();
            }
            try {
                JSONObject jsonArtistModel = new JSONObject(artistModel);
                currentArtist = new Artist(jsonArtistModel);
                artistName = currentArtist.getArtist();
                artistImageUrl = currentArtist.getImageUrl();
                String setsModel = savedInstanceState.getString("detailSets" + artistName);
                JSONObject jsonSetsModel = new JSONObject(setsModel);
                modelsCP.setDetailSets(jsonSetsModel);
                detailSets =  modelsCP.getDetailSets(artistName);
                String eventsModel = savedInstanceState.getString("detailEvents" + artistName);
                JSONObject jsonEventsModel = new JSONObject(eventsModel);
                modelsCP.setDetailEvents(jsonEventsModel);
                detailEvents =  modelsCP.getDetailEvents(artistName);
                onModelsReady();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.artist_detail, container, false);
        artistTextView = (TextView)rootView.findViewById(R.id.artistName);
        artistImageView = (ImageView)rootView.findViewById(R.id.artistImage);

        dateUtils = new DateUtils();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        ImageLoader.getInstance().displayImage(Constants.S3_ROOT_URL + artistImageUrl, artistImageView, options);
        artistTextView.setText(artistName);

        setSocialMediaListeners();

        // Only track Mixpanel events if created for the first time

        if(savedInstanceState == null) {
            this.activity = (SetMineMainActivity)getActivity();
            try {
                JSONObject mixpanelProperties = new JSONObject();
                mixpanelProperties.put("id", currentArtist.getId());
                mixpanelProperties.put("artist", currentArtist.getArtist());
                activity.mixpanel.track("Artist Click Through", mixpanelProperties);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Data models were ready in onCreate method, configure the Pagers with artist data now

        // If false, data models are still being generated asynchronously

        if(modelsReady) {
            configureArtistPagers();
            rootView.findViewById(R.id.detail_loading).setVisibility(View.GONE);
        }


        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentArtist", currentArtist.jsonModelString);
        outState.putString("detailSets"+ artistName, modelsCP.jsonMappings.get("detailSets"+artistName));
        outState.putString("detailEvents"+ artistName, modelsCP.jsonMappings.get("detailEvents"+artistName));
    }

    // Configure the Events/Sets ViewPager and List Adapters

    public void configureArtistPagers() {
        artistViewPager = (ViewPager)rootView.findViewById(R.id.artistDetailPager);
        artistPagerAdapter = new ArtistPagerAdapter(getChildFragmentManager(), currentArtist);
        artistViewPager.setAdapter(artistPagerAdapter);
        final TitlePageIndicator titlePageIndicator = (TitlePageIndicator)rootView.findViewById(R.id.titleTabs);
        titlePageIndicator.setTextSize((float)(titlePageIndicator.getTextSize()*0.8));
        titlePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageSelected(int i) {
                if(i == 0) {
                    titlePageIndicator.setFooterColor(getResources().getColor(R.color.setmine_blue));
                } else {
                    titlePageIndicator.setFooterColor(getResources().getColor(R.color.setmine_purple));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });
        titlePageIndicator.setViewPager(artistViewPager);
    }

    // Execute when the data models are ready to be sent into the UI

    public void onModelsReady() {

        // Make sure onViewCreated has completed and created the rootView

        if(rootView != null) {
            configureArtistPagers();
            rootView.findViewById(R.id.detail_loading).setVisibility(View.GONE);
        }

        // Set modelsReady so onCreateView knows data is ready for the UI

        modelsReady = true;
    }

    // Set Facebook, Twitter, and Web Link actions and Mixpanel event tracking

    public void setSocialMediaListeners() {
        rootView.findViewById(R.id.facebookButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", currentArtist.getId());
                    mixpanelProperties.put("artist", artistName);
                    activity.mixpanel.track("Facebook Link Clicked", mixpanelProperties);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Uri fbUrl = Uri.parse(currentArtist.getFacebookLink());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, fbUrl);
                startActivity(launchBrowser);
            }
        });
        rootView.findViewById(R.id.twitterButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", currentArtist.getId());
                    mixpanelProperties.put("artist", artistName);
                    activity.mixpanel.track("Twitter Link Clicked", mixpanelProperties);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Uri twitterUrl = Uri.parse(currentArtist.getTwitterLink());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, twitterUrl);
                startActivity(launchBrowser);
            }
        });
        rootView.findViewById(R.id.webButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", currentArtist.getId());
                    mixpanelProperties.put("artist", artistName);
                    activity.mixpanel.track("Web Link Clicked", mixpanelProperties);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Uri webUrl = Uri.parse(currentArtist.getWebLink());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, webUrl);
                startActivity(launchBrowser);
            }
        });
//        if(currentArtist.getBio().length() > 2) {
//            ((TextView)rootView.findViewById(R.id.bio)).setText(currentArtist.getBio());
//        }
    }
}

