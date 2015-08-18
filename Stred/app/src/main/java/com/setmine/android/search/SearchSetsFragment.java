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
import com.setmine.android.track.Track;
import com.setmine.android.util.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchSetsFragment extends Fragment implements ApiCaller {

    private static final String TAG = "SearchSetsFragment";

    public DisplayImageOptions options;
    public String searchQuery;
    public SetMineApiGetRequestAsyncTask getSetsTask;

    private enum ListOptions {SETS, EVENTS, TRACKRESPONSES, ARTISTS};

    public View rootView;

    public List<Artist> artists;
    public List<Event> festivals;
    public List<Mix> mixes;
    public List<Genre> genres;
    public List<Set> popularSets;
    public List<Set> recentSets;

    public List<Set> searchedSets;
    public List<Event> searchedEvents;
    public List<TrackResponse> searchedTracks;
    public List<Artist> searchedArtists;

    public Bundle toOutstate = new Bundle();

    public SearchResultSetAdapter searchResultSetAdapter;
    public ArtistAdapter artistAdapter;
    public EventAdapter eventAdapter;
    public MixAdapter mixAdapter;
    public GenreAdapter genreAdapter;

    final Handler handler = new Handler();
    public String lastIdentifier;
    public String handlerParameter;

    final Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            if(handlerParameter.equals("browse")) {
                displayBrowseResults(lastIdentifier);
            } else if(handlerParameter.equals("search")) {
                displaySearchResults();
            }
        }
    };

    @Override
    public void onApiResponseReceived(final JSONObject jsonObject, String identifier) {
        final JSONObject finalJsonObject = jsonObject;
        final String finalIdentifier = identifier;
        lastIdentifier = finalIdentifier;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onApiResponseReceived: ");
                try {
                    toOutstate.putString(finalIdentifier, finalJsonObject.toString()); // For savedInstanceState
                    JSONObject payload = finalJsonObject.getJSONObject("payload");
                    if(finalIdentifier.equals("festivals")) {
                        JSONArray festivalsArray = payload.getJSONArray("festival");
                        for(int i = 0; i < festivalsArray.length(); i++) {
                            festivals.add(new Event(festivalsArray.getJSONObject(i)));
                        }
                        handlerParameter = "browse";
                    } else if(finalIdentifier.equals("mixes")) {
                        JSONArray mixesArray = payload.getJSONArray("mix");
                        for(int i = 0; i < mixesArray.length(); i++) {
                            mixes.add(new Mix(mixesArray.getJSONObject(i)));
                        }
                        handlerParameter = "browse";
                    } else if(finalIdentifier.equals("genres")) {
                        JSONArray genresArray = payload.getJSONArray("genre");
                        for(int i = 0; i < genresArray.length(); i++) {
                            genres.add(new Genre("1", genresArray.getString(i))); // Upgrade API for genres
                        }
                        handlerParameter = "browse";
                    } else if(finalIdentifier.equals("popularSets")) {
                        JSONArray popularSetsArray = payload.getJSONArray("popular");
                        for(int i = 0; i < popularSetsArray.length(); i++) {
                            popularSets.add(new Set(popularSetsArray.getJSONObject(i)));
                        }
                        handlerParameter = "browse";
                    } else if(finalIdentifier.equals("recentSets")) {
                        JSONArray recentSetsArray = payload.getJSONArray("recent");
                        for(int i = 0; i < recentSetsArray.length(); i++) {
                            recentSets.add(new Set(recentSetsArray.getJSONObject(i)));
                        }
                        handlerParameter = "browse";
                    } else if(finalIdentifier.equals("artists")) {
                        JSONArray artistsArray = payload.getJSONArray("artist");
                        for(int i = 0; i < artistsArray.length(); i++) {
                            artists.add(new Artist(artistsArray.getJSONObject(i)));
                        }
                        handlerParameter = "browse";
                    } else if(finalIdentifier.equals("search")) {
                        JSONObject search = payload.getJSONObject("search");
                        JSONArray sets = search.getJSONArray("sets");
                        JSONArray events = search.getJSONArray("upcomingEvents");
                        JSONArray tracks = search.getJSONArray("tracks");
                        JSONArray artists = search.getJSONArray("artists");

                        searchedSets = new ArrayList<Set>();
                        for(int i = 0; i < sets.length(); i++) {
                            searchedSets.add(new Set(sets.getJSONObject(i)));
                        }

                        searchedEvents = new ArrayList<Event>();
                        for(int i = 0; i < events.length(); i++) {
                            searchedEvents.add(new Event(events.getJSONObject(i)));
                        }

                        searchedTracks = new ArrayList<TrackResponse>();
                        for(int i = 0; i < tracks.length(); i++) {
                            searchedTracks.add(new TrackResponse(tracks.getJSONObject(i)));
                        }

                        searchedArtists = new ArrayList<Artist>();
                        for(int i = 0; i < artists.length(); i++) {
                            searchedArtists.add(new Artist(artists.getJSONObject(i)));
                        }

                        handlerParameter = "search";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                handler.post(updateUI);

            }
        }).start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        artists = new ArrayList<Artist>();
        festivals = new ArrayList<Event>();
        mixes = new ArrayList<Mix>();
        genres = new ArrayList<Genre>();
        popularSets = new ArrayList<Set>();
        recentSets = new ArrayList<Set>();

        if(savedInstanceState == null) {

        } else {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_search_sets, container, false);

        // Style search box

        int searchSrcTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = (EditText) rootView.findViewById(searchSrcTextId);
        searchEditText.setTextColor(Color.BLACK);
        searchEditText.setHintTextColor(Color.GRAY);

        // Listener for changed text in search bar

        ((SearchView) rootView.findViewById(R.id.search_sets)).setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                searchSets(query);
                rootView.findViewById(R.id.search_sets).clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchSets(newText);
                return false;
            }
        });

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
        artists = null;
        festivals = null;
        mixes = null;
        genres = null;
        popularSets = null;
        recentSets = null;
        searchResultSetAdapter = null;
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onStop");

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);

    }

    public void searchSets(String query) {
        this.searchQuery = query;

        // Clears the browse results list container
        rootView.findViewById(R.id.browseListContainer).setVisibility(View.GONE);

        // Clears left browse of any selections
        for(int i = 0 ; i < ((ViewGroup)rootView.findViewById(R.id.browse_container)).getChildCount(); i++) {
            ((ViewGroup)rootView.findViewById(R.id.browse_container)).getChildAt(i).setSelected(false);
        }

        // Shows the search results list container
        rootView.findViewById(R.id.setSearchResultsContainer).setVisibility(View.VISIBLE);

        // Hides the actual list and shows loader options until search has completed
        ((ListView)rootView.findViewById(R.id.setSearchResults)).setVisibility(View.GONE);
        rootView.findViewById(R.id.list_option_buttons).setVisibility(View.GONE);
        rootView.findViewById(R.id.setsLoading).setVisibility(View.VISIBLE);
        startTask();
    }

    public void startTask() {
        try {
            cancelTask();
            if (!searchQuery.equals("")) {
                ((ListView)rootView.findViewById(R.id.setSearchResults)).setVisibility(View.GONE);
                rootView.findViewById(R.id.noResults).setVisibility(View.GONE);
                rootView.findViewById(R.id.setsLoading).setVisibility(View.VISIBLE);
                getSetsTask = (SetMineApiGetRequestAsyncTask) new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), this)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                                , "search/" + Uri.encode(searchQuery), "search");
            } else {
                rootView.findViewById(R.id.setsLoading).setVisibility(View.GONE);
                rootView.findViewById(R.id.noResults).setVisibility(View.GONE);
                ((ListView)rootView.findViewById(R.id.setSearchResults)).setVisibility(View.GONE);
                rootView.findViewById(R.id.list_option_buttons).setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancelTask() {
        if (getSetsTask != null) {
            Log.d(TAG, "canceled existing task");
            getSetsTask.cancel(true);
        }
    }

    public void displaySearchResults() {
        searchResultSetAdapter = new SearchResultSetAdapter(new ArrayList<Set>(),
                new ArrayList<Event>(), new ArrayList<TrackResponse>(), new ArrayList<Artist>());
        ((ListView)rootView.findViewById(R.id.setSearchResults)).setAdapter(searchResultSetAdapter);
        searchResultSetAdapter.isRecent = false;
        searchResultSetAdapter.sets = searchedSets;
        searchResultSetAdapter.upcomingEvents = searchedEvents;
        searchResultSetAdapter.tracks = searchedTracks;
        searchResultSetAdapter.artists = searchedArtists;

        if(searchedSets.size() > 0) {
            rootView.findViewById(R.id.list_option_buttons).setVisibility(View.VISIBLE);
            ((ListView)rootView.findViewById(R.id.setSearchResults)).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.setsLoading).setVisibility(View.GONE);
            rootView.findViewById(R.id.noResults).setVisibility(View.GONE);
            searchResultSetAdapter.notifyDataSetChanged();
            ((ListView)rootView.findViewById(R.id.setSearchResults)).setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (searchResultSetAdapter.listState) {
                        case SETS:
                            Set s = searchResultSetAdapter.sets.get(position);
                            ((SetMineMainActivity)getActivity()).playerService.playerManager.setPlaylist(searchResultSetAdapter.sets);
                            ((SetMineMainActivity)getActivity()).playerService.playerManager.selectSetById(s.getId());
                            ((SetMineMainActivity)getActivity()).startPlayerFragment();
                            ((SetMineMainActivity)getActivity()).playSelectedSet();
                            break;
                        case EVENTS:
                            Event e = searchResultSetAdapter.upcomingEvents.get(position);
                            ((SetMineMainActivity) getActivity()).openEventDetailPage(e.getId(), "upcoming");
                            break;
                        case TRACKRESPONSES:
                            TrackResponse tr = searchResultSetAdapter.tracks.get(position);
                            List<Set> setsOfTracks = new ArrayList<Set>();
                            for(int i = 0; i<searchResultSetAdapter.tracks.size(); i++) {
                                setsOfTracks.add(searchResultSetAdapter.tracks.get(i).getSet());
                            }
                            ((SetMineMainActivity)getActivity()).playerService.playerManager.setPlaylist(setsOfTracks);
                            ((SetMineMainActivity)getActivity()).playerService.playerManager.selectSetById(tr.getId());
                            ((SetMineMainActivity)getActivity()).startPlayerFragment();
                            ((SetMineMainActivity)getActivity()).playSelectedSet();
                            break;
                        case ARTISTS:
                            Artist artist = searchResultSetAdapter.artists.get(position);
                            ((SetMineMainActivity) getActivity()).openArtistDetailPage(artist.getArtist());
                            break;
                    }
                }
            });
        }
        else {
            ((ListView)rootView.findViewById(R.id.setSearchResults)).setVisibility(View.GONE);
            rootView.findViewById(R.id.setsLoading).setVisibility(View.GONE);
            rootView.findViewById(R.id.noResults).setVisibility(View.VISIBLE);
        }

    }

    public void displayBrowseResults(String type) {
        if(type.equals("artists")) {
            artistAdapter = new ArtistAdapter(artists);
            ((ListView)rootView.findViewById(R.id.browseList)).setAdapter(artistAdapter);
            rootView.findViewById(R.id.browseLoading).setVisibility(View.GONE);
            rootView.findViewById(R.id.browseList).setVisibility(View.VISIBLE);
            ((ListView)rootView.findViewById(R.id.browseList)).setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    v.setPressed(true);
                    Artist a = artists.get(position);
                    ((SetMineMainActivity)getActivity()).openArtistDetailPage(a.getArtist());
                }
            });
        } else if(type.equals("festivals")) {
            eventAdapter = new EventAdapter(festivals);
            ((ListView)rootView.findViewById(R.id.browseList)).setAdapter(eventAdapter);
            rootView.findViewById(R.id.browseLoading).setVisibility(View.GONE);
            rootView.findViewById(R.id.browseList).setVisibility(View.VISIBLE);
            ((ListView)rootView.findViewById(R.id.browseList)).setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    v.setPressed(true);
                    Event e = festivals.get(position);
                    ((SetMineMainActivity)getActivity()).openEventDetailPage(e.getId(), "recent");
                }
            });
        } else if(type.equals("mixes")) {
            mixAdapter = new MixAdapter(mixes);
            ((ListView)rootView.findViewById(R.id.browseList)).setAdapter(mixAdapter);
            rootView.findViewById(R.id.browseLoading).setVisibility(View.GONE);
            rootView.findViewById(R.id.browseList).setVisibility(View.VISIBLE);
            ((ListView)rootView.findViewById(R.id.browseList)).setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    v.setPressed(true);
                    Mix m = mixes.get(position);
                    ((SearchView) rootView.findViewById(R.id.search_sets)).setQuery(m.getMix(), false);
                }
            });
        } else if(type.equals("genres")) {
            genreAdapter = new GenreAdapter(genres);
            ((ListView)rootView.findViewById(R.id.browseList)).setAdapter(genreAdapter);
            rootView.findViewById(R.id.browseLoading).setVisibility(View.GONE);
            rootView.findViewById(R.id.browseList).setVisibility(View.VISIBLE);
            ((ListView)rootView.findViewById(R.id.browseList)).setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                    v.setPressed(true);
                    Genre g = genres.get(position);
                    ((SearchView) rootView.findViewById(R.id.search_sets)).setQuery(g.getGenre(), false);
                }
            });
        } else if(type.equals("popularSets")) {
            searchResultSetAdapter = new SearchResultSetAdapter(new ArrayList<Set>(),
                    new ArrayList<Event>(), new ArrayList<TrackResponse>(), new ArrayList<Artist>());
            ((ListView)rootView.findViewById(R.id.setSearchResults)).setAdapter(searchResultSetAdapter);
            searchResultSetAdapter.isRecent = false;
            searchResultSetAdapter.sets = popularSets;
            searchResultSetAdapter.notifyDataSetChanged();
            rootView.findViewById(R.id.setsLoading).setVisibility(View.GONE);
            rootView.findViewById(R.id.setSearchResults).setVisibility(View.VISIBLE);
            ((ListView)rootView.findViewById(R.id.setSearchResults)).setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Set s = searchResultSetAdapter.sets.get(position);
                    ((SetMineMainActivity)getActivity()).playerService.playerManager.setPlaylist(searchResultSetAdapter.sets);
                    ((SetMineMainActivity)getActivity()).playerService.playerManager.selectSetById(s.getId());
                    ((SetMineMainActivity)getActivity()).startPlayerFragment();
                    ((SetMineMainActivity)getActivity()).playSelectedSet();
                }
            });

        } else if(type.equals("recentSets")) {
            searchResultSetAdapter = new SearchResultSetAdapter(new ArrayList<Set>(),
                    new ArrayList<Event>(), new ArrayList<TrackResponse>(), new ArrayList<Artist>());
            ((ListView)rootView.findViewById(R.id.setSearchResults)).setAdapter(searchResultSetAdapter);
            searchResultSetAdapter.isRecent = false;
            searchResultSetAdapter.sets = recentSets;
            searchResultSetAdapter.notifyDataSetChanged();
            rootView.findViewById(R.id.setsLoading).setVisibility(View.GONE);
            rootView.findViewById(R.id.setSearchResults).setVisibility(View.VISIBLE);
            ((ListView)rootView.findViewById(R.id.setSearchResults)).setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Set s = searchResultSetAdapter.sets.get(position);
                    ((SetMineMainActivity)getActivity()).playerService.playerManager.setPlaylist(searchResultSetAdapter.sets);
                    ((SetMineMainActivity)getActivity()).playerService.playerManager.selectSetById(s.getId());
                    ((SetMineMainActivity)getActivity()).startPlayerFragment();
                    ((SetMineMainActivity)getActivity()).playSelectedSet();
                }
            });

        }

    }

    public void setBrowseClickListeners() {
        final SearchSetsFragment thisFragment = this;
        rootView.findViewById(R.id.browse_artist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didSelectCategory(v);
                new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), thisFragment)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                                , "artist?light=true", "artists");
            }
        });
        rootView.findViewById(R.id.browse_festival).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didSelectCategory(v);
                new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), thisFragment)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                                , "festival", "festivals");

            }
        });
        rootView.findViewById(R.id.browse_mix).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didSelectCategory(v);
                new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), thisFragment)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                                , "mix", "mixes");
            }
        });
        rootView.findViewById(R.id.browse_genre).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didSelectCategory(v);
                new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), thisFragment)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                                , "genre", "genres");
            }
        });

        rootView.findViewById(R.id.browse_popular).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didSelectPopularRecent(v);
                rootView.findViewById(R.id.noResults).setVisibility(View.GONE);
                new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), thisFragment)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                                , "popular", "popularSets");

            }
        });

        rootView.findViewById(R.id.browse_recent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                didSelectPopularRecent(v);
                rootView.findViewById(R.id.noResults).setVisibility(View.GONE);
                new SetMineApiGetRequestAsyncTask((SetMineMainActivity)getActivity(), thisFragment)
                        .executeOnExecutor(SetMineApiGetRequestAsyncTask.THREAD_POOL_EXECUTOR
                                , "recent", "recentSets");

            }
        });

        rootView.findViewById(R.id.searchResultSets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(searchResultSetAdapter != null) {
                    searchResultSetAdapter.listState = ListOptions.SETS;
                    searchResultSetAdapter.notifyDataSetChanged();
                }
            }
        });

        rootView.findViewById(R.id.searchResultUpcomingEvents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchResultSetAdapter != null) {
                    searchResultSetAdapter.listState = ListOptions.EVENTS;
                    searchResultSetAdapter.notifyDataSetChanged();
                }
            }
        });

        rootView.findViewById(R.id.searchResultTracks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchResultSetAdapter != null) {
                    searchResultSetAdapter.listState = ListOptions.TRACKRESPONSES;
                    searchResultSetAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    public void didSelectCategory(View v) {
        for(int i = 0 ; i < ((ViewGroup)rootView.findViewById(R.id.browse_container)).getChildCount(); i++) {
            ((ViewGroup)rootView.findViewById(R.id.browse_container)).getChildAt(i).setSelected(false);
        }
        v.setSelected(true);
        rootView.findViewById(R.id.setSearchResultsContainer).setVisibility(View.GONE);
        rootView.findViewById(R.id.browseListContainer).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.browseList).setVisibility(View.GONE);
        rootView.findViewById(R.id.browseLoading).setVisibility(View.VISIBLE);

    }

    public void didSelectPopularRecent(View v) {
        for(int i = 0 ; i < ((ViewGroup)rootView.findViewById(R.id.browse_container)).getChildCount(); i++) {
            ((ViewGroup)rootView.findViewById(R.id.browse_container)).getChildAt(i).setSelected(false);
        }
        v.setSelected(true);
        rootView.findViewById(R.id.browseListContainer).setVisibility(View.GONE);
        rootView.findViewById(R.id.setSearchResultsContainer).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.setSearchResults).setVisibility(View.GONE);
        rootView.findViewById(R.id.setsLoading).setVisibility(View.VISIBLE);
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
        public boolean isRecent;
        public List<Set> sets;
        public List<Event> upcomingEvents;
        public List<TrackResponse> tracks;
        public List<Artist> artists;

        public ListOptions listState = ListOptions.SETS;

        SearchResultSetAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        SearchResultSetAdapter(List<Set> Sets, List<Event> Events, List<TrackResponse> TrackResponses, List<Artist> Artists) {
            this();
            sets = Sets;
            upcomingEvents = Events;
            tracks = TrackResponses;
            artists = Artists;
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
                case ARTISTS:
                    size = artists.size();
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
                    setViewHolder.setLength.setText(set.getSetLength());
                    setViewHolder.artistText.setText(set.getArtist());
                    setViewHolder.eventText.setText(set.getEvent());

                    options = new DisplayImageOptions.Builder()
                            .showImageOnLoading(R.drawable.logo_small)
                            .showImageForEmptyUri(R.drawable.logo_small)
                            .showImageOnFail(R.drawable.logo_small)
                            .cacheInMemory(false)
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
                    DateUtils du = new DateUtils();
                    eventHolder.dates.setText(du.formatDateText(event.getStartDate(), event.getEndDate()));
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
                        view = inflater.inflate(R.layout.track_tile, parent, false);
                        trackHolder = new SearchResultTrackHolder();
                        trackHolder.trackName = (TextView) view.findViewById(R.id.trackName);
                        trackHolder.startTime = (TextView) view.findViewById(R.id.startTime);
                        trackHolder.setLength = (TextView) view.findViewById(R.id.setLength);
                        trackHolder.artistText = (TextView) view.findViewById(R.id.artistText);
                        trackHolder.eventText = (TextView) view.findViewById(R.id.eventText);
                        trackHolder.artistImage = (ImageView) view.findViewById(R.id.artistImage);
                        view.setTag(trackHolder);
                        view.setId(Integer.valueOf(track.getId()).intValue());
                    }
                    trackHolder.trackName.setText(track.getTrackName());
                    trackHolder.startTime.setText(track.getStartTime());
                    trackHolder.setLength.setText(track.getSetLength());
                    trackHolder.artistText.setText(track.getArtist());
                    trackHolder.eventText.setText(track.getEvent());

                    options = new DisplayImageOptions.Builder()
                            .showImageOnLoading(R.drawable.logo_small)
                            .showImageForEmptyUri(R.drawable.logo_small)
                            .showImageOnFail(R.drawable.logo_small)
                            .cacheInMemory(false)
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
