package com.setmine.android.event;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.google.android.gms.location.LocationClient;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.setmine.android.MainPagerContainerFragment;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.ModelsContentProvider;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.api.InitialApiCallAsyncTask;
import com.setmine.android.api.SetMineApiGetRequestAsyncTask;
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

    private static final String TAG = "EventPageFragment";

    public static final String ARG_OBJECT = "page";
    public Integer page;

    public SetMineMainActivity activity;
    public Context context;
    public ModelsContentProvider modelsCP;

    public View rootView;
    public ViewPager viewPager;
    public ListView listView;
    public List<View> currentTiles;

    public DateUtils dateUtils;
    public DisplayImageOptions options;
    public Geocoder geocoder;
    public Calendar selectedDate;
    public Location selectedLocation;
    public List<Address> addressResultList;
    public Address addressResult;
    public LocationClient locationClient;
    public Location currentLocation;

    public List<Event> currentEvents;
    public String eventType;
    public EventAdapter eventAdapter;


    public EventPageFragment() {}

    final Handler handler = new Handler();

    final Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            setEventAdapter();
        }
    };

    @Override
    public void onApiResponseReceived(JSONObject jsonObject, String identifier) {
        final JSONObject finalJsonObject = jsonObject;
        final String finalIdentifier = identifier;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onApiResponse: "+finalIdentifier);

                currentEvents= ModelsContentProvider.createModel(finalJsonObject, finalIdentifier);

                handler.post(updateUI);
            }
        }).start();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.activity = (SetMineMainActivity)getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        page = args.getInt(ARG_OBJECT);
        Log.v(TAG, "onCreate: " + page.toString());

        if(savedInstanceState == null) {
            this.activity = (SetMineMainActivity)getActivity();
            if(modelsCP == null) {
                modelsCP = activity.modelsCP;
            }
            if(page == 2) {
                if (this.activity.servicesConnected()) {
                    Log.d(TAG, "servicesConnected");
                    currentLocation = ((SetMineMainActivity) getActivity()).currentLocation;

                }
                else {
                    Log.d(TAG, "services NOT Connected");
                    currentLocation= null;
                }
                new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                                "featured", "upcoming/?latitude="+currentLocation.getLatitude()+"&longitude="+currentLocation.getLongitude());
            }
            else if(page == 3) {
                new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                            "featured", "recentEvents");
            }
            else if(page == 4) {
                //??? are we keeping search page?
             //   currentEvents = modelsCP.searchEvents;
            }
        } else {
            if(modelsCP == null) {
                modelsCP = new ModelsContentProvider();
            }
            String model = savedInstanceState.getString("currentEvents");
            try {
                JSONObject jsonModel = new JSONObject(model);
                if(page == 2) {
                   currentEvents= modelsCP.createModel(jsonModel, "upcomingEvents");
                }
                else if(page == 3) {
                   currentEvents= modelsCP.createModel(jsonModel, "recentEvents");
                }
                else if(page == 4) {
                    currentEvents= modelsCP.createModel(jsonModel, "searchEvents");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView: " + page.toString());

        this.dateUtils = new DateUtils();
        this.geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
        this.selectedDate = Calendar.getInstance();
        this.addressResultList = null;
        this.addressResult = null;

        this.viewPager = ((MainPagerContainerFragment)this.getParentFragment()).mViewPager;
        if(page == 2) {
            eventType = "upcoming";
            rootView = inflater.inflate(R.layout.events_scroll_view, container, false);
            listView = (ListView) rootView.findViewById(R.id.eventsList);
            rootView.findViewById(R.id.sets_nav_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPager.setCurrentItem(2);
                }
            });
        }
        else if(page == 3) {
            eventType = "recent";
            rootView = inflater.inflate(R.layout.events_scroll_view_recent, container, false);
            listView = (ListView) rootView.findViewById(R.id.eventsListRecent);
            rootView.findViewById(R.id.event_nav_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPager.setCurrentItem(1);
                }
            });
            rootView.findViewById(R.id.event_search_nav_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPager.setCurrentItem(3);
                }
            });
        }
        else if(page == 4) {
            eventType = "search";
            rootView = inflater.inflate(R.layout.events_finder, container, false);
            listView = (ListView) rootView.findViewById(R.id.searchResults);
            configureEventSearch();
        }
        setEventAdapter();

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        if(page == 2) {
            outState.putString("currentEvents", modelsCP.jsonMappings.get("soonestEventsAroundMe"));
        }
        else if(page == 3) {
            outState.putString("currentEvents", modelsCP.jsonMappings.get("recentEvents"));
        }
        else if(page == 4) {
            outState.putString("currentEvents", modelsCP.jsonMappings.get("searchEvents"));
        }
        else
            currentTiles = null;
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public void setEventAdapter() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        eventAdapter = new EventAdapter(inflater, currentEvents, eventType);
        listView.setAdapter(eventAdapter);
        eventAdapter.newData();
        eventAdapter.notifyDataSetChanged();
        listView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), false, true));
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Event currentEvent = currentEvents.get(position);
                activity.openEventDetailPage(currentEvent, eventType);
            }
        });
        rootView.findViewById(R.id.centered_loader_container).setVisibility(View.GONE);
    }

    public void eventSearch(View v) {
        eventAdapter.clear();
        eventAdapter.notifyDataSetChanged();
        String latitude = ((Double)selectedLocation.getLatitude()).toString();
        String longitude = ((Double)selectedLocation.getLongitude()).toString();
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy'-'MM'-'d");
        String date = apiDateFormat.format(selectedDate.getTime());
        String route = "upcoming/?date=" + Uri.encode(date) + "&latitude=" + latitude + "&longitude=" + longitude;
        new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                .executeOnExecutor(InitialApiCallAsyncTask.THREAD_POOL_EXECUTOR,
                        route,
                        "searchEvents");
    }

    public void configureEventSearch() {
        final EditText locationText = (EditText)rootView.findViewById(R.id.locationText);
        final TextView dateText = (TextView)rootView.findViewById(R.id.dateText);
        final DatePicker datePicker = (DatePicker)rootView.findViewById(R.id.datePicker);
        if(activity.currentLocation != null) {
            this.selectedLocation = new Location(activity.currentLocation);
        } else {
            this.selectedLocation = null;
        }


        rootView.findViewById(R.id.sets_nav_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });

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
            if(selectedLocation!=null) {
                try {
                    addressResultList = geocoder.getFromLocation(selectedLocation.getLatitude(),
                            selectedLocation.getLongitude(), 1);
                    if (addressResultList.size() > 0) {
                        addressResult = addressResultList.get(0);
                        locationText.setText(addressResult.getLocality() + ", " + addressResult.getAdminArea());
                    } else {
                        locationText.setText("No Address");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        rootView.findViewById(R.id.searchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.getRootView().findViewById(R.id.datePickerContainer)
                        .setVisibility(View.GONE);
                eventSearch(v);
            }
        });
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
                    view = inflater.inflate(R.layout.event_tile_upcoming, parent, false);
                } else if (type.equals("recent")){
                    view = inflater.inflate(R.layout.event_tile_recent, parent, false);
                } else {
                    view = inflater.inflate(R.layout.upcoming_event_tile_top, parent, false);
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

            options =  new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.logo_small)
                    .showImageForEmptyUri(R.drawable.logo_small)
                    .showImageOnFail(R.drawable.logo_small)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .build();

            ImageLoader.getInstance()
                    .displayImage(event.mainImageUrl,
                            holder.image, options, animateFirstListener);


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
