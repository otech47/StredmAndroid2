package com.setmine.android.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.setmine.android.ApiCaller;
import com.setmine.android.ApiResponse;
import com.setmine.android.ModelsContentProvider;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.object.Event;
import com.setmine.android.task.ApiCallAsyncTask;
import com.setmine.android.task.InitialApiCallAsyncTask;
import com.setmine.android.util.DateUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class EventPageFragment extends Fragment implements ApiCaller {

    public static final String ARG_OBJECT = "page";
    public ApiResponse res = null;
    public Context context;
    public View rootView;
    public Integer page;
    public ModelsContentProvider modelsCP;
    public ViewPager eventViewPager;
    public List<View> currentTiles;
    public List<Event> currentEvents;
    public DateUtils dateUtils;
    public DisplayImageOptions options;
    public Geocoder geocoder;
    public SetMineMainActivity activity;
    public Calendar selectedDate;
    public Location selectedLocation;
    public EventAdapter dynamicAdapter;
    public List<Address> addressResultList;
    public Address addressResult;
    public String eventType;

    public EventPageFragment() {}

    @Override
    public void onResponseReceived(JSONObject jsonObject, String identifier) {
        modelsCP.setModel(jsonObject, identifier);
        currentEvents = modelsCP.searchEvents;
        dynamicAdapter.newData();
        dynamicAdapter.notifyDataSetChanged();
    }

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
        this.activity = (SetMineMainActivity)getActivity();
        this.context = activity.getApplicationContext();
        this.modelsCP = activity.modelsCP;
        this.eventViewPager = activity.eventViewPager;
        this.dateUtils = new DateUtils();
        this.geocoder = new Geocoder(context, Locale.getDefault());
        this.selectedDate = Calendar.getInstance();
        this.selectedLocation = new Location(activity.currentLocation);
        this.addressResultList = null;
        this.addressResult = null;

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
        eventType = "";
        if(page == 1) {
            Log.v("page 1", " inflating");
            eventType = "upcoming";
            rootView = inflater.inflate(R.layout.events_scroll_view, container, false);
            listView = (ListView) rootView.findViewById(R.id.eventsList);
            listView.setAdapter(new EventAdapter(inflater, currentEvents, eventType));
        }
        else if(page == 2) {
            Log.v("page 2", " inflating");
            eventType = "recent";
            rootView = inflater.inflate(R.layout.events_scroll_view_recent, container, false);
            listView = (ListView) rootView.findViewById(R.id.eventsListRecent);
            listView.setAdapter(new EventAdapter(inflater, currentEvents, eventType));
        }
        else if(page == 3) {
            Log.v("page 3", " inflating");
            eventType = "search";
            rootView = inflater.inflate(R.layout.events_finder, container, false);
            listView = (ListView) rootView.findViewById(R.id.searchResults);
            final EditText locationText = (EditText)rootView.findViewById(R.id.locationText);
            final TextView dateText = (TextView)rootView.findViewById(R.id.dateText);
            final DatePicker datePicker = (DatePicker)rootView.findViewById(R.id.datePicker);

            dynamicAdapter = new EventAdapter(inflater, currentEvents, eventType);

            listView.setAdapter(dynamicAdapter);

            locationText.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
            locationText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    try {
                        List<Address> results = geocoder.getFromLocationName(v.getText().toString(), 1);
                        if (results != null) {
                            selectedLocation.setLatitude(results.get(0).getLatitude());
                            selectedLocation.setLongitude(results.get(0).getLongitude());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    eventSearch(v);
                    return true;
                }
            });
            if(addressResult == null) {
                try {
                    addressResultList = geocoder.getFromLocation(selectedLocation.getLatitude(),
                            selectedLocation.getLongitude(), 1);
                    if(addressResultList.size() > 0) {
                        addressResult = addressResultList.get(0);
                        locationText.setText(addressResult.getLocality() + ", " + addressResult.getAdminArea());
                    }
                    else {
                        locationText.setText("No Address");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            locationText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        try {
                            List<Address> results = geocoder.getFromLocationName(((TextView) v).getText().toString(), 1);
                            if (results != null) {
                                selectedLocation.setLatitude(results.get(0).getLatitude());
                                selectedLocation.setLongitude(results.get(0).getLongitude());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.v("Location Lost ", "Focus");
                        eventSearch(v);
                    }
                }
            });

            SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMMM' 'd', 'yyyy");
            dateText.setText(inputDateFormat.format(selectedDate.getTime()));

            dateText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getRootView().findViewById(R.id.datePickerContainer).setVisibility(View.VISIBLE);
                }
            });


            Calendar today = Calendar.getInstance();
            datePicker.init(
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH),
                    new DatePicker.OnDateChangedListener() {
                        @Override
                        public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            selectedDate.set(year, monthOfYear, dayOfMonth);
                            SimpleDateFormat inputDateFormat = new SimpleDateFormat("MMMM' 'd', 'yyyy");
                            ((TextView)view.getRootView().findViewById(R.id.dateText)).setText(inputDateFormat.format(selectedDate.getTime()));
                        }
                    }
            );
            ((ViewGroup)datePicker.getParent()).findViewById(R.id.searchButton)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.getRootView().findViewById(R.id.datePickerContainer)
                                    .setVisibility(View.GONE);
                            eventSearch(v);
                        }
                    });
        }
        if(listView != null) {
            listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
            listView.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView <?> parent, View v,
                int position, long id) {
                    Event currentEvent = currentEvents.get(position);

                    EventDetailFragment eventDetailFragment = new EventDetailFragment();
                    eventDetailFragment.EVENT_ID = currentEvent.id;
                    eventDetailFragment.EVENT_NAME = currentEvent.event;
                    eventDetailFragment.EVENT_DATE = dateUtils.formatDateText(currentEvent.startDate, currentEvent.endDate);
                    eventDetailFragment.EVENT_DATE_UNFORMATTED = currentEvent.startDate;
                    eventDetailFragment.EVENT_ADDRESS = currentEvent.address;
                    eventDetailFragment.EVENT_IMAGE = currentEvent.mainImageUrl;
                    eventDetailFragment.EVENT_TYPE = (eventType.equals("search")?"upcoming":eventType);
                    eventDetailFragment.LAST_EVENT_DATE = currentEvent.startDate;
                    SetMineMainActivity activity = (SetMineMainActivity) getActivity();
                    FragmentTransaction transaction = activity.fragmentManager.beginTransaction();
                    transaction.replace(R.id.eventPagerContainer, eventDetailFragment, "eventDetailFragment");
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                }
            });
        }
        Log.v("EPF View Created ", page.toString());
        Log.v("rootview is ", rootView.toString());
        return rootView;
    }

    public void eventSearch(View v) {
        View parentView = v.getRootView();
        dynamicAdapter.clear();
        dynamicAdapter.notifyDataSetChanged();
        String latitude = ((Double)selectedLocation.getLatitude()).toString();
        String longitude = ((Double)selectedLocation.getLongitude()).toString();
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy'-'MM'-'d");
        String date = apiDateFormat.format(selectedDate.getTime());
        String route = "upcoming/?date=" + Uri.encode(date) + "&latitude=" + latitude + "&longitude=" + longitude;
        new ApiCallAsyncTask(activity, context, this, activity.API_ROOT_URL)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR,
                route,
                "searchEvents");
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

        public void clear() {
            events.clear();
        }

        public void newData() {
            events = currentEvents;
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

            holder.city.setText(dateUtils.getCityStateFromAddress(event.address));
            holder.event.setText(event.event);
            holder.date.setText(dateUtils.formatDateText(event.startDate, event.endDate));

            ImageLoader.getInstance().displayImage(SetMineMainActivity.PUBLIC_ROOT_URL + "images/" + event.mainImageUrl, holder.image, options, animateFirstListener);


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
