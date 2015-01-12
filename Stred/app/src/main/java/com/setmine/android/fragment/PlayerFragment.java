package com.setmine.android.fragment;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat.Builder;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.setmine.android.CircularSeekBar;
import com.setmine.android.PlayerService;
import com.setmine.android.R;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.SetsManager;
import com.setmine.android.object.Set;
import com.setmine.android.task.CountPlaysTask;
import com.setmine.android.util.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PlayerFragment extends Fragment implements OnCompletionListener,
		CircularSeekBar.OnCircularSeekBarChangeListener {

	public ImageButton mButtonPlay;
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
    private ImageView mBackgroundOverlay;
    private DisplayImageOptions options;

	// Media Player
	public MediaPlayer mp;

	// Handler to update UI timer, progress bar etc,.
	private final Handler mHandler = new Handler();
	private TimeUtils utils;
	private Set song;
	private boolean isShuffle = false;
	private View rootView;
	private boolean isClosed;

    private SetMineMainActivity activity;
    public PlayerService playerService;

    public SetsManager setsManager;
	private DownloadManager manager;
	private long enqueue;
	private Set downloadedSet;
	private String downloadedSetTitle;
	private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = ((SetMineMainActivity)getActivity());
        context = getActivity().getApplicationContext();
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.rootView = inflater.inflate(R.layout.player, container, false);

		// All player buttons
		mButtonPlay = (ImageButton) rootView
				.findViewById(R.id.player_button_play);
//		mButtonPlayTop = (ImageButton) rootView
//				.findViewById(R.id.player_button_play_top);
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
		mImageThumb = (ImageView) rootView.findViewById(R.id.player_thumb);
		mHeader = (RelativeLayout) rootView.findViewById(R.id.player_header);
		mTracklistButton = (ImageButton) rootView
				.findViewById(R.id.player_button_tracklist);
        mPlaylistButton = (ImageButton)rootView.findViewById(R.id.player_button_playlist);
        mBackgroundOverlay = (ImageView) rootView.findViewById(R.id.background_overlay);
//		setClosed(true);


        utils = new TimeUtils();

        if(mp == null) {
            playerService = activity.playerService;
            mp = playerService.mMediaPlayer;
        }


		// Listeners
		mProgressBar.setOnSeekBarChangeListener(this);
        mProgressBar.setProgress(0);
        mProgressBar.setMax(100);

        mp.setOnCompletionListener(this);
        mp.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);

        setsManager = activity.setsManager;

        if(playerService != null) {
            playerService.serviceSM = setsManager;
        }

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();


//		mTrackLabel.setSelected(true);

		setPlayListeners();
//
		setPreviousListener();
//
		setNextListener();
//
		setSlideTouchGestures();
//
		setBackgroundNoTouch();

		setPagerListeners();
//
//		setShuffleListener();
//
//		setDownloadListener();
//
//		setDownloadBroadcastReceiver();
		// setPlayingNotification();

//        setRetainInstance(true);

		return this.rootView;

	}

    private void updatePlayPauseButton() {
        if(mp != null) {
            if (mp.isPlaying()) {
                // Changing button image to pause button
                mButtonPlay.setImageResource(R.drawable.ic_action_pause_white);
            } else {
                // Changing button image to play button
                mButtonPlay.setImageResource(R.drawable.ic_action_play_white);
            }
        }
    }

	public void setPlayListeners() {
		OnClickListener ocl = new OnClickListener() {

			@Override
			public void onClick(View arg0) {
            // check for already playing
            sendIntentToService("PLAY_PAUSE");

                updatePlayPauseButton();
			}
		};
		mButtonPlay.setOnClickListener(ocl);
//		mButtonPlayTop.setOnClickListener(ocl);
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

	private void setSlideTouchGestures() {
		final GestureDetector gesture = new GestureDetector(getActivity(),
				new GestureDetector.SimpleOnGestureListener() {

					@Override
					public boolean onDown(MotionEvent e) {
						return true;
					}

					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {
						final int SWIPE_MIN_DISTANCE = 120;
						final int SWIPE_MAX_OFF_PATH = 250;
						final int SWIPE_THRESHOLD_VELOCITY = 200;
						try {
							if ((e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
								// Toast.makeText(getActivity(),
								// "onfling off path UP", 500).show();
								((SetMineMainActivity) getActivity()).openPlayer();
								return false;
							} else if ((e2.getY() - e1.getY()) > SWIPE_MAX_OFF_PATH) {
								// Toast.makeText(getActivity(),
								// "onfling off path DOWN", 500).show();
//								((SetMineMainActivity) getActivity()).closePlayer();
								return false;
							}

							if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
									&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
								// Toast.makeText(getActivity(), "onfling left",
								// 500).show();
							} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
									&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
								// Toast.makeText(getActivity(),
								// "onfling right", 500).show();
							} else if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE
									&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
								// Toast.makeText(getActivity(), "onfling up",
								// 500).show();
							} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE
									&& Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
								// Toast.makeText(getActivity(), "onfling down",
								// 500).show();
							}

						} catch (Exception e) {
							// nothing
						}
						return super.onFling(e1, e2, velocityX, velocityY);
					}

					@Override
					public boolean onSingleTapUp(MotionEvent e) {
//						((SetMineMainActivity) getActivity()).togglePlayerClosed();
						return false;
					}

				});

		mHeader.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gesture.onTouchEvent(event);
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


    private void updateTracklist(Set song) {
        ((SetMineMainActivity) getActivity()).tracklistFragment.updateTracklist(song.getTracklist());
    }

	private void setPagerListeners() {
		mTracklistButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                activity.playerContainerFragment.mViewPager.setCurrentItem(2, true);
			}
		});
        mPlaylistButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.playerContainerFragment.mViewPager.setCurrentItem(0, true);
            }
        });
	}

	private void setShuffleListener() {
		mButtonShuffle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                isShuffle = !isShuffle;
                if (isShuffle) {
                    mButtonShuffle.setImageResource(R.drawable.btn_shuffle_on);
                    setsManager.selectedSetIndex = setsManager.getPlaylistShuffled().indexOf(song);
                } else {
                    mButtonShuffle.setImageResource(R.drawable.btn_shuffle);
                    setsManager.selectedSetIndex = setsManager.getPlaylist().indexOf(song);
                }
            }
        });
	}

	private void setDownloadListener() {

		mButtonDownload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] songurlparts = song.getSongURL().split("/");
				String[] imageurlparts = song.getArtistImage().split("/");

				String dir = context.getExternalFilesDir(null).toString();
				String bareImageURL = dir + "/" + imageurlparts[4];

				downloadedSet = new Set(song);
				downloadedSet.setArtistImage(bareImageURL);

				downloadedSet.setSongURL(dir + "/" + songurlparts[4]);

				// execute this when the downloader must be fired
				downloadedSetTitle = song.getArtist() + " - " + song.getEvent();
				// final DownloadSetTask downloadTask = new DownloadSetTask(
				// getActivity().getApplicationContext(), mNotifyManager,
				// mBuilder, title, bareSongURL, song2, dbh);
				// downloadTask.execute(song.getSongURL());

				String url = song.getSongURL();
				DownloadManager.Request request = new DownloadManager.Request(
						Uri.parse(url));
				request.setDescription("Adding to My Sets");
				request.setTitle(downloadedSetTitle);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					request.allowScanningByMediaScanner();
					request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
				}
				request.setDestinationInExternalFilesDir(context, null,
						songurlparts[4]);

				// get download service and enqueue file
				manager = (DownloadManager) getActivity().getSystemService(
						Context.DOWNLOAD_SERVICE);
				enqueue = manager.enqueue(request);

//				((SetMineMainAct) getActivity()).sendEvent(
//						"Set Added to My Sets", "set", downloadedSetTitle);

//				final DownloadImageTask downloadImageTask = new DownloadImageTask(
//						bareImageURL);
//				downloadImageTask.execute(song.getArtistImage());
			}
		});

	}

	private void setDownloadBroadcastReceiver() {

		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				long reference = intent.getLongExtra(
						DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)
						&& reference == enqueue) {

					Intent resultIntent = context
							.getPackageManager()
							.getLaunchIntentForPackage(context.getPackageName());
					resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_SINGLE_TOP);
					resultIntent.setAction("MY_SETS_PAGE");
					PendingIntent resultPendingIntent = PendingIntent
							.getActivity(context, 0, resultIntent,
									PendingIntent.FLAG_UPDATE_CURRENT);
					NotificationManager mNotifyManager = (NotificationManager) getActivity()
							.getSystemService(Context.NOTIFICATION_SERVICE);

					Builder mBuilder = new Builder(getActivity());
					mBuilder.setContentIntent(resultPendingIntent);

					mBuilder.setContentTitle(downloadedSetTitle)
							.setContentText("Added to My Sets")
							.setSmallIcon(R.drawable.logo).setOngoing(false);

					Resources res = context.getResources();
					Bitmap pic = BitmapFactory.decodeResource(res,
							R.drawable.logo);
					int height = (int) res
							.getDimension(android.R.dimen.notification_large_icon_height);
					int width = (int) res
							.getDimension(android.R.dimen.notification_large_icon_width);
					pic = Bitmap.createScaledBitmap(pic, width, height, false);

					mBuilder.setLargeIcon(pic);

					int mNotificationId = 001;
					mNotifyManager.notify(mNotificationId, mBuilder.build());

//					DatabaseHandler db = ((SetMineMainAct) getActivity()).db;
//					db.updateSet(db.getSet("1"), "2");
//					db.updateSet(downloadedSet, "1");
//					db.cleanupFiles();
				}
			}
		};

		getActivity().registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	/**
	 * Function to play a song
	 *
//	 * @param songIndex
	 *            - index of song
	 * */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void playSong() {
		// Play song
        song = setsManager.getSelectedSet();

        try {
            mp.reset();
            mp.setDataSource(song.getSongURL());
            mp.prepare();

            Intent notificationIntent = new Intent(getActivity(), PlayerService.class);
            notificationIntent.setAction("START_ALL");
            notificationIntent.putExtra("ARTIST", song.getArtist());
            notificationIntent.putExtra("EVENT", song.getEvent());
            notificationIntent.putExtra("ARTIST_IMAGE", activity.S3_ROOT_URL + song.getArtistImage());
            notificationIntent.putExtra("EVENT_IMAGE", activity.S3_ROOT_URL + song.getEventImage());
            sendIntentToService(notificationIntent);

            // Updating progress bar
            // WILL UPDATE VIEW LATER
            updateProgressBar();

            updateViewToNewSet();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateViewToNewSet() {
        song = setsManager.getSelectedSet();

        CountPlaysTask cpTask = new CountPlaysTask(activity.getApplicationContext());
        cpTask.execute(song.getId());

        try{
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

        ImageLoader.getInstance().loadImage(activity.S3_ROOT_URL + song.getArtistImage(), options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Bitmap roundedBitmap = ((SetMineMainActivity) getActivity()).imageUtils.getRoundedCornerBitmap(loadedImage, 2000);
                mImageView.setImageDrawable(new BitmapDrawable(activity.getResources(), roundedBitmap));

                playerService.lockscreenImage = loadedImage;

                Intent notificationIntent = new Intent(getActivity(), PlayerService.class);
                notificationIntent.setAction("UPDATE_REMOTE");
                notificationIntent.putExtra("ARTIST", song.getArtist());
                notificationIntent.putExtra("EVENT", song.getEvent());
                sendIntentToService(notificationIntent);
            }
        });
        ImageLoader.getInstance().loadImage(activity.S3_ROOT_URL + song.getEventImage(), options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Bitmap blurredBitmap = ((SetMineMainActivity) getActivity()).imageUtils.fastblur(loadedImage, 4);
                Bitmap roundedBitmap = ((SetMineMainActivity) getActivity()).imageUtils.getRoundedCornerBitmap(loadedImage, 2000);
                mBackgroundOverlay.setImageDrawable(new BitmapDrawable(activity.getResources(), blurredBitmap));

                playerService.lockscreenImage = loadedImage;

                Intent notificationIntent = new Intent(getActivity(), PlayerService.class);
                notificationIntent.setAction("UPDATE_REMOTE");
                notificationIntent.putExtra("ARTIST", song.getArtist());
                notificationIntent.putExtra("EVENT", song.getEvent());
                sendIntentToService(notificationIntent);
            }
        });

        // Displaying Song title
        mTitleLabel.setText(song.getEvent());
        mArtistLabel.setText(song.getArtist());
        mTrackLabel.setText(song.getCurrentTrack(0));
        updateTracklist(song);
        // Display song image
    }

    private void playNext() {
        setsManager.selectSetByIndex(((setsManager.selectedSetIndex + 1) >= setsManager.getPlaylistLength())? 0 : setsManager.selectedSetIndex + 1);
        playSong();
	}

	private void playPrevious() {
        setsManager.selectSetByIndex((setsManager.selectedSetIndex == 0)? setsManager.getPlaylistLength() - 1 : setsManager.selectedSetIndex - 1);
        playSong();
	}

	/**
	 * Update timer on seekbar
	 * */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}


//	Background Runnable thread
	private final Runnable mUpdateTimeTask = new Runnable() {
		@Override
		public void run() {
			long duration = mp.getDuration();
			long time = mp.getCurrentPosition();

			// Displaying Total Duration time
			mDurationLabel.setText("" + utils.milliSecondsToTimer(duration));
			// Displaying time completed playing
			mTimeLabel.setText("" + utils.milliSecondsToTimer(time));

			// set track name
			mTrackLabel.setText(song.getCurrentTrack(time));

			// Updating progress bar
			int progress = (utils.getProgressPercentage(time, duration));
			// Log.d("Progress", ""+progress);
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
	public void onProgressChanged(CircularSeekBar seekBar, int progress,
			boolean fromTouch) {

	}

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
		int totalDuration = mp.getDuration();
		int currentPosition = utils.progressToTimer(seekBar.getProgress(),
				totalDuration);

		// forward or backward to certain seconds
		mp.seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();
	}

	public void skipToTrack(int trackNo) {
		mHandler.removeCallbacks(mUpdateTimeTask);

		int currentPosition = utils.timerToMilliSeconds(song.getTracklist()
				.get(trackNo).getStartTime());

		// forward or backward to certain seconds
		mp.seekTo(currentPosition);

		updateProgressBar();
	}

	/**
	 * On Song Playing completed if repeat is ON play same song again if shuffle
	 * is ON play random song
	 * */
	@Override
	public void onCompletion(MediaPlayer arg0) {
		playNext();
	}

    @Override
    public void onPause() {
        super.onPause();
        ((SetMineMainActivity)getActivity()).playerFragment = this;
    }

    @Override
	public void onDestroy() {
		super.onDestroy();
		mp.release();
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		if (mButtonPlayTop != null) {
			if (isClosed) {
				this.mButtonPlayTop.setVisibility(View.VISIBLE);
				this.mPlaylistButton.setVisibility(View.GONE);
			} else {
				this.mButtonPlayTop.setVisibility(View.GONE);
				this.mPlaylistButton.setVisibility(View.VISIBLE);
			}
		}
		this.isClosed = isClosed;
	}

	public void shuffle(boolean shuffle) {
		isShuffle = shuffle;
		if (isShuffle) {
			mButtonShuffle.setImageResource(R.drawable.btn_shuffle_on);
		} else {
			mButtonShuffle.setImageResource(R.drawable.btn_shuffle);
		}
	}

    private int dpToPx(int dp)
    {
        float density = getActivity().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }

    private void sendIntentToService(String intentAction) {
        Intent playIntent = new Intent(getActivity(), PlayerService.class);
        playIntent.setAction(intentAction);
        playIntent.putExtra(intentAction, true);
        sendIntentToService(playIntent);
    }

    private void sendIntentToService(Intent intent) {
        getActivity().startService(intent);
    }

    public int getCurrentSongIndex() {
        return setsManager.selectedSetIndex;
    }

    public boolean getIsShuffle() {
        return isShuffle;
    }

}
