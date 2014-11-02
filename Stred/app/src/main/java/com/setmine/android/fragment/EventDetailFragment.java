package com.setmine.android.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.setmine.android.object.Lineup;
import com.setmine.android.object.LineupSet;
import com.setmine.android.object.Set;
import com.setmine.android.object.SetViewHolder;
import com.setmine.android.task.LineupsSetsApiCallAsyncTask;
import com.setmine.android.util.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
    public String LAST_EVENT_DATE;
    public String EVENT_TICKET;
    public int EVENT_PAID;
    public List<HashMap<String, String>> setMapsList;
    public ListView lineupContainer;
    public JSONObject savedApiResponse = null;
    public SetsManager setsManager;
    public Context context;
    public SetMineMainActivity activity;
    public List<LineupSet> currentLineupSet;
    public DateUtils dateUtils;
    DisplayImageOptions options;
    Lineup selectedLineup;

    @Override
    public void onLineupsSetsReceived(JSONObject jsonObject, String identifier) {
        Log.v("onLineupsSetsReceived", this.toString());
        ListView listView = null;
        if(identifier == "sets") {
            final List<Set> setModels = new ArrayList<Set>();
            try {
                if(jsonObject.getString("status").equals("success")) {
                    JSONObject payload = jsonObject.getJSONObject("payload");
                    JSONObject festival = null;
                    festival = payload.getJSONObject("festival");
                    JSONArray sets = festival.getJSONArray("sets");
                    for(int i = 0 ; i < sets.length() ; i++) {
                        setModels.add(new Set(sets.getJSONObject(i)));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
            rootView.findViewById(R.id.loading).setVisibility(View.GONE);
        }
        if(identifier == "lineups") {
            Log.v("Lineup received ", identifier);
            activity.modelsCP.setModel(jsonObject, "lineups");
            currentLineupSet = activity.modelsCP.getLastLineup().getLineup();
            rootView.findViewById(R.id.loading).setVisibility(View.GONE);
            lineupContainer.setAdapter(new LineupSetAdapter(currentLineupSet));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Query content provider for lineups or sets
//        If null, check async task status
//        If started, cancel all others, continue with task
//        If not started, launch task
//        Do this logic within Content Provider

        setMapsList = new ArrayList<HashMap<String, String>>();
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
        Log.v("EVENT TYPE", EVENT_TYPE.toString());
        if(EVENT_TYPE.equals("recent")) {
            new LineupsSetsApiCallAsyncTask(activity, context, activity.API_ROOT_URL, this)
                    .execute("festival?search=" + Uri.encode(EVENT_NAME), "sets");
        }
        else if(EVENT_TYPE.equals("upcoming")) {
            selectedLineup = null;
            if(activity.modelsCP.lineups.size() > 0) {
                for(int i = 0 ; i < activity.modelsCP.lineups.size() ; i++) {
                    if(activity.modelsCP.lineups.get(i).getEvent().equals(this.EVENT_NAME)) {
                        selectedLineup = (activity.modelsCP.lineups.get(i));
                        break;
                    }
                }
                if(selectedLineup == null) {
                    new LineupsSetsApiCallAsyncTask(activity, context, activity.API_ROOT_URL, this)
                            .execute("lineup/" + Uri.encode(EVENT_ID), "lineups");
                }
            }
            else {
                new LineupsSetsApiCallAsyncTask(activity, context, activity.API_ROOT_URL, this)
                        .execute("lineup/" + Uri.encode(EVENT_ID), "lineups");
            }
        }
        else {
            Log.v("Detail Fragment has no type", " ");
        }
        Log.v("Detail Fragment Created", "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.event_detail, container, false);
        lineupContainer = (ListView) rootView.findViewById(R.id.lineupContainer);
        ImageView eventImage = (ImageView)rootView.findViewById(R.id.eventImage);
        ImageLoader.getInstance().displayImage(SetMineMainActivity.S3_ROOT_URL + EVENT_IMAGE, eventImage, options);
        if(EVENT_IMAGE.equals(83)) {
            EVENT_IMAGE = "83";
        }
        ((TextView)rootView.findViewById(R.id.eventText)).setText(EVENT_NAME);
        if(EVENT_TYPE == "recent") {
            rootView.findViewById(R.id.directionsButton).setVisibility(View.GONE);
            ((TextView)rootView.findViewById(R.id.eventText)).setBackgroundResource(R.color.setmine_blue);
            ((TextView)rootView.findViewById(R.id.lineupText)).setText("Sets");
        }
        else {
            ((TextView)rootView.findViewById(R.id.eventText)).setBackgroundResource(R.color.setmine_purple);
            if(EVENT_PAID == 0) {
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
                            Log.v("Ticket Link Click Tracked", mixpanelProperties.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Uri ticketUrl = Uri.parse(EVENT_TICKET);
                        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, ticketUrl);
                        startActivity(launchBrowser);
                    }
                });
            }
        }
        ((TextView)rootView.findViewById(R.id.dateText)).setText(EVENT_DATE);
        ((TextView)rootView.findViewById(R.id.locationText)).setText(EVENT_ADDRESS);
        Log.v("actionbar:", activity.getActionBar().toString());
        lineupContainer = (ListView) rootView.findViewById(R.id.lineupContainer);
        if(selectedLineup != null) {
            lineupContainer.setAdapter(new LineupSetAdapter(selectedLineup.getLineup()));
        }
        try {
            JSONObject mixpanelProperties = new JSONObject();
            mixpanelProperties.put("id", this.EVENT_ID);
            mixpanelProperties.put("event", this.EVENT_NAME);
            activity.mixpanel.track("Event Click Through", mixpanelProperties);
            Log.v("Event Click Through Tracked", mixpanelProperties.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("Detail Fragment View created", rootView.toString());
        return rootView;
    }

    @Override
    public void onDestroyView() {
        rootView = getView();
        super.onDestroyView();
    }

    public JSONArray getSetsFromJson(JSONObject json) {
        JSONObject payload;
        JSONObject festival = null;
        JSONArray sets = null;
        try {
            payload = json.getJSONObject("payload");
            festival = payload.getJSONObject("festival");
            sets = festival.getJSONArray("sets");
            return sets;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public JSONArray getLineupFromJson(JSONObject json) {
        JSONObject payload;
        JSONArray lineup = null;
        try {
            payload = json.getJSONObject("payload");
            lineup = payload.getJSONArray("lineup");
            return lineup;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class LineupSetViewHolder {
        TextView artistText;
        TextView setTime;
        ImageView artistImage;
        View pastSetsButton;
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
            LineupSet lineupSet = lineupSets.get(position);
            if (convertView == null) {
                view = inflater.inflate(R.layout.artist_tile_upcoming, parent, false);
                holder = new LineupSetViewHolder();
                holder.setTime = (TextView) view.findViewById(R.id.setTime);
                holder.artistText = (TextView) view.findViewById(R.id.artistText);
                holder.artistImage = (ImageView) view.findViewById(R.id.artistImage);
                holder.pastSetsButton = view.findViewById(R.id.infoButton);
                view.setTag(holder);
            } else {
                holder = (LineupSetViewHolder) view.getTag();
            }

            holder.setTime.setText(dateUtils.getDayFromDate(EVENT_DATE_UNFORMATTED, lineupSet.getDay()) + " " + lineupSet.getTime());
            holder.artistText.setText(lineupSet.getArtist());
            if(lineupSet.isHasSets()) {
                holder.pastSetsButton.setVisibility(View.VISIBLE);
                holder.pastSetsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.openSearch(null);
                        activity.searchSetsFragment.searchView.setQuery(holder.artistText.getText(), false);
                    }
                });
            }

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
            Set set = sets.get(position);
            if (convertView == null) {
                if(EVENT_TYPE.equals("upcoming")) {
                    view = inflater.inflate(R.layout.artist_tile_upcoming, parent, false);
                } else if(EVENT_TYPE.equals("recent")) {
                    view = inflater.inflate(R.layout.artist_tile_recent, parent, false);
                }
                holder = new SetViewHolder();
                if(EVENT_TYPE.equals("recent")) {
                    holder.playCount = (TextView) view.findViewById(R.id.playCount);
                }
                holder.artistText = (TextView) view.findViewById(R.id.artistText);
                holder.artistImage = (ImageView) view.findViewById(R.id.artistImage);
                holder.playButton = (ImageView) view.findViewById(R.id.playButton);
                view.setTag(holder);
                view.setId(Integer.valueOf(set.getId()).intValue());
            } else {
                holder = (SetViewHolder) view.getTag();
            }

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
