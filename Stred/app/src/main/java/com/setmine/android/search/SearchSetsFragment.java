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
import com.setmine.android.interfaces.OnTaskCompleted;
import com.setmine.android.object.SearchResultEventHolder;
import com.setmine.android.object.SearchResultTrackHolder;
import com.setmine.android.object.TrackResponse;
import com.setmine.android.set.Mix;
import com.setmine.android.set.SearchResultSetViewHolder;
import com.setmine.android.set.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchSetsFragment extends Fragment implements OnTaskCompleted<Set>, ApiCaller {

    private static final String TAG = "SearchSetsFragment";

    private SetMineMainActivity activity;
    public SearchSetsFragment thisFragment = this;
    public DisplayImageOptions options;
    public String searchQuery;
    public SetMineApiGetRequestAsyncTask getSetsTask;

    public ModelsContentProvider modelsCP;
    public boolean modelsReady;
    private enum ListOptions {SETS, EVENTS, TRACKRESPONSES};

    public View rootView;
    public SearchView searchView;
    public ListView browseItemList;
    public ListView searchedSetsList;
    public LinearLayout listOptionButtons;
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
    public List<Artist> allArtists;

    public Bundle toOutstate = new Bundle();

    public int modelsLoaded = 0;

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
    public void onApiResponseReceived(final JSONObject jsonObject, String identifier) {
        final JSONObject finalJsonObject = jsonObject;
        final String finalIdentifier = identifier;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onApiResponseReceived: ");
                try {
                    toOutstate.putString(finalIdentifier, finalJsonObject.toString()); // For savedInstanceState
                    JSONObject payload = finalJsonObject.getJSONObject("payload");
                    if(finalIdentifier.equals("artists")) {
                        JSONArray artistsArray = payload.getJSONArray("artist");
                        for(int i = 0; i < artistsArray.length(); i++) {
                            artists.add(new Artist(artistsArray.getJSONObject(i)));
                        }
                        modelsLoaded++;
                    } else if(finalIdentifier.equals("festivals")) {
                        JSONArray festivalsArray = payload.getJSONArray("festival");
                        for(int i = 0; i < festivalsArray.length(); i++) {
                            festivals.add(new Event(festivalsArray.getJSONObject(i)));
                        }
                        modelsLoaded++;
                    } else if(finalIdentifier.equals("mixes")) {
                        JSONArray mixesArray = payload.getJSONArray("mix");
                        for(int i = 0; i < mixesArray.length(); i++) {
                            mixes.add(new Mix(mixesArray.getJSONObject(i)));
                        }
                        modelsLoaded++;
                    } else if(finalIdentifier.equals("genres")) {
                        JSONArray genresArray = payload.getJSONArray("genre");
                        for(int i = 0; i < genresArray.length(); i++) {
                            genres.add(new Genre("1", genresArray.getString(i))); // Upgrade API for genres
                        }
                        modelsLoaded++;
                    } else if(finalIdentifier.equals("popularSets")) {
                        JSONArray popularSetsArray = payload.getJSONArray("popular");
                        for(int i = 0; i < popularSetsArray.length(); i++) {
                            popularSets.add(new Set(popularSetsArray.getJSONObject(i)));
                        }
                        modelsLoaded++;
                    } else if(finalIdentifier.equals("recentSets")) {
                        JSONArray recentSetsArray = payload.getJSONArray("recent");
                        for(int i = 0; i < recentSetsArray.length(); i++) {
                            popularSets.add(new Set(recentSetsArray.getJSONObject(i)));
                        }
                        modelsLoaded++;
                    } else if(finalIdentifier.equals("allArtists")) {
                        JSONArray allArtistsArray = payload.getJSONArray("artist");
                        for(int i = 0; i < allArtistsArray.length(); i++) {
                            allArtists.add(new Artist(allArtistsArray.getJSONObject(i)));
                        }
                        modelsLoaded++;
                    }
                    if(modelsLoaded == 7) {
                        handler.post(updateUI);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        this.activity = (SetMineMainActivity)getActivity();

        // Recover data from saved instance or retrieve from activity
        artists = new ArrayList<Artist>();
        festivals = new ArrayList<Event>();
        mixes = new ArrayList<Mix>();
        genres = new ArrayList<Genre>();
        popularSets = new ArrayList<Set>();
        recentSets = new ArrayList<Set>();
        allArtists = new ArrayList<Artist>();

        modelsLoaded = 0;

        if(savedInstanceState == null) {

            new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "festival", "festivals");
            new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "mix", "mixes");
            new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "genre", "genres");
            new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "popular", "popularSets");
            new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "recent", "recentSets");
            new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                    .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                            , "artist?all=true", "allArtists");
        } else {
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
                onApiResponseReceived(jsonArtistsModel, "artists");
                onApiResponseReceived(jsonFestivalsModel, "festivals");
                onApiResponseReceived(jsonMixesModel, "mixes");
                onApiResponseReceived(jsonGenresModel, "genres");
                onApiResponseReceived(jsonPopularSetsModel, "popularSets");
                onApiResponseReceived(jsonRecentSetsModel, "recentSets");
                onApiResponseReceived(jsonAllArtistsModel, "allArtists");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
        outState.putString("artists", toOutstate.getString("artists"));
        outState.putString("festivals", toOutstate.getString("festivals"));
        outState.putString("mixes", toOutstate.getString("mixes"));
        outState.putString("genres", toOutstate.getString("genres"));
        outState.putString("popularSets", toOutstate.getString("popularSets"));
        outState.putString("recentSets", toOutstate.getString("recentSets"));
        outState.putString("allArtists", toOutstate.getString("allArtists"));

        super.onSaveInstanceState(outState);

    }

    public void finishOnCreate() {
        rootView.findViewById(R.id.search_loading).setVisibility(View.GONE);
        artistAdapter = new ArtistAdapter(artists);
        eventAdapter = new EventAdapter(festivals);
        mixAdapter = new MixAdapter(mixes);
        genreAdapter = new GenreAdapter(genres);

        setBrowseClickListeners();
    }

    public void searchSets(String query) {
        this.searchQuery = query;

        // Clears the browse results list container
        browseItemListContainer.setVisibility(View.GONE);

        // Clears left browse of any selections
        for(int i = 0 ; i < browseNavContainer.getChildCount(); i++) {
            browseNavContainer.getChildAt(i).setSelected(false);
        }

        // Shows the search results list container
        rootView.findViewById(R.id.setSearchResultsContainer).setVisibility(View.VISIBLE);

        // Hides the actual list and shows loader options until search has completed
        searchedSetsList.setVisibility(View.GONE);
        listOptionButtons.setVisibility(View.GONE);
        setsLoading.setVisibility(View.VISIBLE);
        startTask();
    }

    @Override
    public void startTask() {
        try {
            cancelTask();
            if (!searchQuery.equals("")) {
                getSetsTask = (SetMineApiGetRequestAsyncTask) new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                                , "search/" + Uri.encode(searchQuery), "searchedSets");
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

    @Override
    public void cancelTask() {
        if (getSetsTask != null) {
            Log.d(TAG, "canceled existing task");
            getSetsTask.cancel(true);
        }
    }

    @Override
    public void onTaskCompleted(List<Set> list) {
        Log.d(TAG, list.toString());
        searchResultSetAdapter.isRecent = false;
        searchResultSetAdapter.sets = list;
        List<Event> upcomingEvents = modelsCP.upcomingEvents;
        if(upcomingEvents.size() > 0) {
            searchResultSetAdapter.upcomingEvents = upcomingEvents;
        }
        List<TrackResponse> trackResponses = modelsCP.searchedTracks;
        if(trackResponses.size() > 0) {
            searchResultSetAdapter.tracks = trackResponses;
        }
        if(list.size() > 0) {
            listOptionButtons.setVisibility(View.VISIBLE);
            searchedSetsList.setVisibility(View.VISIBLE);
            setsLoading.setVisibility(View.GONE);
            noResults.setVisibility(View.GONE);
            searchResultSetAdapter.notifyDataSetChanged();
            searchedSetsList.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (searchResultSetAdapter.listState) {
                        case SETS:
                            Set s = searchResultSetAdapter.sets.get(position);
                            activity.playerService.playerManager.setPlaylist(searchResultSetAdapter.sets);
                            activity.playerService.playerManager.selectSetById(s.getId());
                            activity.playSelectedSet();
                            break;
                        case EVENTS:
                            Event e = searchResultSetAdapter.upcomingEvents.get(position);
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
            searchedSetsList.setVisibility(View.GONE);
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
                new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), thisFragment)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                                , "artist", "artists");
                
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
                searchedSetsList.setVisibility(View.VISIBLE);
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
                setsLoading.setVisibility(View.GONE);
                noResults.setVisibility(View.GONE);
                searchResultSetAdapter.notifyDataSetChanged();
//                  activity.playerManager.setPlaylist(searchResultSetAdapter.sets);
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
                    setViewHolder.playCount.setText(set.getSetLength());
                    setViewHolder.artistText.setText(set.getArtist());
                    setViewHolder.eventText.setText(set.getEvent());

                    options = new DisplayImageOptions.Builder()
                            .showImageOnLoading(R.drawable.logo_small)
                            .showImageForEmptyUri(R.drawable.logo_small)
                            .showImageOnFail(R.drawable.logo_small)
                            .cacheInMemory(true)
                            .cacheOnDisk(true)
                            .considerExifParams(true)
                            .build();

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

                    options = new DisplayImageOptions.Builder()
                            .showImageOnLoading(R.drawable.logo_small)
                            .showImageForEmptyUri(R.drawable.logo_small)
                            .showImageOnFail(R.drawable.logo_small)
                            .cacheInMemory(true)
                            .cacheOnDisk(true)
                            .considerExifParams(true)
                            .build();

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

                    options = new DisplayImageOptions.Builder()
                            .showImageOnLoading(R.drawable.logo_small)
                            .showImageForEmptyUri(R.drawable.logo_small)
                            .showImageOnFail(R.drawable.logo_small)
                            .cacheInMemory(true)
                            .cacheOnDisk(true)
                            .considerExifParams(true)
                            .build();

                    ImageLoader.getInstance().displayImage(track.getArtistImage(), trackHolder.artistImage, options, animateFirstListener);
                    break;
            }

            return view;
        }
    }



}
