package com.setmine.android.fragment;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.Notification;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
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
import android.widget.SeekBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.setmine.android.ImageCache;
import com.setmine.android.R;
import com.setmine.android.SetMineApplication;
import com.setmine.android.SetMineMainActivity;
import com.setmine.android.SetsManager;
import com.setmine.android.TracklistActivity;
import com.setmine.android.object.Set;
import com.setmine.android.util.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class PlayerFragment extends Fragment implements OnCompletionListener,
		SeekBar.OnSeekBarChangeListener {

	public ImageButton mButtonPlay;
	public ImageButton mButtonPlayTop;
	private ImageButton mButtonRewind;
	private ImageButton mButtonFastForward;
	private ImageButton mButtonShuffle;
	private ImageButton mButtonDownload;
	private SeekBar mProgressBar;
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
	private MediaPlayer mp;
	// Handler to update UI timer, progress bar etc,.
	private final Handler mHandler = new Handler();
	private TimeUtils utils;
	private int currentSongIndex = 0;
	private Set song;
	private boolean isShuffle = false;
	private View rootView;
	private boolean isClosed;

    private SetMineMainActivity activity;

    private SetsManager setsManager;
    private List<Set> songsList;
	private DownloadManager manager;
	private long enqueue;
	private Set downloadedSet;
	private String downloadedSetTitle;
	private Context context;
    public ImageCache imageCache;
    public ImageView externalPlayControl;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.rootView = inflater.inflate(R.layout.player, container, false);

		// All player buttons
		mButtonPlay = (ImageButton) rootView
				.findViewById(R.id.player_button_play);
		mButtonPlayTop = (ImageButton) rootView
				.findViewById(R.id.player_button_play_top);
		mButtonRewind = (ImageButton) rootView
				.findViewById(R.id.player_button_rewind);
		mButtonFastForward = (ImageButton) rootView
				.findViewById(R.id.player_button_fast_forward);
		mButtonShuffle = (ImageButton) rootView
				.findViewById(R.id.player_button_shuffle);
		mButtonDownload = (ImageButton) rootView
				.findViewById(R.id.player_button_download);
		mProgressBar = (SeekBar) rootView
				.findViewById(R.id.player_progress_bar);
		mTimeLabel = (TextView) rootView.findViewById(R.id.player_song_time);
		mDurationLabel = (TextView) rootView
				.findViewById(R.id.player_song_duration);
		mTitleLabel = (TextView) rootView.findViewById(R.id.player_song_title);
		mArtistLabel = (TextView) rootView
				.findViewById(R.id.player_song_artist);
		mTrackLabel = (TextView) rootView.findViewById(R.id.player_track_title);
		mImageView = (ImageView) rootView.findViewById(R.id.player_image);
		mImageThumb = (ImageView) rootView.findViewById(R.id.player_thumb);
		mHeader = (RelativeLayout) rootView.findViewById(R.id.player_header);
		mTracklistButton = (ImageButton) rootView
				.findViewById(R.id.player_button_tracklist);
        mBackgroundOverlay = (ImageView) rootView.findViewById(R.id.background_overlay);
//		setClosed(true);

		// Mediaplayer
		mp = new MediaPlayer();
		utils = new TimeUtils();

        activity = ((SetMineMainActivity)getActivity());

		// Listeners
		mProgressBar.setOnSeekBarChangeListener(this); // Important
		mp.setOnCompletionListener(this); // Important

		context = getActivity().getApplicationContext();
        setsManager = activity.setsManager;

		// Getting all songs list
		songsList = setsManager.getPlaylist();

        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

		// By default play first song
//		this.currentSongIndex = ((SetMineMainActivity) getActivity()).getCurrentSongIndex();

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

		setTracklistListener();
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



	public void setPlayListeners() {
		OnClickListener ocl = new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// check for already playing
				if (mp.isPlaying()) {
					if (mp != null) {
						mp.pause();
						// Changing button image to play button
						mButtonPlay.setImageResource(R.drawable.ic_action_play_white);
						mButtonPlayTop.setImageResource(R.drawable.ic_action_play_white);
                        externalPlayControl.setImageResource(R.drawable.ic_action_play);
					}
				} else {
					// Resume song
					if (mp != null) {
						mp.start();
						// Changing button image to pause button
						mButtonPlay.setImageResource(R.drawable.ic_action_pause_white);
						mButtonPlayTop.setImageResource(R.drawable.ic_action_pause_white);
                        externalPlayControl.setImageResource(R.drawable.btn_pause);
					}
				}

			}
		};
		mButtonPlay.setOnClickListener(ocl);
		mButtonPlayTop.setOnClickListener(ocl);
        if(externalPlayControl != null)
            externalPlayControl.setOnClickListener(ocl);
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
								((SetMineMainActivity) getActivity()).closePlayer();
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

	private void setTracklistListener() {
		mTracklistButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                ((SetMineMainActivity) getActivity()).playerContainerFragment.mViewPager.setCurrentItem(1);
//				((SetMineMainAct) getActivity()).sendEvent("Track List Clicked", "set", song.getArtist() + " - " + song.getEvent());
//				Intent intent = new Intent(context, TracklistActivity.class);
//				intent.putExtra("isShuffle", isShuffle);
//				intent.putExtra("position", currentSongIndex);
//				if (mp != null) {
//					intent.putExtra("time", mp.getCurrentPosition());
//				}
//				getActivity().startActivityForResult(intent, 0);
			}
		});
	}

	private void setPlayingNotification() {
		Intent intent = new Intent(getActivity(), SetMineMainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(getActivity(), 0,
				intent, 0);

		Bitmap bm = Bitmap.createScaledBitmap(
				BitmapFactory.decodeResource(getResources(), R.drawable.logo),
				getResources().getDimensionPixelSize(
						android.R.dimen.notification_large_icon_width),
				getResources().getDimensionPixelSize(
						android.R.dimen.notification_large_icon_height), true);

		// build notification
		// the addAction re-use the same intent to keep the example short
		Notification n = new NotificationCompat.Builder(getActivity())
				.setContentTitle("EDC Chicago")
				.setContentText("Hardwell")
				.setSmallIcon(R.drawable.logo)
				.setContentIntent(pIntent)
				.setLargeIcon(bm)
				.setAutoCancel(true)
				.addAction(R.drawable.ic_action_rewind_white, "", pIntent)
				.addAction(R.drawable.ic_action_play_white, "", pIntent)
				.addAction(R.drawable.ic_action_fast_forward_white, "", pIntent)
				.setOngoing(true).build();

		NotificationManager notificationManager = (NotificationManager) getActivity()
				.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.notify(0, n);

	}

	private void setShuffleListener() {
		mButtonShuffle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                isShuffle = !isShuffle;
                if (isShuffle) {
                    mButtonShuffle.setImageResource(R.drawable.btn_shuffle_on);
                    currentSongIndex = setsManager.getPlaylistShuffled().indexOf(song);
                } else {
                    mButtonShuffle.setImageResource(R.drawable.btn_shuffle);
                    currentSongIndex = setsManager.getPlaylist().indexOf(song);
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
    public void playSong(int position) {
		// Play song

		try {
			if (isShuffle) {
				song = setsManager.getPlaylistShuffled().get(
						position);
			} else {
				song = setsManager.getPlaylist()
						.get(position);
			}
//			((SetMineMainAct) getActivity()).sendEvent("Set Played", "set",
//					song.getArtist() + " - " + song.getEvent());

			mp.reset();
			mp.setDataSource(song.getSongURL());
			mp.prepare();
			mp.start();

            JSONObject mixpanelProperties = new JSONObject();
            mixpanelProperties.put("id", song.getId());
            mixpanelProperties.put("artist", song.getArtist());
            mixpanelProperties.put("event", song.getEvent());
            activity.mixpanel.track("Specific Set Played", mixpanelProperties);
            Log.v("Specific Set Played Tracked", mixpanelProperties.toString());

            // Displaying Song title
			mTitleLabel.setText(song.getEvent());
			mArtistLabel.setText(song.getArtist());
			mTrackLabel.setText(song.getCurrentTrack(0));
//			mTrackLabel.setSelected(true);

            updateTracklist(song);
			// Display song image

            ImageLoader.getInstance().displayImage(activity.S3_ROOT_URL + song.getArtistImage(), mImageThumb, options);
            ImageLoader.getInstance().displayImage(activity.S3_ROOT_URL + song.getEventImage(), mImageView, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    Bitmap blurredBitmap = ((SetMineMainActivity) getActivity()).fastblur(loadedImage, 4);
                    mBackgroundOverlay.setImageDrawable(new BitmapDrawable(activity.getResources(), blurredBitmap));
                }
            });

			// Changing Button Image to pause image
			mButtonPlay.setImageResource(R.drawable.ic_action_pause_white);
			mButtonPlayTop.setImageResource(R.drawable.ic_action_pause_white);

			// set Progress bar values
			mProgressBar.setProgress(0);
			mProgressBar.setMax(100);

			if (song.isDownloaded()) {
				mButtonDownload.setVisibility(View.GONE);
			} else {
//				mButtonDownload.setVisibility(View.VISIBLE);
			}
			// Updating progress bar
			updateProgressBar();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
            e.printStackTrace();
        }
    }

	private void playNext() {
		currentSongIndex++;
		if (currentSongIndex >= setsManager
				.getPlaylistLength()) {
			currentSongIndex = 0;
		}
		playSong(currentSongIndex);
	}

	private void playPrevious() {
		currentSongIndex--;
		if (currentSongIndex < 0) {
			currentSongIndex = ((SetMineApplication) context)
					.getPlaylistLength() - 1;
		}
		playSong(currentSongIndex);
	}

	/**
	 * Update timer on seekbar
	 * */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	/**
	 * Background Runnable thread
	 * */
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
//			mTrackLabel.setSelected(true);

			// Updating progress bar
			int progress = (utils.getProgressPercentage(time, duration));
			// Log.d("Progress", ""+progress);
			mProgressBar.setProgress(progress);

			// Running this thread after 100 milliseconds
			mHandler.postDelayed(this, 100);
		}
	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromTouch) {

	}

	/**
	 * When user starts moving the progress handler
	 * */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// remove message Handler from updating progress bar
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	/**
	 * When user stops moving the progress handler
	 * */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
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
//		((SetMineMainAct) getActivity()).sendEvent("Set Completed", "set",
//				song.getArtist() + " - " + song.getEvent());
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

    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    public boolean getIsShuffle() {
        return isShuffle;
    }
}
