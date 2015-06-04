package com.setmine.android.player;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.set.Set;
import com.setmine.android.track.Track;
import com.setmine.android.util.TimeUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jfonte on 10/16/2014.
 */
public class TracklistFragment extends Fragment {

    private final String TAG = "TracklistFragment";


    public static final String SONG_ARG_OBJECT = "song";
    public static final String SHUFFLE_ARG_OBJECT = "shuffle";
    public View rootView;
    public ListView listview;
    public Integer setNum;
    public Set set;
    public Track track;
    public List<Track> tracklist;
    public Boolean shuffle;
    public Context context;
    public DisplayImageOptions options;
    public TrackAdapter trackAdapter;
    public Bundle savedInstanceState;
    public SetMineMainActivity activity;
    public PlayerContainerFragment playerContainerFragment;

    public PlayerService playerService;
    public PlayerManager playerManager;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle args = getArguments();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        if(savedInstanceState == null) {
            super.onCreate(savedInstanceState);
            activity = (SetMineMainActivity) getActivity();
            playerService = activity.playerService;
            playerManager = playerService.playerManager;
            this.context = activity.getApplicationContext();
            playerContainerFragment = ((PlayerContainerFragment)getParentFragment());
            playerContainerFragment.tracklistFragment = this;

            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.logo_small)
                    .showImageForEmptyUri(R.drawable.logo_small)
                    .showImageOnFail(R.drawable.logo_small)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .build();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.tracklist, container, false);
        this.savedInstanceState = savedInstanceState;
        listview = (ListView) rootView.findViewById(R.id.tracklist);
        updateTracklist();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void updateTracklist() {
        Log.d(TAG, "updateTracklist");

        set = playerManager.getSelectedSet();
        tracklist = (set != null)? set.getTracklist() : null;

        trackAdapter = new TrackAdapter(getLayoutInflater(savedInstanceState), tracklist);
        listview.setAdapter(trackAdapter);
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                playerContainerFragment.playerFragment.skipToTrack(position);
                playerContainerFragment.mViewPager.setCurrentItem(0);
            }
        });

    }



    private static class TrackViewHolder {
        TextView track;
        TextView timestamp;
    }

    class TrackAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
        private List<Track> tracks;
        private String type;

        TrackAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        TrackAdapter(LayoutInflater Inflater, List<Track> Tracks) {
            inflater = Inflater;
            tracks = Tracks;
        }

        @Override
        public int getCount() {
            return (tracks != null)? tracks.size() : 0;
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
            final TrackViewHolder holder;
            Track track1 = tracks.get(position);
            if (convertView == null) {
                view = inflater.inflate(R.layout.list_track_item, parent, false);
                holder = new TrackViewHolder();
                holder.track = (TextView) view.findViewById(R.id.list_track_item_text);
                holder.timestamp = (TextView) view.findViewById(R.id.list_track_item_timestamp);
                view.setTag(holder);
            } else {
                holder = (TrackViewHolder) view.getTag();
            }

            Track t = tracks.get(position);
            String title = t.getTrackName();
            String timestamp = t.getStartTime();
            TimeUtils utils = new TimeUtils();
            int timeMS = utils.timerToMilliSeconds(timestamp);
            timestamp = utils.milliSecondsToTimer(timeMS);

            holder.track.setText(title);
            holder.timestamp.setText(timestamp);

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