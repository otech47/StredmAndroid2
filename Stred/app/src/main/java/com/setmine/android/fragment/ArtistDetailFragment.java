package com.setmine.android.fragment;

/**
 * Created by oscarlafarga on 11/19/14.
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.setmine.android.LineupsSetsApiCaller;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.SetsManager;
import com.setmine.android.adapter.ArtistPagerAdapter;
import com.setmine.android.object.Artist;
import com.setmine.android.object.LineupSet;
import com.setmine.android.task.LineupsSetsApiCallAsyncTask;
import com.setmine.android.util.DateUtils;
import com.viewpagerindicator.TitlePageIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArtistDetailFragment extends Fragment implements LineupsSetsApiCaller {

    public Context context;
    public SetMineMainActivity activity;
    public FragmentManager fragmentManager;
    public ArtistPagerAdapter artistPagerAdapter;
    public ViewPager artistViewPager;

    public View rootView;
    public ImageView artistImageView;
    public TextView artistTextView;
    public View socialContaineViewr;
    public View upcomingEventsListView;
    public View setsListView;
    public View eventTile;

    private static final String amazonS3Url = "http://setmine.s3-website-us-east-1.amazonaws.com/namecheap/";
    public Artist selectedArtist;

    public String artistName;
    public String artistImageUrl;

    public List<HashMap<String, String>> setMapsList;
    public ListView lineupContainer;
    public JSONObject savedApiResponse = null;
    public SetsManager setsManager;

    public List<LineupSet> currentLineupSet;
    public DateUtils dateUtils;
    DisplayImageOptions options;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setMapsList = new ArrayList<HashMap<String, String>>();
        setsManager = ((SetMineMainActivity)getActivity()).setsManager;
        context = getActivity().getApplicationContext();
        activity = (SetMineMainActivity)getActivity();
        fragmentManager = activity.fragmentManager;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        dateUtils = new DateUtils();

        artistName = selectedArtist.getArtist();
        artistImageUrl = selectedArtist.getImageUrl();

        Log.v("Artist Detail Fragment Created", this.toString());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.artist_detail, container, false);
        artistTextView = (TextView)rootView.findViewById(R.id.artistName);
        artistImageView = (ImageView)rootView.findViewById(R.id.artistImage);

        ImageLoader.getInstance().displayImage(SetMineMainActivity.S3_ROOT_URL + artistImageUrl, artistImageView, options);
        artistTextView.setText(artistName);
        rootView.findViewById(R.id.facebookButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", selectedArtist.getId());
                    mixpanelProperties.put("artist", artistName);
                    activity.mixpanel.track("Facebook Link Clicked", mixpanelProperties);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Uri fbUrl = Uri.parse(selectedArtist.getFacebookLink());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, fbUrl);
                startActivity(launchBrowser);
            }
        });
        rootView.findViewById(R.id.twitterButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", selectedArtist.getId());
                    mixpanelProperties.put("artist", artistName);
                    activity.mixpanel.track("Twitter Link Clicked", mixpanelProperties);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Uri twitterUrl = Uri.parse(selectedArtist.getTwitterLink());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, twitterUrl);
                startActivity(launchBrowser);
            }
        });
        rootView.findViewById(R.id.webButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", selectedArtist.getId());
                    mixpanelProperties.put("artist", artistName);
                    activity.mixpanel.track("Web Link Clicked", mixpanelProperties);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Uri webUrl = Uri.parse(selectedArtist.getWebLink());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, webUrl);
                startActivity(launchBrowser);
            }
        });
//        if(selectedArtist.getBio().length() > 2) {
//            ((TextView)rootView.findViewById(R.id.bio)).setText(selectedArtist.getBio());
//        }

        if(activity.modelsCP.detailEvents.containsKey(artistName) &&
                activity.modelsCP.detailSets.containsKey(artistName)) {
            finishCreateView();
        } else {
            new LineupsSetsApiCallAsyncTask(activity, context, activity.API_ROOT_URL, this)
                    .execute("artist?search=" + Uri.encode(artistName), "sets");
        }

        try {
            JSONObject mixpanelProperties = new JSONObject();
            mixpanelProperties.put("id", selectedArtist.getId());
            mixpanelProperties.put("artist", selectedArtist.getArtist());
            activity.mixpanel.track("Artist Click Through", mixpanelProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v("Artist Detail Fragment View created", rootView.toString());
        return rootView;
    }

    @Override
    public void onDestroyView() {
        rootView = getView();
        super.onDestroyView();
    }

    @Override
    public void onLineupsSetsReceived(JSONObject jsonObject, String identifier) {
        Log.v("Artist Detail onLineupsSetsReceived", jsonObject.toString());
        activity.modelsCP.setDetailSets(jsonObject);
        activity.modelsCP.setDetailEvents(jsonObject);
        finishCreateView();

    }

    public void finishCreateView() {
        artistViewPager = (ViewPager)rootView.findViewById(R.id.artistDetailPager);
        artistPagerAdapter = new ArtistPagerAdapter(getChildFragmentManager(), selectedArtist);
        rootView.findViewById(R.id.detail_loading).setVisibility(View.GONE);
        artistViewPager.setAdapter(artistPagerAdapter);
        final TitlePageIndicator titlePageIndicator = (TitlePageIndicator)rootView.findViewById(R.id.titleTabs);
        titlePageIndicator.setTextSize((float)(titlePageIndicator.getTextSize()*0.8));
        titlePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {}

            @Override
            public void onPageSelected(int i) {
                if(i == 0) {
                    titlePageIndicator.setFooterColor(getResources().getColor(R.color.setmine_purple));
                } else {
                    titlePageIndicator.setFooterColor(getResources().getColor(R.color.setmine_blue));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });
        titlePageIndicator.setViewPager(artistViewPager);


    }
}

