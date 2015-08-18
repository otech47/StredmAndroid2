package com.setmine.android.player;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.api.SetMineApiGetRequestAsyncTask;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.set.Set;
import com.setmine.android.track.Track;
import com.setmine.android.track.TrackHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jfonte on 10/16/2014.
 */
public class TracklistFragment extends Fragment implements ApiCaller {

    private final String TAG = "TracklistFragment";

    public View rootView;
    public Set set;
    public Track track;
    public List<Track> tracklist;
    public TrackAdapter trackAdapter;
    public Bundle savedInstanceState;

    public PlayerService playerService;
    public PlayerManager playerManager;


    @Override
    public void onApiResponseReceived(JSONObject jsonObject, String identifier) {
        try {
            JSONObject payload = jsonObject.getJSONObject("payload");
            JSONArray tracklistArray = payload.getJSONArray("tracks");
            tracklist = new ArrayList<Track>();
            for(int i = 0; i < tracklistArray.length(); i++) {
                tracklist.add(new Track(tracklistArray.getJSONObject(i)));
            }
            populateTracklist();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle args = getArguments();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        playerService = ((SetMineMainActivity) getActivity()).playerService;
        playerManager = playerService.playerManager;
        ((PlayerContainerFragment)getParentFragment()).tracklistFragment = this;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.tracklist, container, false);
        this.savedInstanceState = savedInstanceState;
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
        if(set != null) {
            new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR,
                            "tracklist/" + set.getId(), "tracklist");
        }
    }

    public void populateTracklist() {
        Log.d(TAG, "populateTracklist");
        playerManager.setTracklist(tracklist);
        trackAdapter = new TrackAdapter(tracklist);
        ((ListView) rootView.findViewById(R.id.tracklist)).setAdapter(trackAdapter);
        ((ListView) rootView.findViewById(R.id.tracklist)).setOnItemClickListener(new ListView
                .OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                ((PlayerContainerFragment)getParentFragment()).playerFragment.skipToTrack(position);
                ((PlayerContainerFragment)getParentFragment()).mViewPager.setCurrentItem(0);
            }
        });

    }


    class TrackAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<Track> tracks;

        TrackAdapter(List<Track> Tracks) {
            if(getActivity() != null) {
                inflater = LayoutInflater.from(getActivity());
            }
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
            final TrackHolder holder;
            if (convertView == null) {
                view = inflater.inflate(R.layout.list_track_item, parent, false);
                holder = new TrackHolder();
                holder.songName = (TextView) view.findViewById(R.id.list_track_song);
                holder.artistName = (TextView) view.findViewById(R.id.list_track_artist);
                holder.startTime = (TextView) view.findViewById(R.id.list_track_item_timestamp);
                view.setTag(holder);
            } else {
                holder = (TrackHolder) view.getTag();
            }

            Track t = tracks.get(position);
            String songName = t.getSongName();
            String artistName = t.getArtistName();
            String startTime = t.getStartTime();

            holder.songName.setText(songName);
            holder.artistName.setText(artistName);
            holder.startTime.setText(startTime);

            return view;
        }
    }

}
