package com.setmine.android.player;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.api.SetMineApiPostRequestAsyncTask;
import com.setmine.android.external.CircularSeekBar;
import com.setmine.android.image.ImageUtils;
import com.setmine.android.interfaces.ApiCaller;
import com.setmine.android.set.Set;
import com.setmine.android.user.User;
import com.setmine.android.util.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class PlayerFragment extends Fragment implements
		CircularSeekBar.OnCircularSeekBarChangeListener, ApiCaller {

    private final String TAG = "PlayerFragment";

    private View rootView;
	public ImageButton mButtonPlay;
    public ImageButton mButtonShare;
	public ImageButton mButtonPlayTop;
	private ImageButton mButtonRewind;
	private ImageButton mButtonFastForward;
	private ImageButton mButtonShuffle;
	private ImageButton mButtonDownload;
	private CircularSeekBar mProgressBar;
	private TextView mTitleLabel;
	private TextView mArtistLabel;
	private TextView mTimeLabel;
	private TextView mDurationLabel;
	private TextView mTrackLabel;
	private ImageView mImageView;
	private ImageView mImageThumb;
	private RelativeLayout mHeader;
	private ImageButton mPlaylistButton;
	private ImageButton mTracklistButton;
    private ImageView mBackgroundImage;
    private View playerLoader;
    private ShareActionProvider mShareActionProvider;

    public ImageView favoriteSetButton;

    private DisplayImageOptions options;

	// Handler to update UI timer, progress bar etc,.
	private final Handler mHandler = new Handler();
	private TimeUtils utils;
	private Set song;

    private SetMineMainActivity activity;
    public PlayerService playerService;

    public PlayerManager playerManager;
	private DownloadManager manager;
	private long enqueue;
	private Set downloadedSet;
	private String downloadedSetTitle;
	private Context context;

    private User user;

    private final Runnable updateFavoriteSets = new Runnable() {
        @Override
        public void run() {
            updateFavoriteSetView();
        }
    };

    private final Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            long duration = playerService.mediaPlayer.getDuration();
            long time = playerService.mediaPlayer.getCurrentPosition();

            // Displaying Total Duration time
            mDurationLabel.setText("" + utils.milliSecondsToTimer(duration));
            // Displaying time completed playing
            mTimeLabel.setText("" + utils.milliSecondsToTimer(time));

            // set track name
            mTrackLabel.setText(song.getCurrentTrack(time));

            // Updating progress bar
            int progress = (utils.getProgressPercentage(time, duration));
            mProgressBar.setProgress(progress);

            updatePlayPauseButton();

            if(playerService.newSong) {
                updateViewToNewSet();
                playerService.newSong = false;
            }

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };


    @Override
    public void onApiResponseReceived(JSONObject jsonObject, String identifier) {
        final JSONObject finalJsonObject = jsonObject;
        final String finalIdentifier = identifier;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onApiResponseReceived: ");
                if(finalIdentifier.equals("updateUserSets")) {
                    Log.d(TAG, "favorite sets updated");
                    Log.d(TAG, finalJsonObject.toString());
                    try {
                        JSONObject payload = finalJsonObject.getJSONObject("payload");
                        JSONObject userJSON = payload.getJSONObject("user");
                        user = new User(userJSON);
                    } catch(JSONException e) {
                        e.printStackTrace();
                    }

                    mHandler.post(updateFavoriteSets);
                }
            }
        }).start();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (SetMineMainActivity)activity;
        this.playerService = this.activity.playerService;
        this.playerManager = playerService.playerManager;
        ((PlayerContainerFragment)getParentFragment()).playerFragment = this;
        user = this.activity.user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if(savedInstanceState != null) {
            String jsonUser = savedInstanceState.getString("user");
            try {
                user = new User(new JSONObject(jsonUser));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {

        }



    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");

        this.rootView = inflater.inflate(R.layout.player, container, false);

		// All player buttons

        mButtonShare = (ImageButton) rootView
                .findViewById(R.id.player_button_share);
		mButtonPlay = (ImageButton) rootView
				.findViewById(R.id.player_button_play);
		mButtonRewind = (ImageButton) rootView
				.findViewById(R.id.player_button_rewind);
		mButtonFastForward = (ImageButton) rootView
				.findViewById(R.id.player_button_fast_forward);
		mProgressBar = (CircularSeekBar) rootView
				.findViewById(R.id.circular_seek_bar);
		mTimeLabel = (TextView) rootView.findViewById(R.id.player_song_time);
		mDurationLabel = (TextView) rootView
				.findViewById(R.id.player_song_duration);
		mTitleLabel = (TextView) rootView.findViewById(R.id.player_event_name);
		mArtistLabel = (TextView) rootView
				.findViewById(R.id.player_artist_name);
		mTrackLabel = (TextView) rootView.findViewById(R.id.player_track_title);
		mImageView = (ImageView) rootView.findViewById(R.id.player_image);
		mHeader = (RelativeLayout) rootView.findViewById(R.id.player_header);
		mTracklistButton = (ImageButton) rootView
				.findViewById(R.id.player_button_tracklist);
        mBackgroundImage = (ImageView) rootView.findViewById(R.id.background_image);
        favoriteSetButton = (ImageView) rootView.findViewById(R.id.favorite_set_icon);
        playerLoader = rootView.findViewById(R.id.centered_loader_container);
        playerLoader.setVisibility(View.VISIBLE);


        utils = new TimeUtils();

		// Listeners
		mProgressBar.setOnSeekBarChangeListener(this);
        mProgressBar.setProgress(0);
        mProgressBar.setMax(100);

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();


		setPlayListeners();
//
		setPreviousListener();
//
		setNextListener();

        setShareListeners();
//
//		setSlideTouchGestures();
//
		setBackgroundNoTouch();

		setPagerListeners();

        updateViewToNewSet();

        updateProgressBar();

        // Set Click Listener for Favorite Set Button

//
//		setShuffleListener();
//
//		setDownloadListener();
//
//		setDownloadBroadcastReceiver();
		// setPlayingNotification();



		return this.rootView;

	}

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("user", user.jsonModelString);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateTimeTask);
    }


	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	@Override
	public void onProgressChanged(CircularSeekBar seekBar, int progress,
			boolean fromTouch) {}

	/**
	 * When user starts moving the progress handler
	 * */
	@Override
	public void onStartTrackingTouch(CircularSeekBar seekBar) {
		// remove message Handler from updating progress bar
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	/**
	 * When user stops moving the progress handler
	 * */
	@Override
	public void onStopTrackingTouch(CircularSeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = playerService.mediaPlayer.getDuration();
		int currentPosition = utils.progressToTimer(seekBar.getProgress(),
				totalDuration);

		// forward or backward to certain seconds
		playerService.mediaPlayer.seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();
	}

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)

    private void playPrevious() {

        playerManager.selectSetByIndex((playerManager.selectedSetIndex == 0) ?
                playerManager.getPlaylistLength() - 1 :
                playerManager.selectedSetIndex - 1);
        updateViewToNewSet();
        updateTracklist();
        sendIntentToService("START_ALL");


    }

    private void playNext() {

        playerManager.selectSetByIndex(
                ((playerManager.selectedSetIndex + 1) >= playerManager.getPlaylistLength()) ? 0
                        : playerManager.selectedSetIndex + 1);
        updateViewToNewSet();
        updateTracklist();
        sendIntentToService("START_ALL");

    }

    private void sendIntentToService(String intentAction) {
        Intent playIntent = new Intent(getActivity(), PlayerService.class);
        playIntent.setAction(intentAction);
        playIntent.putExtra(intentAction, true);
        sendIntentToService(playIntent);
    }

    private void sendIntentToService(Intent intent) {
        ((SetMineMainActivity)getActivity()).sendIntentToService(intent);
    }

    private void setPagerListeners() {
        mTracklistButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ((PlayerContainerFragment) getParentFragment()).mViewPager.setCurrentItem(2, true);
            }
        });
    }

    private void setShareListeners(){
        mButtonShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "http://setmine.com/?play/"
                        + playerManager.getSelectedSet().getId());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "http://setmine.com/?play/"+playerManager.getSelectedSet().getId()));

            }
        });
    }

    public void setPlayListeners() {
        OnClickListener ocl = new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                sendIntentToService("PLAY_PAUSE");
                updatePlayPauseButton();
            }
        };
        mButtonPlay.setOnClickListener(ocl);
    }

    private void setPreviousListener() {
        mButtonRewind.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                playPrevious();
            }
        });
    }

    private void setNextListener() {
        mButtonFastForward.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                playNext();
            }
        });
    }

    private void setBackgroundNoTouch() {
        final GestureDetector gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                });
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
            }
        });
    }

    public void skipToTrack(int trackNo) {
        mHandler.removeCallbacks(mUpdateTimeTask);

        int currentPosition = utils.timerToMilliSeconds(song.getTracklist()
                .get(trackNo).getStartTime());

        // forward or backward to certain seconds
        playerService.mediaPlayer.seekTo(currentPosition);

        updateProgressBar();
    }

    public void updateFavoriteSetView() {

        // If set is already favorited change the Favorite Set icon

        final ApiCaller superThis = this;


        if (user.isSetFavorited(song)) {
            favoriteSetButton.setImageResource(R.drawable.unfavorite_button_white);
        } else {
            favoriteSetButton.setImageResource(R.drawable.favorite_button_white);
        }


        favoriteSetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user.isRegistered()) {
                    if (user.isSetFavorited(song)) {
                        Toast.makeText(activity.getApplicationContext(),
                                "Removed from My Sets", Toast.LENGTH_SHORT).show();
                        favoriteSetButton.setImageResource(R.drawable.favorite_button_white);
                    } else {
                        Toast.makeText(activity.getApplicationContext(),
                                "Added to My Sets", Toast.LENGTH_SHORT).show();
                        favoriteSetButton.setImageResource(R.drawable.unfavorite_button_white);
                    }
                    try {
                        JSONObject jsonUserData = new JSONObject();
                        JSONObject jsonPostData = new JSONObject();
                        jsonUserData.put("userID", Integer.parseInt(user.getId()));
                        jsonUserData.put("setId", Integer.parseInt(song.getId()));
                        jsonPostData.put("userData", jsonUserData);
                        Log.d(TAG, jsonPostData.toString());

                        SetMineApiPostRequestAsyncTask updateFavoriteSetsTask =
                                new SetMineApiPostRequestAsyncTask(activity, superThis);
                        updateFavoriteSetsTask
                                .executeOnExecutor(SetMineApiPostRequestAsyncTask.THREAD_POOL_EXECUTOR,
                                        "user/updateFavoriteSets", jsonPostData.toString(), "updateUserSets");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    activity.openMainViewPager(0);
                }
            }
        });


    }

    public void updateViewToNewSet() {
        Log.d(TAG, "updateViewToNewSet");

        playerLoader.setVisibility(View.VISIBLE);

        song = playerManager.getSelectedSet();


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject mixpanelProperties = new JSONObject();
                    mixpanelProperties.put("id", song.getId());
                    mixpanelProperties.put("artist", song.getArtist());
                    mixpanelProperties.put("event", song.getEvent());
                    activity.mixpanel.track("Specific Set Played", mixpanelProperties);
                    activity.mixpanel.getPeople().increment("play_count", 1);
                    activity.mixpanel.getPeople().append("sets_played_ids", song.getId());
                    if(song.getEpisode().length() > 0) {
                        activity.mixpanel.getPeople().append("sets_played_names", song.getArtist()+" - "+song.getEvent()+" - "+song.getEpisode());
                    } else {
                        activity.mixpanel.getPeople().append("sets_played_names", song.getArtist()+" - "+song.getEvent());
                    }
                    activity.mixpanel.getPeople().append("sets_played_names", song.getId());
                    activity.mixpanel.getPeople().append("sets_played_artists", song.getArtist());
                    activity.mixpanel.getPeople().append("sets_played_events", song.getEvent());
                    activity.mixpanel.getPeople().append("sets_played_genres", song.getGenre());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();



        ImageLoader.getInstance().loadImage(song.getArtistImage(), options, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
                playerLoader.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                ImageUtils imageUtils = new ImageUtils();
                Bitmap roundedBitmap = imageUtils.getRoundedCornerBitmap(loadedImage, 5000);
                if(mBackgroundImage != null) {
                    mImageView.setImageDrawable(new BitmapDrawable(getActivity().getResources(), roundedBitmap));
                }

                Intent notificationIntent = new Intent(getActivity(), PlayerService.class);
                notificationIntent.setAction("UPDATE_REMOTE");
                notificationIntent.putExtra("ARTIST", song.getArtist());
                notificationIntent.putExtra("EVENT", song.getEvent());
                sendIntentToService(notificationIntent);
                playerService.artistImage = loadedImage;

                playerLoader.setVisibility(View.GONE);

            }
        });
        ImageLoader.getInstance().loadImage(song.getEventImage(), options, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                ImageUtils imageUtils = new ImageUtils();
                Bitmap blurredBitmap = imageUtils.fastblur(loadedImage, 4);
                if(mBackgroundImage != null) {
                    mBackgroundImage.setImageDrawable(new BitmapDrawable(getActivity().getResources(), blurredBitmap));
                }

                playerService.lockscreenImage = loadedImage;

                Intent notificationIntent = new Intent(getActivity(), PlayerService.class);
                notificationIntent.setAction("UPDATE_REMOTE");
                notificationIntent.putExtra("ARTIST", song.getArtist());
                notificationIntent.putExtra("EVENT", song.getEvent());
                sendIntentToService(notificationIntent);

            }
        });

        updateFavoriteSetView();


        // Displaying Song title
        mTitleLabel.setText(song.getEvent());
        mArtistLabel.setText(song.getArtist());
        mTrackLabel.setText(song.getCurrentTrack(0));
//        ((PlayerContainerFragment)getParentFragment()).mPlayerPagerAdapter
//                .playListFragment.updatePlaylist();



    }

    private void updatePlayPauseButton() {
        if(playerService.mediaPlayer != null) {
            if (playerService.mediaPlayer.isPlaying()) {
                mButtonPlay.setImageResource(R.drawable.ic_action_pause_white);
            } else {
                mButtonPlay.setImageResource(R.drawable.ic_action_play_white);
            }
        }
    }

    private void updateTracklist() {
        ((PlayerContainerFragment) getParentFragment()).tracklistFragment.updateTracklist();
    }

//    private void setSlideTouchGestures() {
//        final GestureDetector gesture = new GestureDetector(getActivity(),
//                new GestureDetector.SimpleOnGestureListener() {
//
//                    @Override
//                    public boolean onDown(MotionEvent e) {
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onFling(MotionEvent e1, MotionEvent e2,
//                                           float velocityX, float velocityY) {
//                        final int SWIPE_MIN_DISTANCE = 120;
//                        final int SWIPE_MAX_OFF_PATH = 250;
//                        final int SWIPE_THRESHOLD_VELOCITY = 200;
//                        try {
//                            if ((e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
//                                // Toast.makeText(getActivity(),
//                                // "onfling off path UP", 500).show();
//                                ((SetMineMainActivity) getActivity()).openPlayer();
//                                return false;
//                            } else if ((e2.getY() - e1.getY()) > SWIPE_MAX_OFF_PATH) {
//                                // Toast.makeText(getActivity(),
//                                // "onfling off path DOWN", 500).show();
////								((SetMineMainActivity) getActivity()).closePlayer();
//                                return false;
//                            }
//
//                            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
//                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                                // Toast.makeText(getActivity(), "onfling left",
//                                // 500).show();
//                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
//                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                                // Toast.makeText(getActivity(),
//                                // "onfling right", 500).show();
//                            } else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
//                                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                                // Toast.makeText(getActivity(), "onfling up",
//                                // 500).show();
//                            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
//                                    && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
//                                // Toast.makeText(getActivity(), "onfling down",
//                                // 500).show();
//                            }
//
//                        } catch (Exception e) {
//                            // nothing
//                        }
//                        return super.onFling(e1, e2, velocityX, velocityY);
//                    }
//
//                    @Override
//                    public boolean onSingleTapUp(MotionEvent e) {
////						((SetMineMainActivity) getActivity()).togglePlayerClosed();
//                        return false;
//                    }
//
//                });
//
//        mHeader.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return gesture.onTouchEvent(event);
//            }
//        });
//    }

    //    private void setShuffleListener() {
//        mButtonShuffle.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                isShuffle = !isShuffle;
//                if (isShuffle) {
//                    mButtonShuffle.setImageResource(R.drawable.btn_shuffle_on);
//                    playerManager.selectedSetIndex = playerManager.getPlaylistShuffled().indexOf(song);
//                } else {
//                    mButtonShuffle.setImageResource(R.drawable.btn_shuffle);
//                    playerManager.selectedSetIndex = playerManager.getPlaylist().indexOf(song);
//                }
//            }
//        });
//    }

}
