package com.setmine.android.fragment;

import android.content.Context;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.object.SearchResultSetViewHolder;
import com.setmine.android.object.Set;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by oscarlafarga on 11/2/14.
 */
public class PlaylistFragment extends Fragment {

    private static final String TAG = "PlaylistFragment";

    public View rootView;
    public Integer setNum;
    public Boolean shuffle;
    public SetMineMainActivity activity;
    public Context context;
    public DisplayImageOptions options;
    public List<Set> setlist;
    public SetAdapter setListAdapter;
    public Bundle savedInstanceState;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        this.activity = (SetMineMainActivity) getActivity();
        this.context = activity.getApplicationContext();

        Bundle arguments = getArguments();
        ArrayList<Set> playlistlistParcel = arguments.getParcelableArrayList("playlist");
        setlist = new ArrayList<Set>(playlistlistParcel);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.playlist, container, false);
        if(savedInstanceState != null) {
            ArrayList<Set> setlistParcel = savedInstanceState.getParcelableArrayList("setlist");
            setlist = new ArrayList<Set>(setlistParcel);
        }
        ListView listview = (ListView) rootView.findViewById(R.id.playlist);
        setListAdapter = new SetAdapter(setlist);
        listview.setAdapter(setListAdapter);
        setListAdapter.notifyDataSetChanged();
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                activity.playerService.playerManager.setPlaylist(setlist);
                activity.playerService.playerManager.selectSetById(setlist.get(position).getId());
                activity.startPlayerFragment();
                activity.playSelectedSet();
            }
        });
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
        ArrayList<Set> setlistBundle = new ArrayList<Set>(setlist);
        outState.putParcelableArrayList("setlist", setlistBundle);
    }

    public void updatePlaylist() {
        ListView listview = (ListView) rootView.findViewById(R.id.playlist);
        setlist = activity.playerService.playerManager.getPlaylist();
        setListAdapter.sets = setlist;
        setListAdapter.notifyDataSetChanged();
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                activity.playSetWithSetID(activity.playerFragment
                        .playerManager.getPlaylist().get(position).getId());
                activity.playerContainerFragment.mViewPager.setCurrentItem(1);
            }
        });
    }

    class SetAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ImageLoadingListener animateFirstListener = new EventDetailFragment.AnimateFirstDisplayListener();
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
            final SearchResultSetViewHolder holder;
            final Set set = sets.get(position);
            if (convertView == null) {
                view = inflater.inflate(R.layout.set_tile, parent, false);
                holder = new SearchResultSetViewHolder();
                holder.playCount = (TextView) view.findViewById(R.id.playCount);
                holder.setLength = (TextView) view.findViewById(R.id.setLength);
                holder.artistText = (TextView) view.findViewById(R.id.artistText);
                holder.eventText = (TextView) view.findViewById(R.id.eventText);
                holder.artistImage = (ImageView) view.findViewById(R.id.artistImage);
                view.setTag(holder);
                view.setId(Integer.valueOf(set.getId()).intValue());
            } else {
                holder = (SearchResultSetViewHolder) view.getTag();
            }


            holder.playCount.setText(set.getPopularity() + " plays");
            holder.setLength.setText(set.getSetLength());
            holder.artistText.setText(set.getArtist());
            holder.eventText.setText(set.getEvent());

            ImageLoader.getInstance().displayImage(set.getArtistImage(), holder.artistImage, options, animateFirstListener);

            return view;
        }
    }
}
