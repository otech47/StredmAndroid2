package com.setmine.android.search;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.setmine.android.ModelsContentProvider;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.api.SetMineApiGetRequestAsyncTask;
import com.setmine.android.artist.Artist;
import com.setmine.android.event.Event;
import com.setmine.android.event.EventDetailFragment;
import com.setmine.android.genre.Genre;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.object.SearchResultEventHolder;
import com.setmine.android.object.SearchResultTrackHolder;
import com.setmine.android.object.TrackResponse;
import com.setmine.android.set.Mix;
import com.setmine.android.set.SearchResultSetViewHolder;
import com.setmine.android.set.Set;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchSetsFragment extends Fragment implements ApiCaller {

    // Statics
    private static final String TAG = "SearchSetsFragment";

    // Views
    public View rootView;
    public SearchView searchView;
    public ListView browseItemList;
    public ListView searchedSetsList;
    public LinearLayout listOptionButtons;
    public TextView noResults;
    public View loader;
    public ProgressBar setsLoading;
    public ViewGroup browseNavContainer;
    public View browseItemListContainer;
    public View selectedBrowseView;

    // Models
    public List<Artist> artists;
    public List<Event> festivals;
    public List<Mix> mixes;
    public List<Genre> genres;
    public List<Set> popularSets;
    public List<Set> recentSets;
    public List<Set> searchedSets;
    public List<Event> searchedEvents;
    public List<TrackResponse> searchedTracks;

    public int initialModels;

    private SetMineMainActivity activity;
    public String searchQuery;
    public SetMineApiGetRequestAsyncTask getSetsTask;

    public ModelsContentProvider modelsCP;
    public boolean modelsReady;
    private enum ListOptions {SETS, EVENTS, TRACKRESPONSES};

    public SearchResultSetAdapter searchResultSetAdapter;
    public ArtistAdapter artistAdapter;
    public EventAdapter eventAdapter;
    public MixAdapter mixAdapter;
    public GenreAdapter genreAdapter;

    final Handler handler = new Handler();

    final Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            finishOnCreate();
        }
    };

    @Override
    public void onApiResponseReceived(final JSONObject jsonObject, final String identifier) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(identifier.equals("artists")) {
                    artists = ModelsContentProvider.createModel(jsonObject, identifier);
                    initialModels++;
                } else if(identifier.equals("festivals")) {
                    festivals = ModelsContentProvider.createModel(jsonObject, identifier);
                    initialModels++;
                } else if(identifier.equals("mixes")) {
                    mixes = ModelsContentProvider.createModel(jsonObject, identifier);
                    initialModels++;
                } else if(identifier.equals("genres")) {
                    genres = ModelsContentProvider.createModel(jsonObject, identifier);
                    initialModels++;
                } else if(identifier.equals("popularSets")) {
                    popularSets = ModelsContentProvider.createModel(jsonObject, identifier);
                    initialModels++;
                } else if(identifier.equals("recentSets")) {
                    recentSets = ModelsContentProvider.createModel(jsonObject, identifier);
                    initialModels++;
                } else if(identifier.equals("search")) {
                    searchedSets = ModelsContentProvider.createModel(jsonObject, "searchedSets");
                    searchedEvents = ModelsContentProvider.createModel(jsonObject, "searchedEvents");
                    searchedTracks = ModelsContentProvider.createModel(jsonObject, "searchedTracks");
                    populateSearchResults(searchedSets, searchedEvents, searchedTracks);
                }
                handler.post(updateUI);
            }
        }).start();
    }

    public void finishOnCreate() {
        if(initialModels == 6) {
            artistAdapter = new ArtistAdapter(artists);
            eventAdapter = new EventAdapter(festivals);
            mixAdapter = new MixAdapter(mixes);
            genreAdapter = new GenreAdapter(genres);
            setBrowseClickListeners();
            loader.setVisibility(View.GONE);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");


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
        searchedSetsList = (ListView)rootView.findViewById(R.id.setSearchResults);
        searchView = (SearchView) rootView.findViewById(R.id.search_sets);
        listOptionButtons = (LinearLayout) rootView.findViewById(R.id.list_option_buttons);
        loader = rootView.findViewById(R.id.loader_container);

        // Initialize search results list adapter

        searchResultSetAdapter = new SearchResultSetAdapter(new ArrayList<Set>(), new ArrayList<Event>(), new ArrayList<TrackResponse>());
        searchedSetsList.setAdapter(searchResultSetAdapter);

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

        // Recover data from saved instance or make API calls

        initialModels = 0;

        artists = new ArrayList<Artist>();
        festivals = new ArrayList<Event>();
        mixes = new ArrayList<Mix>();
        genres = new ArrayList<Genre>();
        popularSets = new ArrayList<Set>();
        recentSets = new ArrayList<Set>();

        if(savedInstanceState == null) {
            this.activity = (SetMineMainActivity)getActivity();
            new SetMineApiGetRequestAsyncTask(activity, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "artist?all=true", "artists");
            new SetMineApiGetRequestAsyncTask(activity, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "festival", "festivals");
            new SetMineApiGetRequestAsyncTask(activity, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "mix", "mixes");
            new SetMineApiGetRequestAsyncTask(activity, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "genre", "genres");
            new SetMineApiGetRequestAsyncTask(activity, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "popular", "popularSets");
            new SetMineApiGetRequestAsyncTask(activity, this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "recent", "recentSets");
        } else {
            final ArrayList<String> artistsModel = savedInstanceState.getStringArrayList("artists");
            final ArrayList<String> festivalsModel = savedInstanceState.getStringArrayList("festivals");
            final ArrayList<String> mixesModel = savedInstanceState.getStringArrayList("mixes");
            final ArrayList<String> genresModel = savedInstanceState.getStringArrayList("genres");
            final ArrayList<String> popularSetsModel = savedInstanceState.getStringArrayList("popularSets");
            final ArrayList<String> recentSetsModel = savedInstanceState.getStringArrayList("recentSets");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < artistsModel.size(); i++) {
                            artists.add(new Artist(new JSONObject(artistsModel.get(i))));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < festivalsModel.size(); i++) {
                            festivals.add(new Event(new JSONObject(festivalsModel.get(i))));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < mixesModel.size(); i++) {
                            mixes.add(new Mix(new JSONObject(mixesModel.get(i))));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < genresModel.size(); i++) {
                            genres.add(new Genre(new JSONObject(genresModel.get(i))));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < popularSetsModel.size(); i++) {
                            popularSets.add(new Set(new JSONObject(popularSetsModel.get(i))));
                        }
                        for (int i = 0; i < recentSetsModel.size(); i++) {
                            recentSets.add(new Set(new JSONObject(recentSetsModel.get(i))));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            finishOnCreate();
        }

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
        ArrayList<String> artistsJsonStringArray = new ArrayList<String>();
        for(int i = 0 ; i < artists.size() ; i++) {
            artistsJsonStringArray.add(artists.get(i).jsonModelString);
        }
        ArrayList<String> festivalsJsonStringArray = new ArrayList<String>();
        for(int i = 0 ; i < festivals.size() ; i++) {
            festivalsJsonStringArray.add(festivals.get(i).jsonModelString);
        }
        ArrayList<String> mixesJsonStringArray = new ArrayList<String>();
        for(int i = 0 ; i < mixes.size() ; i++) {
            mixesJsonStringArray.add(mixes.get(i).jsonModelString);
        }
        ArrayList<String> genresJsonStringArray = new ArrayList<String>();
        for(int i = 0 ; i < genres.size() ; i++) {
            genresJsonStringArray.add(genres.get(i).jsonModelString);
        }
        ArrayList<String> popularSetsJsonStringArray = new ArrayList<String>();
        for(int i = 0 ; i < popularSets.size() ; i++) {
            popularSetsJsonStringArray.add(popularSets.get(i).jsonModelString);
        }
        ArrayList<String> recentSetsJsonStringArray = new ArrayList<String>();
        for(int i = 0 ; i < recentSets.size() ; i++) {
            recentSetsJsonStringArray.add(recentSets.get(i).jsonModelString);
        }

        outState.putStringArrayList("artists", artistsJsonStringArray);
        outState.putStringArrayList("festivals", festivalsJsonStringArray);
        outState.putStringArrayList("mixes", mixesJsonStringArray);
        outState.putStringArrayList("genres", genresJsonStringArray);
        outState.putStringArrayList("popularSets", popularSetsJsonStringArray);
        outState.putStringArrayList("recentSets", recentSetsJsonStringArray);
        super.onSaveInstanceState(outState);

    }

    public void searchSets(String query) {
        this.searchQuery = query;
        browseItemListContainer.setVisibility(View.GONE);
        for(int i = 0 ; i < browseNavContainer.getChildCount(); i++) {
            browseNavContainer.getChildAt(i).setSelected(false);
        }
        rootView.findViewById(R.id.setSearchResultsContainer).setVisibility(View.VISIBLE);
        searchedSetsList.setVisibility(View.GONE);
        listOptionButtons.setVisibility(View.GONE);
        setsLoading.setVisibility(View.VISIBLE);
        startTask();
    }

    public void startTask() {
        Log.d(TAG, "startTask");
        try {
            cancelTask();
            if (!searchQuery.equals("")) {
                getSetsTask = new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this);
                getSetsTask.executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                        , "search/" + Uri.encode(searchQuery), "search");
            } else {
                setsLoading.setVisibility(View.GONE);
                noResults.setVisibility(View.GONE);
                searchedSetsList.setVisibility(View.GONE);
                listOptionButtons.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelTask() {
        Log.d(TAG, "cancelTask");
        if (getSetsTask != null) {
            Log.d(TAG, "task canceled");
            getSetsTask.cancel(true);
        }
    }

    public void populateSearchResults(List<Set> sets, List<Event> events, List<TrackResponse> tracks) {
        searchResultSetAdapter.isRecent = false;
        if(sets.size() > 0) {
            searchResultSetAdapter.sets = sets;
        }
        if(events.size() > 0) {
            searchResultSetAdapter.upcomingEvents = events;
        }
        if(tracks.size() > 0) {
            searchResultSetAdapter.tracks = tracks;
        }
        if(sets.size() > 0 || events.size() > 0 || tracks.size() > 0) {
            listOptionButtons.setVisibility(View.VISIBLE);
            searchedSetsList.setVisibility(View.VISIBLE);
            setsLoading.setVisibility(View.GONE);
            noResults.setVisibility(View.GONE);
            searchResultSetAdapter.notifyDataSetChanged();
            searchedSetsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    activity = (SetMineMainActivity)getActivity();
                    switch (searchResultSetAdapter.listState) {
                        case SETS:
                            Set s = searchResultSetAdapter.sets.get(position);
                            activity.playerService.playerManager.setPlaylist(searchResultSetAdapter.sets);
                            activity.playerService.playerManager.selectSetById(s.getId());
                            activity.playSelectedSet();
                            break;
                        case EVENTS:
                            String e = searchResultSetAdapter.upcomingEvents.get(position).getId();
                            ((SetMineMainActivity) getActivity()).openEventDetailPage(e, "upcoming");
                            break;
                        case TRACKRESPONSES:
                            TrackResponse tr = searchResultSetAdapter.tracks.get(position);
                            List<Set> setsOfTracks = new ArrayList<Set>();
                            for(int i = 0; i<searchResultSetAdapter.tracks.size(); i++) {
                                setsOfTracks.add(searchResultSetAdapter.tracks.get(i).getSet());
                            }
                            activity.playerService.playerManager.setPlaylist(setsOfTracks);
                            activity.playerService.playerManager.selectSetById(tr.getId());
                            activity.playSelectedSet();
                            break;
                    }
                }
            });
        }
        else {
            listOptionButtons.setVisibility(View.GONE);
            searchedSetsList.setVisibility(View.GONE);
            setsLoading.setVisibility(View.GONE);
            noResults.setVisibility(View.VISIBLE);
        }

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
                        String a = artists.get(position).getArtist();
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
                        String e = festivals.get(position).getEvent();
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
                        Mix m = mixes.get(position);
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
                        Genre g = genres.get(position);
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
                searchedSetsList.setVisibility(View.VISIBLE);
                listOptionButtons.setVisibility(View.GONE);
                setsLoading.setVisibility(View.GONE);
                noResults.setVisibility(View.GONE);
                searchResultSetAdapter.notifyDataSetChanged();
                searchedSetsList.setOnItemClickListener(new ListView.OnItemClickListener() {
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
                searchedSetsList.setVisibility(View.VISIBLE);
                listOptionButtons.setVisibility(View.GONE);
                setsLoading.setVisibility(View.GONE);
                noResults.setVisibility(View.GONE);
                searchResultSetAdapter.notifyDataSetChanged();
                searchedSetsList.setOnItemClickListener(new ListView.OnItemClickListener() {
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

        rootView.findViewById(R.id.searchResultSets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchResultSetAdapter.listState = ListOptions.SETS;
                searchResultSetAdapter.notifyDataSetChanged();
            }
        });

        rootView.findViewById(R.id.searchResultUpcomingEvents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchResultSetAdapter.listState = ListOptions.EVENTS;
                searchResultSetAdapter.notifyDataSetChanged();
            }
        });

        rootView.findViewById(R.id.searchResultTracks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchResultSetAdapter.listState = ListOptions.TRACKRESPONSES;
                searchResultSetAdapter.notifyDataSetChanged();
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

    public class SearchResultSetAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ImageLoadingListener animateFirstListener = new EventDetailFragment.AnimateFirstDisplayListener();
        public List<Set> sets;
        public boolean isRecent;
        public List<Event> upcomingEvents;
        public List<TrackResponse> tracks;
        public ListOptions listState = ListOptions.SETS;

        SearchResultSetAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        SearchResultSetAdapter(List<Set> Sets, List<Event> Events, List<TrackResponse> TrackResponses) {
            this();
            sets = Sets;
            upcomingEvents = Events;
            tracks = TrackResponses;
        }

        public void changeListType(ListOptions option) {
            this.listState = option;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            int size = 0;
            switch (listState) {
                case SETS:
                    size = sets.size();
                    break;
                case EVENTS:
                    size = upcomingEvents.size();
                    break;
                case TRACKRESPONSES:
                    size = tracks.size();
                    break;
            }
            return size;
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
            final SearchResultSetViewHolder setViewHolder;
            final SearchResultEventHolder eventHolder;
            final SearchResultTrackHolder trackHolder;

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.logo_small)
                    .showImageForEmptyUri(R.drawable.logo_small)
                    .showImageOnFail(R.drawable.logo_small)
                    .cacheInMemory(false)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .build();

            switch (listState) {
                case SETS:
                    final Set set = sets.get(position);
                    if (convertView != null && view.getTag() instanceof SearchResultSetViewHolder) {
                        setViewHolder = (SearchResultSetViewHolder) view.getTag();
                    } else {
                        view = inflater.inflate(R.layout.set_tile, parent, false);
                        setViewHolder = new SearchResultSetViewHolder();
                        setViewHolder.playCount = (TextView) view.findViewById(R.id.playCount);
                        setViewHolder.setLength = (TextView) view.findViewById(R.id.setLength);
                        setViewHolder.artistText = (TextView) view.findViewById(R.id.artistText);
                        setViewHolder.eventText = (TextView) view.findViewById(R.id.eventText);
                        setViewHolder.artistImage = (ImageView) view.findViewById(R.id.artistImage);
                        setViewHolder.playButton = (ImageView) view.findViewById(R.id.playsIcon);
                        view.setTag(setViewHolder);
                        view.setId(Integer.valueOf(set.getId()).intValue());
                    }
                    setViewHolder.playCount.setText(set.getPopularity() + " plays");
                    setViewHolder.setLength.setText(set.getSetLength());
                    setViewHolder.artistText.setText(set.getArtist());
                    setViewHolder.eventText.setText(set.getEvent());



                    ImageLoader.getInstance().displayImage(set.getArtistImage(), setViewHolder.artistImage, options, animateFirstListener);
                    break;
                case EVENTS:
                    final Event event = upcomingEvents.get(position);
                    if (convertView != null && view.getTag() instanceof SearchResultEventHolder) {
                        eventHolder = (SearchResultEventHolder) view.getTag();
                    } else {
                        view = inflater.inflate(R.layout.event_tile_upcoming_small, parent, false);
                        eventHolder = new SearchResultEventHolder();
                        eventHolder.dates = (TextView) view.findViewById(R.id.dates);
                        eventHolder.eventText = (TextView) view.findViewById(R.id.eventText);
                        eventHolder.eventImage = (ImageView) view.findViewById(R.id.eventImage);
                        view.setTag(eventHolder);
                        view.setId(Integer.valueOf(event.getId()).intValue());
                    }
                    eventHolder.dates.setText(event.getStartDate()+ "-"+event.getEndDate());
                    eventHolder.eventText.setText(event.getEvent());

                    ImageLoader.getInstance().displayImage(event.getIconImageUrl(), eventHolder.eventImage, options, animateFirstListener);

                    break;
                case TRACKRESPONSES:
                    final TrackResponse track = tracks.get(position);
                    if (convertView != null && view.getTag() instanceof SearchResultTrackHolder) {
                        trackHolder = (SearchResultTrackHolder) view.getTag();
                    } else {
                        view = inflater.inflate(R.layout.set_tile, parent, false);
                        trackHolder = new SearchResultTrackHolder();
                        trackHolder.setLength = (TextView) view.findViewById(R.id.setLength);
                        trackHolder.artistText = (TextView) view.findViewById(R.id.artistText);
                        trackHolder.eventText = (TextView) view.findViewById(R.id.eventText);
                        trackHolder.artistImage = (ImageView) view.findViewById(R.id.artistImage);
                        view.setTag(trackHolder);
                        view.setId(Integer.valueOf(track.getId()).intValue());
                    }
                    trackHolder.setLength.setText(track.getSetLength());
                    trackHolder.artistText.setText(track.getArtist());
                    trackHolder.eventText.setText(track.getEvent());

                    ImageLoader.getInstance().displayImage(track.getArtistImage(), trackHolder.artistImage, options, animateFirstListener);
                    break;
            }

            return view;
        }
    }



}
