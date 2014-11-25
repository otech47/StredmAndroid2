package com.setmine.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.object.Artist;
import com.setmine.android.object.Event;
import com.setmine.android.util.DateUtils;

import java.util.List;

/**
 * Created by oscarlafarga on 11/20/14.
 */
public class DetailUpcomingEventsFragment extends Fragment {

    public List<Event> detailEvents;
    public View rootView;
    public SetMineMainActivity activity;
    public DateUtils dateUtils;
    public Artist selectedArtist;

    public DisplayImageOptions options;

    public DetailUpcomingEventsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (SetMineMainActivity)getActivity();
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.festival_icon)
                .showImageForEmptyUri(R.drawable.festival_icon)
                .showImageOnFail(R.drawable.festival_icon)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        dateUtils = new DateUtils();
        detailEvents = activity.modelsCP.getDetailEvents(selectedArtist.getArtist());
        Log.d("Detail Events Fragment Created", this.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.detail_list, container, false);
        View detailListContainer = rootView.findViewById(R.id.detailListContainer);
        if(detailEvents.size() == 0) {
            ((TextView)rootView.findViewById(R.id.noResults)).setText("No Events Found");
            detailListContainer.setBackgroundResource(R.drawable.top_bottom_border_purple);
            rootView.findViewById(R.id.noResults).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.message).setVisibility(View.VISIBLE);
        }
        else {
            rootView.findViewById(R.id.noResults).setVisibility(View.GONE);
            rootView.findViewById(R.id.message).setVisibility(View.GONE);
        }
        for(final Event uEvent : detailEvents) {
            View eventTile = inflater.inflate(R.layout.event_tile_upcoming_small, container, false);
            ((TextView)eventTile.findViewById(R.id.eventText)).setText(uEvent.getEvent());
            ((TextView)eventTile.findViewById(R.id.dates))
                    .setText(dateUtils.formatDateText(uEvent.getStartDate(), uEvent.getEndDate()));
            ImageView imageView = (ImageView)eventTile.findViewById(R.id.eventImage);
            ImageLoader.getInstance()
                    .displayImage(SetMineMainActivity.S3_ROOT_URL + uEvent.getIconImageUrl(),
                            imageView, options);
            eventTile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setPressed(true);
                    Event currentEvent = uEvent;
                    EventDetailFragment eventDetailFragment = new EventDetailFragment();
                    eventDetailFragment.EVENT_ID = currentEvent.id;
                    eventDetailFragment.EVENT_NAME = currentEvent.event;
                    eventDetailFragment.EVENT_DATE = dateUtils.formatDateText(currentEvent.startDate, currentEvent.endDate);
                    eventDetailFragment.EVENT_DATE_UNFORMATTED = currentEvent.startDate;
                    eventDetailFragment.EVENT_ADDRESS = currentEvent.address;
                    eventDetailFragment.EVENT_IMAGE = currentEvent.mainImageUrl;
                    eventDetailFragment.EVENT_TYPE = "upcoming";
                    eventDetailFragment.EVENT_PAID = currentEvent.getPaid();
                    eventDetailFragment.EVENT_TICKET = currentEvent.getTicketLink();
                    SetMineMainActivity activity = (SetMineMainActivity) getActivity();
                    FragmentTransaction transaction = activity.fragmentManager.beginTransaction();
                    transaction.replace(R.id.eventPagerContainer, eventDetailFragment, "eventDetailFragment");
                    transaction.addToBackStack(null);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.commit();
                }
            });
            ((ViewGroup)detailListContainer).addView(eventTile);
        }

        Log.d("Detail Events Fragment View Created", this.toString());
        return rootView;
    }
}
