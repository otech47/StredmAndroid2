package com.setmine.android.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.setmine.android.ApiCaller;
import com.setmine.android.ModelsContentProvider;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.object.Artist;
import com.setmine.android.object.Constants;
import com.setmine.android.object.Event;
import com.setmine.android.object.Lineup;
import com.setmine.android.object.LineupSet;
import com.setmine.android.object.Set;
import com.setmine.android.object.SetViewHolder;
import com.setmine.android.task.SetMineApiGetRequestAsyncTask;
import com.setmine.android.util.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by oscarlafarga on 9/22/14.
 */
public class EventDetailFragment extends Fragment implements ApiCaller {

    private static final String TAG = "EventDetailFragment";

    public View rootView;
    public ImageView eventImageView;
    public ListView lineupContainerView;

    public Event currentEvent;
    public String EVENT_TYPE;
    public boolean modelsReady;

    public Context context;
    public SetMineMainActivity activity;
    public ModelsContentProvider modelsCP;

    public Lineup detailLineups;
    public List<LineupSet> currentLineupSet;

    public List<Set> detailSets;

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
                if(finalIdentifier.equals("sets")) {
                    modelsCP.setDetailSets(finalJsonObject);
                    detailSets = modelsCP.getDetailSets(currentEvent.getEvent());
                }
                if(finalIdentifier.equals("lineups")) {
                    modelsCP.setLineups(finalJsonObject);
                    detailLineups = modelsCP.getLineups(currentEvent.getEvent());
                }
                handler.post(updateUI);
            }
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        modelsReady = false;

        // If savedInstance is null, the fragment is being generated directly from the activity
        // So it is safe to assume the activity has a ModelsContentProvider

        // Else, the activity may or may not be attached yet
        // Use the Bundle to re-generate event data

        if(savedInstanceState == null) {
            this.activity = (SetMineMainActivity)getActivity();
            modelsCP = activity.modelsCP;
            Bundle arguments = getArguments();
            EVENT_TYPE = arguments.getString("eventType");
            String eventJsonString = arguments.getString("currentEvent");
            try {
                JSONObject eventJson = new JSONObject(eventJsonString);
                currentEvent = new Event(eventJson);
            } catch(Exception e) {
                e.printStackTrace();
            }
            if(EVENT_TYPE == "recent") {
                new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                                "festival?search=" + Uri.encode(currentEvent.getEvent()), "sets");
            } else {
                new SetMineApiGetRequestAsyncTask(activity, this)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                                "lineup/" + Uri.encode(currentEvent.getId()), "lineups");
            }
        } else {
            String eventModel = savedInstanceState.getString("currentEvent");
            EVENT_TYPE = savedInstanceState.getString("eventType");
            if(modelsCP == null) {
                modelsCP = new ModelsContentProvider();
            }
            try {
                JSONObject jsonEventModel = new JSONObject(eventModel);
                currentEvent = new Event(jsonEventModel);
                if(EVENT_TYPE == "recent") {
                    String setsModel = savedInstanceState.getString("detailSets" + currentEvent.getEvent());
                    JSONObject jsonSetsModel = new JSONObject(setsModel);
                    modelsCP.setDetailSets(jsonSetsModel);
                    detailSets = modelsCP.getDetailSets(currentEvent.getEvent());
                } else {
                    String lineupsModel = savedInstanceState.getString("detailLineups" + currentEvent.getEvent());
                    JSONObject jsonLineupsModel = new JSONObject(lineupsModel);
                    modelsCP.setLineups(jsonLineupsModel);
                    detailLineups =  modelsCP.getLineups(currentEvent.getEvent());
                }
                onModelsReady();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.event_detail, container, false);
        lineupContainerView = (ListView) rootView.findViewById(R.id.lineupContainer);
        eventImageView = (ImageView)rootView.findViewById(R.id.eventImage);

        options =  new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        dateUtils = new DateUtils();

        // Set text and for event details

        ImageLoader.getInstance().displayImage(currentEvent.getMainImageUrl(), eventImageView, options);
        ((TextView)rootView.findViewById(R.id.dateText)).setText(currentEvent.getDateFormatted());
        ((TextView)rootView.findViewById(R.id.locationText)).setText(currentEvent.getVenue() + ", "
                + dateUtils.getCityStateFromAddress(currentEvent.getAddress()));
        ((TextView)rootView.findViewById(R.id.eventText)).setText(currentEvent.getEvent());

        // Customize detail page based on recent or upcoming

        if(EVENT_TYPE == "recent") {
            ((TextView)rootView.findViewById(R.id.eventText)).setBackgroundResource(R.color.setmine_blue);
            ((TextView)rootView.findViewById(R.id.lineupText)).setText("Sets");

            rootView.findViewById(R.id.directionsButton).setVisibility(View.GONE);

            // Set Facebook Click Listeners for past events

            rootView.findViewById(R.id.facebookButton).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.facebookButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        JSONObject mixpanelProperties = new JSONObject();
                        mixpanelProperties.put("id", currentEvent.getId());
                        mixpanelProperties.put("event", currentEvent.getEvent());
                        activity.mixpanel.track("Facebook Link Clicked", mixpanelProperties);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Uri fbUrl = Uri.parse(currentEvent.getFacebookLink());
                    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, fbUrl);
                    startActivity(launchBrowser);
                }
            });

        } else {
            ((TextView)rootView.findViewById(R.id.eventText)).setBackgroundResource(R.color.setmine_purple);

            // Set Facebook and Ticket Click Listeners and functions if it is a paid promoted event

            if(currentEvent.getPaid() == 1) {
                configurePaidButtons();
            }
        }

        // Set Twitter and Web click listeners for all events past, upcoming (paid and unpaid)

        configureSocialMediaButtons();

        // Only track Mixpanel events if created for the first time

        if(savedInstanceState == null) {
            try {
                JSONObject mixpanelProperties = new JSONObject();
                mixpanelProperties.put("id", currentEvent.getId());
                mixpanelProperties.put("event", currentEvent.getEvent());
                activity.mixpanel.track("Event Click Through", mixpanelProperties);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Data models were ready in onCreate method, configure the Pagers with artist data now

        // If false, data models are currently being generated asynchronously

        if(modelsReady && activity != null) {
            setAdapters();
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v(TAG, "onActivityCreated");
        this.activity = (SetMineMainActivity)getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentEvent", currentEvent.jsonModelString);
        outState.putString("eventType", EVENT_TYPE);
        if(EVENT_TYPE.equals("recent")) {
            outState.putString("detailSets"+currentEvent.getEvent(),
                    modelsCP.jsonMappings.get("detailSets"+currentEvent.getEvent()));
        } else {
            outState.putString("detailLineups"+currentEvent.getEvent(),
                    modelsCP.jsonMappings.get("detailLineups"+currentEvent.getEvent()));
        }
    }

    public void setAdapters() {
        if(EVENT_TYPE == "recent") {
            lineupContainerView.setAdapter(new SetAdapter(detailSets));
            lineupContainerView.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    v.setPressed(true);
                    activity.playerService.playerManager.setPlaylist(detailSets);
                    Set s = detailSets.get(position);
                    activity.playerService.playerManager.selectSetById(s.getId());
                }
            });
        } else {
            currentLineupSet = detailLineups.getLineup();
            lineupContainerView.setAdapter(new LineupSetAdapter(currentLineupSet));
            lineupContainerView.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    v.setPressed(true);
                    String artistName = currentLineupSet.get(position).getArtist();
                    Artist artist = null;
                    List<Artist> allArtists = modelsCP.getAllArtists();
                    for (int i = 0; i < allArtists.size(); i++) {
                        if (allArtists.get(i).getArtist().equals(artistName)) {
                            artist = allArtists.get(i);
                        }
                    }
                    activity.openArtistDetailPage(artist);
                }
            });
        }
        rootView.findViewById(R.id.loading).setVisibility(View.GONE);
    }

    // Set Click listeners and Mixpanel event tracking for Facebook and Ticket links

    public void configurePaidButtons() {
        rootView.findViewById(R.id.facebookButton).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.facebookButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", currentEvent.getId());
                    mixpanelProperties.put("event", currentEvent.getEvent());
                    activity.mixpanel.track("Facebook Link Clicked", mixpanelProperties);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Uri fbUrl = Uri.parse(currentEvent.getFacebookLink());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, fbUrl);
                startActivity(launchBrowser);
            }
        });

        Button buyTickets = (Button)rootView.findViewById(R.id.button_buy_tickets);
        buyTickets.setVisibility(View.VISIBLE);
        buyTickets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", currentEvent.getId());
                    mixpanelProperties.put("event", currentEvent.getEvent());
                    activity.mixpanel.track("Ticket Link Clicked", mixpanelProperties);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Uri ticketUrl = Uri.parse(currentEvent.getTicketLink());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, ticketUrl);
                startActivity(launchBrowser);
            }
        });
    }

    // Set Click listeners and Mixpanel event tracking for Twitter and Web links

    public void configureSocialMediaButtons() {
        rootView.findViewById(R.id.twitterButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", currentEvent.getId());
                    mixpanelProperties.put("event", currentEvent.getEvent());
                    activity.mixpanel.track("Twitter Link Clicked", mixpanelProperties);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Uri twitterUrl = Uri.parse(currentEvent.getTwitterLink());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, twitterUrl);
                startActivity(launchBrowser);
            }
        });
        rootView.findViewById(R.id.webButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", currentEvent.getId());
                    mixpanelProperties.put("event", currentEvent.getEvent());
                    activity.mixpanel.track("Web Link Clicked", mixpanelProperties);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Uri webUrl = Uri.parse(currentEvent.getWebLink());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, webUrl);
                startActivity(launchBrowser);
            }
        });
    }

    // Detail Models must be valid before executing this method

    public void onModelsReady() {
        if(rootView != null && activity != null) {
            setAdapters();
        }
        modelsReady = true;
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

            holder.setTime.setText(dateUtils.getDayFromDate(currentEvent.getStartDate(), lineupSet.getDay()) + " " + lineupSet.getTime());
            holder.artistText.setText(lineupSet.getArtist());
            holder.detailActionButton.setVisibility(View.GONE);

            ImageLoader.getInstance().displayImage(Constants.S3_ROOT_URL + lineupSet.getArtistImage(), holder.artistImage, options, animateFirstListener);

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
                    artistDetailFragment.currentArtist = currentArtist;
                    FragmentTransaction transaction = activity.fragmentManager.beginTransaction();
                    transaction.replace(R.id.currentFragmentContainer, artistDetailFragment, "artistDetailFragment");
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                }
            });

            if(holder.playCount != null) {
                holder.playCount.setText(set.getPopularity() + " plays");
            }
            holder.artistText.setText(set.getArtist());

            ImageLoader.getInstance().displayImage(Constants.S3_ROOT_URL + set.getArtistImage(), holder.artistImage, options, animateFirstListener);

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
