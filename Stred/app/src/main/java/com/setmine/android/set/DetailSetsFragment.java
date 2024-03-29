package com.setmine.android.set;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.setmine.android.artist.Artist;
import com.setmine.android.artist.ArtistDetailFragment;
import com.setmine.android.event.Event;

import java.util.List;

/**
 * Created by oscarlafarga on 11/20/14.
 */
public class DetailSetsFragment extends Fragment {

    public List<Set> detailSets;
    public View rootView;
    public SetMineMainActivity activity;
    public Artist selectedArtist;

    public DisplayImageOptions options;

    public DetailSetsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.detail_list, container, false);
        View detailListContainer = rootView.findViewById(R.id.detailListContainer);

        detailSets = selectedArtist.getSets();

        if(detailSets.size() == 0) {
            ((TextView)rootView.findViewById(R.id.noResults)).setText("No Sets Found");
            detailListContainer.setBackgroundResource(R.drawable.top_bottom_border_blue);
            rootView.findViewById(R.id.noResults).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.message).setVisibility(View.VISIBLE);
        }
        else {
            rootView.findViewById(R.id.noResults).setVisibility(View.GONE);
            rootView.findViewById(R.id.message).setVisibility(View.GONE);
            for(final Set set : detailSets) {
                View setTile = inflater.inflate(R.layout.artist_tile_recent, container, false);
                ((TextView)setTile.findViewById(R.id.artistText)).setText(set.getEvent());
                ((TextView)setTile.findViewById(R.id.playCount)).setText(set.getPopularity()+" plays");

                options = new DisplayImageOptions.Builder()
                        .showImageOnLoading(R.drawable.logo_small)
                        .showImageForEmptyUri(R.drawable.logo_small)
                        .showImageOnFail(R.drawable.logo_small)
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .considerExifParams(true)
                        .build();
                ImageView imageView = (ImageView)setTile.findViewById(R.id.artistImage);
                ImageLoader.getInstance()
                        .displayImage(set.getEventImage(),
                                imageView, options);
                if(set.isRadiomix() == 1) {
                    setTile.findViewById(R.id.playsIcon).setVisibility(View.GONE);
                } else {
                    ((ImageView)setTile.findViewById(R.id.iconImage)).setImageResource(R.drawable.festival_icon);
                    ((TextView)setTile.findViewById(R.id.iconText)).setText("Event Info");
                    setTile.findViewById(R.id.playsIcon).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.setPressed(true);
                            Log.d("DSF", Integer.toString(set.getEventID()));
                            ((SetMineMainActivity)getActivity()).openEventDetailPage(Integer.toString(set.getEventID()),
                                    "recent");

                        }
                    });
                }
                setTile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((SetMineMainActivity)getActivity()).playerService.playerManager.setPlaylist
                                (detailSets);
                        ((SetMineMainActivity)getActivity()).playerService.playerManager
                                .selectSetById(set.getId());
                        ((SetMineMainActivity)getActivity()).startPlayerFragment();
                        ((SetMineMainActivity)getActivity()).playSelectedSet();
                    }
                });
                ((ViewGroup)detailListContainer).addView(setTile);
            }
        }

        return rootView;
    }
}
