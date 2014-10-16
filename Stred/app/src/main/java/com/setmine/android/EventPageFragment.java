package com.setmine.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.stredm.android.object.Event;
import com.stredm.android.task.EventApiCallTask;
import com.stredm.android.util.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class EventPageFragment extends Fragment {

    public static final String ARG_OBJECT = "page";
    public ApiResponse res = null;
    public Context context;
    public View rootView;
    public Integer page;
    public ImageCache imageCache;
    public ModelsContentProvider modelsCP;
    public ViewPager eventViewPager;
    public List<View> currentTiles;
    public List<Event> currentEvents;
    public DateUtils dateUtils;
    public DisplayImageOptions options;
    public String lastEventDate;

    public EventPageFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle args = getArguments();
        page = args.getInt(ARG_OBJECT);
        if(page == 1) {
            currentEvents = ((SetMineMainActivity)getActivity()).modelsCP.upcomingEvents;
        }
        else if(page == 2) {
            currentEvents = ((SetMineMainActivity)getActivity()).modelsCP.recentEvents;
        }
        else if(page == 3) {
            currentEvents = ((SetMineMainActivity)getActivity()).modelsCP.searchEvents;
        }
        else
            currentTiles = null;
        Log.v("EPF Attached "+page.toString(), getActivity().toString());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SetMineMainActivity rootActivity = (SetMineMainActivity)getActivity();
        this.context = rootActivity.getApplicationContext();
        this.modelsCP = rootActivity.modelsCP;
        this.eventViewPager = rootActivity.eventViewPager;
        this.dateUtils = new DateUtils();

        options =  new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        Log.v("EPF Created "+page.toString(), getActivity().toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.v("page is ", page.toString());
        ListView listView = null;
        if(page == 1) {
            Log.v("page 1", " inflating");
            rootView = inflater.inflate(R.layout.events_scroll_view, container, false);
            listView = (ListView) rootView.findViewById(R.id.eventsList);
            listView.setAdapter(new EventAdapter(inflater, currentEvents, "upcoming"));
        }
        else if(page == 2) {
            Log.v("page 2", " inflating");
            rootView = inflater.inflate(R.layout.events_scroll_view_recent, container, false);
            listView = (ListView) rootView.findViewById(R.id.eventsListRecent);
            listView.setAdapter(new EventAdapter(inflater, currentEvents, "recent"));
        }
        else if(page == 3) {
            Log.v("page 3", " inflating");
            rootView = inflater.inflate(R.layout.events_finder, container, false);
            listView = (ListView) rootView.findViewById(R.id.searchResults);
            listView.setAdapter(new EventAdapter(inflater, currentEvents, "search"));
            rootView.findViewById(R.id.locationText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(!hasFocus) {
                        eventSearch(v);
                    }
                }
            });
            rootView.findViewById(R.id.dateText).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(!hasFocus) {
                        eventSearch(v);
                    }
                }
            });
        }
        if(listView != null) {
            listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
        }
        Log.v("EPF View Created ", page.toString());
        Log.v("rootview is ", rootView.toString());
        return rootView;
    }

    public void eventSearch(View v) {
        String location = ((TextView)((ViewGroup)v.getParent().getParent()).findViewById(R.id.locationText)).getText().toString();
        Log.v("location", location);
        String latitude = "33";
        String longitude = "-84";
        String date = ((TextView)((ViewGroup)v.getParent().getParent()).findViewById(R.id.dateText)).getText().toString();
//        EventApiCallTask eventSearchTask = new EventApiCallTask(context, this);
        String route = "upcoming/?date=" + Uri.encode(date) + "&latitude=" + latitude + "&longitude=" + longitude;
//        eventSearchTask.execute(route);
    }

    private static class ViewHolder {
        TextView event;
        TextView date;
        TextView city;
        ImageView image;
    }

    class EventAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
        private List<Event> events;
        private String type;

        EventAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        EventAdapter(LayoutInflater Inflater, List<Event> Events, String Type) {
            inflater = Inflater;
            events = Events;
            type = Type;
        }

        @Override
        public int getCount() {
            return events.size();
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
            final ViewHolder holder;
            Event event = events.get(position);
            if (convertView == null) {
                if(type.equals("upcoming")) {
                    view = inflater.inflate(R.layout.event_tile, parent, false);
                } else if (type.equals("recent")){
                    view = inflater.inflate(R.layout.event_tile_recent, parent, false);
                } else {
                    view = inflater.inflate(R.layout.event_search_tile, parent, false);
                }
                holder = new ViewHolder();
                holder.city = (TextView) view.findViewById(R.id.city);
                holder.event = (TextView) view.findViewById(R.id.event);
                holder.date = (TextView) view.findViewById(R.id.date);
                holder.image = (ImageView) view.findViewById(R.id.image);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            holder.city.setText(dateUtils.formatLocationFromAddress(event.address));
            holder.event.setText(event.event);
            holder.date.setText(dateUtils.formatDateText(event.startDate, event.endDate));

            ImageLoader.getInstance().displayImage(SetMineMainActivity.PUBLIC_ROOT_URL + "images/" + event.mainImageUrl, holder.image, options, animateFirstListener);

            final String eName = event.event;
            final String eDate = dateUtils.formatDateText(event.startDate, event.endDate);
            final String eCity = dateUtils.formatLocationFromAddress(event.address);
            final String eImage = event.mainImageUrl;
            final String eId = event.id;
            final String eDateUnformatted = event.startDate;
            View clickView = null;
            if(view.findViewById(R.id.button) != null) {
                clickView = view.findViewById(R.id.button);
            } else {
                clickView = view;
            }
            clickView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EventDetailFragment eventDetailFragment = new EventDetailFragment();
                    eventDetailFragment.EVENT_ID = eId;
                    eventDetailFragment.EVENT_NAME = eName;
                    eventDetailFragment.EVENT_DATE = eDate;
                    eventDetailFragment.EVENT_DATE_UNFORMATTED = eDateUnformatted;
                    eventDetailFragment.EVENT_CITY = eCity;
                    eventDetailFragment.EVENT_IMAGE = eImage;
                    eventDetailFragment.EVENT_TYPE = (type.equals("search")?"upcoming":type);
                    eventDetailFragment.LAST_EVENT_DATE = eDateUnformatted;
                    SetMineMainActivity activity = (SetMineMainActivity) getActivity();
                    FragmentTransaction transaction = activity.fragmentManager.beginTransaction();
                    transaction.replace(R.id.eventPagerContainer, eventDetailFragment, "eventDetailFragment");
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                }
            });

            return view;
        }
    }

    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

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
