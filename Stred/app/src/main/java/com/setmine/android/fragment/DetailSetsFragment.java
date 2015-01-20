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
import com.setmine.android.object.Set;
import com.setmine.android.util.DateUtils;

import java.util.List;

/**
 * Created by oscarlafarga on 11/20/14.
 */
public class DetailSetsFragment extends Fragment {

    public List<Set> detailSets;
    public View rootView;
    public SetMineMainActivity activity;
    public DateUtils dateUtils;
    public Artist selectedArtist;

    public DisplayImageOptions options;


    public DetailSetsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (SetMineMainActivity)getActivity();
        dateUtils = new DateUtils();
        detailSets = activity.modelsCP.getDetailSets(selectedArtist.getArtist());
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
        Log.d("Detail Sets Fragment Created", this.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.detail_list, container, false);
        View detailListContainer = rootView.findViewById(R.id.detailListContainer);
        if(detailSets.size() == 0) {
            ((TextView)rootView.findViewById(R.id.noResults)).setText("No Sets Found");
            detailListContainer.setBackgroundResource(R.drawable.top_bottom_border_blue);
            rootView.findViewById(R.id.noResults).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.message).setVisibility(View.VISIBLE);
        }
        else {
            rootView.findViewById(R.id.noResults).setVisibility(View.GONE);
            rootView.findViewById(R.id.message).setVisibility(View.GONE);
        }
        for(final Set set : detailSets) {
            View setTile = inflater.inflate(R.layout.artist_tile_recent, container, false);
            ((TextView)setTile.findViewById(R.id.artistText)).setText(set.getEvent());
            ((TextView)setTile.findViewById(R.id.playCount)).setText(set.getPopularity()+" plays");

            ImageView imageView = (ImageView)setTile.findViewById(R.id.artistImage);
            ImageLoader.getInstance()
                    .displayImage(SetMineMainActivity.S3_ROOT_URL + set.getEventImage(),
                            imageView, options);
            if(set.isRadiomix() == 1) {
                setTile.findViewById(R.id.detailActionButton).setVisibility(View.GONE);
            } else {
                ((ImageView)setTile.findViewById(R.id.iconImage)).setImageResource(R.drawable.festival_icon);
                ((TextView)setTile.findViewById(R.id.iconText)).setText("Event Info");
                setTile.findViewById(R.id.detailActionButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setPressed(true);
                        Event currentEvent = null;
                        for (Event event : activity.modelsCP.getEvents()) {
                            if (event.getEvent().equals(set.getEvent())) {
                                currentEvent = event;
                            }
                        }
                        EventDetailFragment eventDetailFragment = new EventDetailFragment();
                        eventDetailFragment.EVENT_ID = currentEvent.id;
                        eventDetailFragment.EVENT_NAME = currentEvent.event;
                        eventDetailFragment.EVENT_DATE_FORMATTED = dateUtils.formatDateText(currentEvent.startDate, currentEvent.endDate);
                        eventDetailFragment.EVENT_START_DATE_UNFORMATTED = currentEvent.startDate;
                        eventDetailFragment.EVENT_ADDRESS = currentEvent.address;
                        eventDetailFragment.EVENT_IMAGE = currentEvent.mainImageUrl;
                        eventDetailFragment.EVENT_TYPE = "recent";
                        eventDetailFragment.EVENT_PAID = currentEvent.getPaid();
                        eventDetailFragment.EVENT_TICKET = currentEvent.getTicketLink();
                        SetMineMainActivity activity = (SetMineMainActivity) getActivity();
                        FragmentTransaction transaction = activity.fragmentManager.beginTransaction();
                        Log.v("which frag artist sets detail", "is this");
                        transaction.replace(R.id.eventPagerContainer, eventDetailFragment, "eventDetailFragment");
                        transaction.addToBackStack(null);
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.commit();
                    }
                });
            }
            setTile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.setsManager.setPlaylist(detailSets);
                    activity.playlistFragment.updatePlaylist();
                    activity.startPlayerFragment(set.getId());
                }
            });
            ((ViewGroup)detailListContainer).addView(setTile);
        }
        Log.d("Detail Sets Fragment View Created", this.toString());
        return rootView;
    }
}
