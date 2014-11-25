package com.setmine.android.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.setmine.android.LineupsSetsApiCaller;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.SetsManager;
import com.setmine.android.object.Artist;
import com.setmine.android.object.Lineup;
import com.setmine.android.object.LineupSet;
import com.setmine.android.object.Set;
import com.setmine.android.object.SetViewHolder;
import com.setmine.android.task.LineupsSetsApiCallAsyncTask;
import com.setmine.android.util.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by oscarlafarga on 9/22/14.
 */
public class EventDetailFragment extends Fragment implements LineupsSetsApiCaller {

    public View rootView;
    private static final String amazonS3Url = "http://setmine.s3-website-us-east-1.amazonaws.com/namecheap/";
    public String EVENT_ID;
    public String EVENT_NAME;
    public String EVENT_DATE;
    public String EVENT_DATE_UNFORMATTED;
    public String EVENT_ADDRESS;
    public String EVENT_IMAGE;
    public String EVENT_TYPE;
    public String EVENT_TICKET;
    public int EVENT_PAID;
    public ListView lineupContainer;
    public JSONObject savedApiResponse = null;
    public SetsManager setsManager;
    public Context context;
    public SetMineMainActivity activity;
    public List<LineupSet> currentLineupSet;
    public DateUtils dateUtils;
    DisplayImageOptions options;
    Lineup selectedLineup = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("Event Detail Fragment Created", "");
        setsManager = ((SetMineMainActivity)getActivity()).setsManager;
        context = getActivity().getApplicationContext();
        activity = (SetMineMainActivity)getActivity();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        dateUtils = new DateUtils();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.event_detail, container, false);
        lineupContainer = (ListView) rootView.findViewById(R.id.lineupContainer);
        ImageView eventImage = (ImageView)rootView.findViewById(R.id.eventImage);
        ImageLoader.getInstance().displayImage(SetMineMainActivity.S3_ROOT_URL + EVENT_IMAGE, eventImage, options);
        ((TextView)rootView.findViewById(R.id.dateText)).setText(EVENT_DATE);
        ((TextView)rootView.findViewById(R.id.locationText)).setText(EVENT_ADDRESS);
        lineupContainer = (ListView) rootView.findViewById(R.id.lineupContainer);
        if(EVENT_IMAGE.equals(83)) {
            EVENT_IMAGE = "83";
        }
        ((TextView)rootView.findViewById(R.id.eventText)).setText(EVENT_NAME);
        if(EVENT_TYPE == "recent") {
            rootView.findViewById(R.id.directionsButton).setVisibility(View.GONE);
            ((TextView)rootView.findViewById(R.id.eventText)).setBackgroundResource(R.color.setmine_blue);
            ((TextView)rootView.findViewById(R.id.lineupText)).setText("Sets");
            if(activity.modelsCP.detailSets.containsKey(EVENT_NAME)) {
                finishCreateView();
            } else {
                new LineupsSetsApiCallAsyncTask(activity, context, activity.API_ROOT_URL, this)
                        .execute("festival?search=" + Uri.encode(EVENT_NAME), "sets");
            }
        }
        else {
            ((TextView)rootView.findViewById(R.id.eventText)).setBackgroundResource(R.color.setmine_purple);
            if(EVENT_PAID == 1) {
                Button buyTickets = (Button)rootView.findViewById(R.id.button_buy_tickets);
                buyTickets.setVisibility(View.VISIBLE);
                buyTickets.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            JSONObject mixpanelProperties = new JSONObject();
                            mixpanelProperties.put("id", EVENT_ID);
                            mixpanelProperties.put("event", EVENT_NAME);
                            activity.mixpanel.track("Ticket Link Clicked", mixpanelProperties);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Uri ticketUrl = Uri.parse(EVENT_TICKET);
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, ticketUrl);
                        startActivity(launchBrowser);
                    }
                });
            }
            if(activity.modelsCP.lineups.containsKey(EVENT_NAME)) {
                finishCreateView();
            } else {
                new LineupsSetsApiCallAsyncTask(activity, context, activity.API_ROOT_URL, this)
                        .execute("lineup/" + Uri.encode(EVENT_ID), "lineups");
            }
        }

        try {
            JSONObject mixpanelProperties = new JSONObject();
            mixpanelProperties.put("id", this.EVENT_ID);
            mixpanelProperties.put("event", this.EVENT_NAME);
            activity.mixpanel.track("Event Click Through", mixpanelProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.v("Event Detail Fragment View created", rootView.toString());
        return rootView;
    }

    @Override
    public void onDestroyView() {
        rootView = getView();
        super.onDestroyView();
    }

    // Implement Lineup/Sets API Response Callback

    @Override
    public void onLineupsSetsReceived(JSONObject jsonObject, String identifier) {
        Log.v("Event Detail onLineupsSetsReceived", this.toString());
        if(identifier == "sets") {
            activity.modelsCP.setDetailSets(jsonObject);
        }
        if(identifier == "lineups") {
            activity.modelsCP.setLineups(jsonObject);
        }
        finishCreateView();
    }

    // Models must be valid before executing this method

    public void finishCreateView() {
        if(EVENT_TYPE == "recent") {
            final List<Set> setModels = activity.modelsCP.getDetailSets(EVENT_NAME);
            lineupContainer.setAdapter(new SetAdapter(setModels));
            lineupContainer.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    v.setPressed(true);
                    Set s = setModels.get(position);
                    ((SetMineMainActivity)getActivity()).startPlayerFragment(Integer.parseInt(s.getId()));
                }
            });
            activity.setsManager.setPlaylist(setModels);
            activity.playlistFragment.updatePlaylist();
        } else {
            selectedLineup = activity.modelsCP.getLineups(EVENT_NAME);
            currentLineupSet = selectedLineup.getLineup();
            rootView.findViewById(R.id.loading).setVisibility(View.GONE);
            lineupContainer.setAdapter(new LineupSetAdapter(currentLineupSet));
            lineupContainer.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    v.setPressed(true);
                    String artistName = currentLineupSet.get(position).getArtist();
                    Artist currentArtist = null;
                    List<Artist> allArtists = activity.modelsCP.getAllArtists();
                    for(int i = 0 ; i < allArtists.size() ; i++) {
                        if(allArtists.get(i).getArtist().equals(artistName)) {
                            currentArtist = allArtists.get(i);
                        }
                    }
                    ArtistDetailFragment artistDetailFragment = new ArtistDetailFragment();
                    artistDetailFragment.selectedArtist = currentArtist;
                    FragmentTransaction transaction = activity.fragmentManager.beginTransaction();
                    transaction.replace(R.id.eventPagerContainer, artistDetailFragment, "artistDetailFragment");
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                }
            });
        }
        rootView.findViewById(R.id.loading).setVisibility(View.GONE);
    }

    private static class LineupSetViewHolder {
        TextView artistText;
        TextView setTime;
        ImageView artistImage;
        View detailActionButton;
    }

    class LineupSetAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
        private List<LineupSet> lineupSets;

        LineupSetAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        LineupSetAdapter(List<LineupSet> LineupSets) {
            this();
            lineupSets = LineupSets;
        }

        @Override
        public int getCount() {
            return lineupSets.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final LineupSetViewHolder holder;
            final LineupSet lineupSet = lineupSets.get(position);
            if (convertView == null) {
                view = inflater.inflate(R.layout.artist_tile_upcoming, parent, false);
                holder = new LineupSetViewHolder();
                holder.setTime = (TextView) view.findViewById(R.id.setTime);
                holder.artistText = (TextView) view.findViewById(R.id.artistText);
                holder.artistImage = (ImageView) view.findViewById(R.id.artistImage);
                holder.detailActionButton = view.findViewById(R.id.detailActionButton);
                view.setTag(holder);
            } else {
                holder = (LineupSetViewHolder) view.getTag();
            }

            holder.setTime.setText(dateUtils.getDayFromDate(EVENT_DATE_UNFORMATTED, lineupSet.getDay()) + " " + lineupSet.getTime());
            holder.artistText.setText(lineupSet.getArtist());
            holder.detailActionButton.setVisibility(View.GONE);

            ImageLoader.getInstance().displayImage(activity.S3_ROOT_URL + lineupSet.getArtistImage(), holder.artistImage, options, animateFirstListener);

            return view;
        }
    }

    class SetAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
        private List<Set> sets;

        SetAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        SetAdapter(List<Set> Sets) {
            this();
            sets = Sets;
        }

        @Override
        public int getCount() {
            return sets.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final SetViewHolder holder;
            final Set set = sets.get(position);
            if (convertView == null) {
                view = inflater.inflate(R.layout.artist_tile_recent, parent, false);
                holder = new SetViewHolder();
                if(EVENT_TYPE.equals("recent")) {
                    holder.playCount = (TextView) view.findViewById(R.id.playCount);
                }
                holder.artistText = (TextView) view.findViewById(R.id.artistText);
                holder.artistImage = (ImageView) view.findViewById(R.id.artistImage);
                holder.detailActionButton = view.findViewById(R.id.detailActionButton);
                view.setTag(holder);
                view.setId(Integer.valueOf(set.getId()).intValue());
            } else {
                holder = (SetViewHolder) view.getTag();
            }
            holder.detailActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Artist currentArtist = null;
                    for (int i = 0; i < activity.modelsCP.getArtists().size(); i++) {
                        if (activity.modelsCP.getArtists().get(i)
                                .getArtist().equals(set.getArtist())) {
                            currentArtist = activity.modelsCP.getArtists().get(i);
                        }
                    }
                    ArtistDetailFragment artistDetailFragment = new ArtistDetailFragment();
                    artistDetailFragment.selectedArtist = currentArtist;
                    FragmentTransaction transaction = activity.fragmentManager.beginTransaction();
                    transaction.replace(R.id.eventPagerContainer, artistDetailFragment, "artistDetailFragment");
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                }
            });

            if(holder.playCount != null) {
                holder.playCount.setText(set.getPopularity() + " plays");
            }
            holder.artistText.setText(set.getArtist());

            ImageLoader.getInstance().displayImage(activity.S3_ROOT_URL + set.getArtistImage(), holder.artistImage, options, animateFirstListener);

            return view;
        }
    }

    public static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }
}
