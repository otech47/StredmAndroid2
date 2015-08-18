package com.setmine.android.event;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.artist.Artist;

import java.util.List;

/**
 * Created by oscarlafarga on 11/20/14.
 */
public class DetailUpcomingEventsFragment extends Fragment {

    public List<Event> detailEvents;
    public View rootView;
    public Artist selectedArtist;

    public DisplayImageOptions options;

    public DetailUpcomingEventsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.detail_list, container, false);
        View detailListContainer = rootView.findViewById(R.id.detailListContainer);

        detailEvents = selectedArtist.getUpcomingEvents();

        if(detailEvents.size() == 0) {
            ((TextView)rootView.findViewById(R.id.noResults)).setText("No Events Found");
            detailListContainer.setBackgroundResource(R.drawable.top_bottom_border_purple);
            rootView.findViewById(R.id.noResults).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.message).setVisibility(View.VISIBLE);
        }
        else {
            rootView.findViewById(R.id.noResults).setVisibility(View.GONE);
            rootView.findViewById(R.id.message).setVisibility(View.GONE);
            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.festival_icon)
                    .showImageForEmptyUri(R.drawable.festival_icon)
                    .showImageOnFail(R.drawable.festival_icon)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .build();
            for(final Event uEvent : detailEvents) {
                View eventTile = inflater.inflate(R.layout.event_tile_upcoming_small, container, false);
                ((TextView)eventTile.findViewById(R.id.eventText)).setText(uEvent.getEvent());
                ((TextView)eventTile.findViewById(R.id.dates))
                        .setText(uEvent.getDateFormatted());

                ImageView imageView = (ImageView)eventTile.findViewById(R.id.eventImage);
                ImageLoader.getInstance()
                        .displayImage(uEvent.getIconImageUrl(),
                                imageView, options);
                eventTile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setPressed(true);
                        ((SetMineMainActivity) getActivity()).openEventDetailPage(uEvent.getId(), "upcoming");
                    }
                });
                ((ViewGroup)detailListContainer).addView(eventTile);
            }
        }


        return rootView;
    }

    public void setDetailEvents(List<Event> events) {
        detailEvents = events;
    }

    public void populateDetailEvents() {

    }
}
