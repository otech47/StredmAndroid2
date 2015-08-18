package com.setmine.android.event;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.api.SetMineApiGetRequestAsyncTask;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.set.LineupSet;
import com.setmine.android.set.Set;
import com.setmine.android.set.SetViewHolder;
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

    // Statics
    private static final String TAG = "EventDetailFragment";

    // Views
    public View rootView;

    // Models
    public Event currentEvent;
    public String EVENT_TYPE;
    public Lineup detailLineups;
    public List<LineupSet> currentLineupSet;
    public List<Set> detailSets;

    public Context context;
    public SetMineMainActivity activity;

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
    public void onApiResponseReceived(final JSONObject jsonObject, String identifier) {
        final JSONObject finalJsonObject = jsonObject;
        final String finalIdentifier = identifier;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(finalIdentifier.equals("recent")) {
                        Log.d(TAG, finalJsonObject.toString());
                        JSONObject payload = finalJsonObject.getJSONObject("payload");
                        currentEvent = new Event(payload.getJSONObject("festival"));
                    } else if(finalIdentifier.equals("upcoming")) {
                        JSONObject payload = finalJsonObject.getJSONObject("payload");
                        currentEvent = new Event(payload.getJSONObject("upcoming"));
                    }
                    handler.post(updateUI);
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.event_detail, container, false);

        // If savedInstance is null, the fragment is being generated directly from the activity

        // Else, the activity may or may not be attached yet
        // Use the Bundle to re-generate event data

        if(savedInstanceState == null) {
            // Generate event data
            Bundle arguments = getArguments();
            EVENT_TYPE = arguments.getString("eventType");
            String eventID = arguments.getString("eventID");

            Log.d(TAG, "festival/id/" + eventID);

            if(EVENT_TYPE.equals("recent")) {
                new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                                "festival/id/" + eventID, "recent");
            } else {
                new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                                "upcoming/id/" + eventID, "upcoming");
            }
        } else {
            EVENT_TYPE = savedInstanceState.getString("eventType");
            String eventModel = savedInstanceState.getString("currentEvent");

            try {
                JSONObject jsonEventModel = new JSONObject(eventModel);
                currentEvent = new Event(jsonEventModel);
                onModelsReady();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Customize detail page based on recent or upcoming

        if(EVENT_TYPE.equals("recent")) {
            rootView.findViewById(R.id.eventText).setBackgroundResource(R.color.setmine_blue);
            ((TextView)rootView.findViewById(R.id.lineupText)).setText("Sets");

            rootView.findViewById(R.id.directionsButton).setVisibility(View.GONE);

            // Set Facebook Click Listeners for past events

            rootView.findViewById(R.id.facebookButton).setVisibility(View.VISIBLE);

        } else {
            rootView.findViewById(R.id.eventText).setBackgroundResource(R.color.setmine_purple);

        }



        return rootView;
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
    }

    public void setAdapters() {
        activity = (SetMineMainActivity) getActivity();
        if(EVENT_TYPE.equals("recent")) {
            detailSets = currentEvent.getEventSets();
            ((ListView) rootView.findViewById(R.id.lineupContainer)).setAdapter(new SetAdapter(detailSets));
            ((ListView) rootView.findViewById(R.id.lineupContainer)).setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    v.setPressed(true);
                    activity.playerService.playerManager.setPlaylist(detailSets);
                    Set s = detailSets.get(position);
                    activity.playerService.playerManager.selectSetById(s.getId());
                    activity.startPlayerFragment();
                    activity.playSelectedSet();
                }
            });
        } else {
            currentLineupSet = currentEvent.getEventLineup().getLineup();
            ((ListView) rootView.findViewById(R.id.lineupContainer)).setAdapter(new LineupSetAdapter(currentLineupSet));
            ((ListView) rootView.findViewById(R.id.lineupContainer)).setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    v.setPressed(true);
                    String artistName = currentLineupSet.get(position).getArtist();
                    activity.openArtistDetailPage(artistName);
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
        rootView.findViewById(R.id.facebookButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", currentEvent.getId());
                    mixpanelProperties.put("event", currentEvent.getEvent());
                    ((SetMineMainActivity)getActivity()).mixpanel.track("Facebook Link Clicked", mixpanelProperties);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Uri fbUrl = Uri.parse(currentEvent.getFacebookLink());
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, fbUrl);
                startActivity(launchBrowser);
            }
        });
        rootView.findViewById(R.id.twitterButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", currentEvent.getId());
                    mixpanelProperties.put("event", currentEvent.getEvent());
                    ((SetMineMainActivity)getActivity()).mixpanel.track("Twitter Link Clicked", mixpanelProperties);
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
                    ((SetMineMainActivity)getActivity()).mixpanel.track("Web Link Clicked", mixpanelProperties);
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
        setAdapters();

        // Mixpanel tracking
        try {
            JSONObject mixpanelProperties = new JSONObject();
            mixpanelProperties.put("id", currentEvent.getId());
            mixpanelProperties.put("event", currentEvent.getEvent());
            mixpanelProperties.put("type", EVENT_TYPE);
            activity.mixpanel.track("Event Click Through", mixpanelProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DisplayImageOptions options =  new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        DateUtils dateUtils = new DateUtils();

        // Set text and for event details

        ImageLoader.getInstance().displayImage(currentEvent.getMainImageUrl(), (ImageView)rootView.findViewById(R.id.eventImage), options);
        ((TextView)rootView.findViewById(R.id.dateText)).setText(currentEvent.getDateFormatted());
        ((TextView)rootView.findViewById(R.id.locationText)).setText(currentEvent.getVenue() + ", "
                + dateUtils.getCityStateFromAddress(currentEvent.getAddress()));
        ((TextView)rootView.findViewById(R.id.eventText)).setText(currentEvent.getEvent());
        if(currentEvent.getPaid() == 1) {
            configurePaidButtons();
        }
        configureSocialMediaButtons();

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
                holder.detailActionButton = view.findViewById(R.id.playsIcon);
                view.setTag(holder);
            } else {
                holder = (LineupSetViewHolder) view.getTag();
            }

            DateUtils dateUtils = new DateUtils();

            holder.setTime.setText(dateUtils.getDayFromDate(currentEvent.getStartDate(), lineupSet.getDay()) + " " + lineupSet.getTime());
            holder.artistText.setText(lineupSet.getArtist());
            holder.detailActionButton.setVisibility(View.GONE);

            DisplayImageOptions options =  new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.logo_small)
                    .showImageForEmptyUri(R.drawable.logo_small)
                    .showImageOnFail(R.drawable.logo_small)
                    .cacheInMemory(false)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .build();

            ImageLoader.getInstance().displayImage(lineupSet.getArtistImage(), holder.artistImage, options, animateFirstListener);

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
                holder.detailActionButton = view.findViewById(R.id.playsIcon);
                view.setTag(holder);
                view.setId(Integer.valueOf(set.getId()).intValue());
            } else {
                holder = (SetViewHolder) view.getTag();
            }
            holder.detailActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String currentArtist = set.getArtist();
                    activity.openArtistDetailPage(currentArtist);
                }
            });

            if(holder.playCount != null) {
                holder.playCount.setText(set.getPopularity() + " plays");
            }
            holder.artistText.setText(set.getArtist());

            DisplayImageOptions options =  new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.logo_small)
                    .showImageForEmptyUri(R.drawable.logo_small)
                    .showImageOnFail(R.drawable.logo_small)
                    .cacheInMemory(false)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .build();

            ImageLoader.getInstance().displayImage(set.getArtistImage(), holder.artistImage, options, animateFirstListener);

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
