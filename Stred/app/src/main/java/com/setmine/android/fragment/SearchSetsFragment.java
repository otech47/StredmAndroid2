package com.setmine.android.fragment;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.setmine.android.ModelsContentProvider;
import com.setmine.android.OnTaskCompleted;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.object.Artist;
import com.setmine.android.Constants;
import com.setmine.android.object.Event;
import com.setmine.android.object.Genre;
import com.setmine.android.object.Mix;
import com.setmine.android.object.SearchResultSetViewHolder;
import com.setmine.android.object.Set;
import com.setmine.android.task.GetSetsTask;
import com.setmine.android.util.DateUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchSetsFragment extends Fragment implements OnTaskCompleted<Set> {

    private static final String TAG = "SearchSetsFragment";

    private SetMineMainActivity activity;
    public DisplayImageOptions options;
    public String searchQuery;
    public GetSetsTask getSetsTask;

    public ModelsContentProvider modelsCP;
    public boolean modelsReady;

    public View rootView;
    public SearchView searchView;
    public ListView browseItemList;
    public ListView searchResultsList;
    public TextView noResults;
    public ProgressBar setsLoading;
    public ViewGroup browseNavContainer;
    public View browseItemListContainer;
    public View selectedBrowseView;

    public List<Artist> artists;
    public List<Event> festivals;
    public List<Mix> mixes;
    public List<Genre> genres;
    public List<Set> popularSets;
    public List<Set> recentSets;

    public SearchResultSetAdapter searchResultSetAdapter;
    public ArtistAdapter artistAdapter;
    public EventAdapter eventAdapter;
    public MixAdapter mixAdapter;
    public GenreAdapter genreAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        this.activity = (SetMineMainActivity)getActivity();

        // Recover data from saved instance or retrieve from activity

        if(savedInstanceState == null) {
            this.activity = (SetMineMainActivity)getActivity();
            modelsCP = activity.modelsCP;
        } else {
            if(modelsCP == null) {
                modelsCP = new ModelsContentProvider();
            }
            String artistsModel = savedInstanceState.getString("artists");
            String festivalsModel = savedInstanceState.getString("festivals");
            String mixesModel = savedInstanceState.getString("mixes");
            String genresModel = savedInstanceState.getString("genres");
            String popularSetsModel = savedInstanceState.getString("popularSets");
            String recentSetsModel = savedInstanceState.getString("recentSets");
            String allArtistsModel = savedInstanceState.getString("allArtists");
            try {
                JSONObject jsonArtistsModel = new JSONObject(artistsModel);
                JSONObject jsonFestivalsModel = new JSONObject(festivalsModel);
                JSONObject jsonMixesModel = new JSONObject(mixesModel);
                JSONObject jsonGenresModel = new JSONObject(genresModel);
                JSONObject jsonPopularSetsModel = new JSONObject(popularSetsModel);
                JSONObject jsonRecentSetsModel = new JSONObject(recentSetsModel);
                JSONObject jsonAllArtistsModel = new JSONObject(allArtistsModel);
                modelsCP.setModel(jsonArtistsModel, "artists");
                modelsCP.setModel(jsonFestivalsModel, "festivals");
                modelsCP.setModel(jsonMixesModel, "mixes");
                modelsCP.setModel(jsonGenresModel, "genres");
                modelsCP.setModel(jsonPopularSetsModel, "popularSets");
                modelsCP.setModel(jsonRecentSetsModel, "recentSets");
                modelsCP.setModel(jsonAllArtistsModel, "allArtists");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        artists = modelsCP.getArtists();
        festivals = modelsCP.getEvents();
        mixes = modelsCP.getMixes();
        genres = modelsCP.getGenres();
        popularSets = modelsCP.getPopularSets();
        recentSets = modelsCP.getRecentSets();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_search_sets, container, false);

        // Find and store view elements

        noResults = (TextView)rootView.findViewById(R.id.noResults);
        setsLoading = (ProgressBar)rootView.findViewById(R.id.setsLoading);
        browseItemList = (ListView)rootView.findViewById(R.id.browseList);
        browseNavContainer = (ViewGroup)rootView.findViewById(R.id.browse_container);
        browseItemListContainer = rootView.findViewById(R.id.browseListContainer);
        searchResultsList = (ListView)rootView.findViewById(R.id.setSearchResults);
        searchView = (SearchView) rootView.findViewById(R.id.search_sets);

        // Initialize search results list adapter

        searchResultSetAdapter = new SearchResultSetAdapter(new ArrayList<Set>());
        searchResultsList.setAdapter(searchResultSetAdapter);

        // Style search box

        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) rootView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.BLACK);
        searchEditText.setHintTextColor(Color.GRAY);

        // Listener for changed text in search bar

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchSets(query);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchSets(newText);
                return false;
            }
        });

        artistAdapter = new ArtistAdapter(artists);
        eventAdapter = new EventAdapter(festivals);
        mixAdapter = new MixAdapter(mixes);
        genreAdapter = new GenreAdapter(genres);

        setBrowseClickListeners();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        outState.putString("artists", modelsCP.jsonMappings.get("artists"));
        outState.putString("festivals", modelsCP.jsonMappings.get("festivals"));
        outState.putString("mixes", modelsCP.jsonMappings.get("mixes"));
        outState.putString("genres", modelsCP.jsonMappings.get("genres"));
        outState.putString("popularSets", modelsCP.jsonMappings.get("popularSets"));
        outState.putString("recentSets", modelsCP.jsonMappings.get("recentSets"));
        outState.putString("allArtists", modelsCP.jsonMappings.get("allArtists"));
        super.onSaveInstanceState(outState);

    }

    public void searchSets(String query) {
        this.searchQuery = query;
        browseItemListContainer.setVisibility(View.GONE);
        for(int i = 0 ; i < browseNavContainer.getChildCount(); i++) {
            browseNavContainer.getChildAt(i).setSelected(false);
        }
        rootView.findViewById(R.id.setSearchResultsContainer).setVisibility(View.VISIBLE);
        searchResultsList.setVisibility(View.GONE);
        setsLoading.setVisibility(View.VISIBLE);
        startTask();
    }

    @Override
    public void startTask() {
        try {
            cancelTask();
            if (!searchQuery.equals("")) {
                getSetsTask = new GetSetsTask(activity,
                        activity.getApplicationContext(), Constants.API_ROOT_URL, this);
                getSetsTask.execute("search/"+ Uri.encode(searchQuery), "searchedSets");
            } else {
                setsLoading.setVisibility(View.GONE);
                noResults.setVisibility(View.GONE);
                searchResultsList.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancelTask() {
        if (getSetsTask != null) {
            getSetsTask.cancel(true);
        }
    }

    @Override
    public void onTaskCompleted(List<Set> list) {
        Log.d(TAG, list.toString());
        searchResultSetAdapter.isRecent = false;
        searchResultSetAdapter.sets = list;
        if(list.size() > 0) {
            searchResultsList.setVisibility(View.VISIBLE);
            setsLoading.setVisibility(View.GONE);
            noResults.setVisibility(View.GONE);
            searchResultSetAdapter.notifyDataSetChanged();
            searchResultsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Set s = searchResultSetAdapter.sets.get(position);
                    activity.playerService.playerManager.setPlaylist(searchResultSetAdapter.sets);
                    activity.playerService.playerManager.selectSetById(s.getId());
                    activity.startPlayerFragment();
                    activity.playSelectedSet();
                }
            });
        }
        else {
            searchResultsList.setVisibility(View.GONE);
            setsLoading.setVisibility(View.GONE);
            noResults.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onTaskFailed() {

    }

    public void setBrowseClickListeners() {
        rootView.findViewById(R.id.browse_artist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didSelectCategory(v);
                browseItemList.setAdapter(artistAdapter);
                browseItemList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                        v.setPressed(true);
                        Artist a = activity.modelsCP.getArtists().get(position);
                        activity.openArtistDetailPage(a);
                    }
                });
            }
        });
        rootView.findViewById(R.id.browse_festival).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didSelectCategory(v);
                browseItemList.setAdapter(eventAdapter);
                browseItemList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                        v.setPressed(true);
                        Event e = activity.modelsCP.getEvents().get(position);
                        activity.openEventDetailPage(e, "recent");
                    }
                });
            }
        });
        rootView.findViewById(R.id.browse_mix).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didSelectCategory(v);
                browseItemList.setAdapter(mixAdapter);
                browseItemList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                        v.setPressed(true);
                        Mix m = activity.modelsCP.getMixes().get(position);
                        searchView.setQuery(m.getMix(), false);
                    }
                });

            }
        });
        rootView.findViewById(R.id.browse_genre).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didSelectCategory(v);
                browseItemList.setAdapter(genreAdapter);
                browseItemList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                        v.setPressed(true);
                        Genre g = activity.modelsCP.getGenres().get(position);
                        searchView.setQuery(g.getGenre(), false);
                    }
                });
            }
        });

        rootView.findViewById(R.id.browse_popular).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didSelectPopularRecent(v);
                searchResultSetAdapter.isRecent = false;
                searchResultSetAdapter.sets = popularSets;
                searchResultsList.setVisibility(View.VISIBLE);
                setsLoading.setVisibility(View.GONE);
                noResults.setVisibility(View.GONE);
                searchResultSetAdapter.notifyDataSetChanged();
                searchResultsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Set s = searchResultSetAdapter.sets.get(position);
                        activity.playerService.playerManager.setPlaylist(searchResultSetAdapter.sets);
                        activity.playerService.playerManager.selectSetById(s.getId());
                        activity.startPlayerFragment();
                        activity.playSelectedSet();
                    }
                });
            }
        });

        rootView.findViewById(R.id.browse_recent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didSelectPopularRecent(v);
                searchResultSetAdapter.isRecent = true;
                searchResultSetAdapter.sets = recentSets;
                searchResultsList.setVisibility(View.VISIBLE);
                setsLoading.setVisibility(View.GONE);
                noResults.setVisibility(View.GONE);
                searchResultSetAdapter.notifyDataSetChanged();
//                  activity.playerManager.setPlaylist(searchResultSetAdapter.sets);
                searchResultsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Set s = searchResultSetAdapter.sets.get(position);
                        activity.playerService.playerManager.setPlaylist(searchResultSetAdapter.sets);
                        activity.playerService.playerManager.selectSetById(s.getId());
                        activity.startPlayerFragment();
                        activity.playSelectedSet();
                    }
                });
            }
        });
    }

    public void didSelectCategory(View v) {
        selectedBrowseView = v;
        for(int i = 0 ; i < browseNavContainer.getChildCount(); i++) {
            browseNavContainer.getChildAt(i).setSelected(false);
        }
        v.setSelected(true);
        rootView.findViewById(R.id.setSearchResultsContainer).setVisibility(View.GONE);
        browseItemListContainer.setVisibility(View.VISIBLE);
    }

    public void didSelectPopularRecent(View v) {
        selectedBrowseView = v;
        for(int i = 0 ; i < browseNavContainer.getChildCount(); i++) {
            browseNavContainer.getChildAt(i).setSelected(false);
        }
        v.setSelected(true);
        browseItemListContainer.setVisibility(View.GONE);
        rootView.findViewById(R.id.setSearchResultsContainer).setVisibility(View.VISIBLE);
    }

    public static class BrowseItemHolder {
        TextView browseItemText;
    }

    // Adapters for Browse Lists

    public class ArtistAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<Artist> artists;

        ArtistAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        ArtistAdapter(List<Artist> artists) {
            this();
            this.artists = artists;
        }

        @Override
        public int getCount() {
            return artists.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final BrowseItemHolder holder;
            Artist artist = artists.get(position);
            if (convertView == null) {
                view = inflater.inflate(R.layout.browse_list_tile, parent, false);
                holder = new BrowseItemHolder();
                holder.browseItemText = (TextView) view.findViewById(R.id.browseItemText);
                view.setTag(holder);
            } else {
                holder = (BrowseItemHolder) view.getTag();
            }
            holder.browseItemText.setText(artist.getArtist());
            return view;
        }
    }

    public class EventAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<Event> events;

        EventAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        EventAdapter(List<Event> events) {
            this();
            this.events = events;
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
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final BrowseItemHolder holder;
            Event event = events.get(position);
            if (convertView == null) {
                view = inflater.inflate(R.layout.browse_list_tile, parent, false);
                holder = new BrowseItemHolder();
                holder.browseItemText = (TextView) view.findViewById(R.id.browseItemText);
                view.setTag(holder);
            } else {
                holder = (BrowseItemHolder) view.getTag();
            }
            holder.browseItemText.setText(event.getEvent());
            return view;
        }
    }

    public class MixAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<Mix> mixes;

        MixAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        MixAdapter(List<Mix> mixes) {
            this();
            this.mixes = mixes;
        }

        @Override
        public int getCount() {
            return mixes.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final BrowseItemHolder holder;

            Mix mix = mixes.get(position);
            if (convertView == null) {
                view = inflater.inflate(R.layout.browse_list_tile, parent, false);
                holder = new BrowseItemHolder();
                holder.browseItemText = (TextView) view.findViewById(R.id.browseItemText);
                view.setTag(holder);
            } else {
                holder = (BrowseItemHolder) view.getTag();
            }
            holder.browseItemText.setText(mix.getMix());
            return view;
        }
    }

    public class GenreAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private List<Genre> genres;

        GenreAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        GenreAdapter(List<Genre> genres) {
            this();
            this.genres = genres;
        }

        @Override
        public int getCount() {
            return genres.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final BrowseItemHolder holder;
            Genre genre = genres.get(position);
            if (convertView == null) {
                view = inflater.inflate(R.layout.browse_list_tile, parent, false);
                holder = new BrowseItemHolder();
                holder.browseItemText = (TextView) view.findViewById(R.id.browseItemText);
                view.setTag(holder);
            } else {
                holder = (BrowseItemHolder) view.getTag();
            }
            holder.browseItemText.setText(genre.getGenre());
            return view;
        }
    }


    class SearchResultSetAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ImageLoadingListener animateFirstListener = new EventDetailFragment.AnimateFirstDisplayListener();
        public List<Set> sets;
        public boolean isRecent;

        SearchResultSetAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        SearchResultSetAdapter(List<Set> Sets) {
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
                holder.playButton = (ImageView) view.findViewById(R.id.playsIcon);
                view.setTag(holder);
                view.setId(Integer.valueOf(set.getId()).intValue());
            } else {
                holder = (SearchResultSetViewHolder) view.getTag();
            }

            holder.playCount.setText(set.getPopularity() + " plays");
            int playID = getResources().getIdentifier("com.setmine.android:drawable/ic_action_play", null, null);
            holder.playButton.setImageResource(playID);
            if(isRecent) {
                holder.playCount.setText((new DateUtils()).convertDateToDaysAgo(set.getDatetime()));
                int timeID = getResources().getIdentifier("com.setmine.android:drawable/recent_icon", null, null);
                holder.playButton.setImageResource(timeID);
            }
            holder.setLength.setText(set.getSetLength());
            holder.artistText.setText(set.getArtist());
            holder.eventText.setText(set.getEvent());

            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.logo_small)
                    .showImageForEmptyUri(R.drawable.logo_small)
                    .showImageOnFail(R.drawable.logo_small)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .build();

            ImageLoader.getInstance().displayImage(set.getArtistImage(), holder.artistImage, options, animateFirstListener);

            return view;
        }
    }



}
