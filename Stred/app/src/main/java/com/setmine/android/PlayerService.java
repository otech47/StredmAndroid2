package com.setmine.android;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.setmine.android.fragment.PlayerFragment;
import com.setmine.android.object.Constants;
import com.setmine.android.object.Set;
import com.setmine.android.task.CountPlaysTask;

import java.io.IOException;

public class PlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private final String TAG = "PlayerService";
    private static final String SERVICE_COMMAND = "SERVICE_COMMAND";
    private NotificationManager notificationManager;
    public MediaPlayer mediaPlayer;
    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;
    public IBinder playerBinder = new PlayerBinder();
    private int NOTIFICATION_ID = 1212;
    private RemoteViews remoteViews;
    private int REQUEST_CODE_STOP = 3434;
    private Notification notification;
    public AudioManager am;
    private ComponentName receiver = new ComponentName(RemoteControlReceiver.class.getPackage().getName(), RemoteControlReceiver.class.getName());
    private float oldVolume = 0.0f;
    private float LOW_VOLUME = 0.2f;
    public RemoteControlClient remoteControlClient;
    public Bitmap lockscreenImage;
    public PlayerManager playerManager;
    public PlayerFragment playerFragment;
    public boolean newSong = false;

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if(mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            acquireLocks();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playNext();
                }
            });
        }
        if (intent != null && intent.getAction() != null) {
            if(intent.getAction().equals("PLAY_PAUSE")) {
                playPause();
            } else if(intent.getAction().equals("START_ALL")) {
                String artist = playerManager.getSelectedSet().getArtist();
                String event = playerManager.getSelectedSet().getEvent();
                remoteControl(artist, event);
                showNotification();
                playSong();
            } else if(intent.getAction().equals("UPDATE_REMOTE")) {
                String artist = playerManager.getSelectedSet().getArtist();
                String event = playerManager.getSelectedSet().getEvent();
                remoteControl(artist, event);
            } else if(intent.getAction().equals("NOTIFICATION_OFF")) {
                pause();
                removeNotification();
            } else if(intent.getAction().equals("NEXT")) {
                playNext();
                showNotification();
            } else if(intent.getAction().equals("PREVIOUS")) {
                playPrevious();
                showNotification();
            }
        }
        return START_STICKY;
    }

    @TargetApi(5)
    private void playPause() {
        if(mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                pause();
            } else {
                play();
            }
        }
        try {
            notificationManager.notify(NOTIFICATION_ID, notification);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(5)
    private void play() {
        if(mediaPlayer != null && !mediaPlayer.isPlaying() && am != null && notificationManager != null) {
            int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                try {
                    am.registerMediaButtonEventReceiver(receiver);
                    mediaPlayer.start();
                    acquireLocks();
                    if(remoteViews != null) {
                        remoteViews.setImageViewResource(R.id.button_play_pause, R.drawable.ic_action_pause_white);
                    }
                    notificationManager.notify(NOTIFICATION_ID, notification);
                    remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                    startForeground(NOTIFICATION_ID, notification);
                } catch(Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @TargetApi(5)
    private void pause() {
        if(mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.pause();
                wifiLock.release();
                am.abandonAudioFocus(this);
                if(remoteViews != null) {
                    remoteViews.setImageViewResource(R.id.button_play_pause, R.drawable.ic_action_play_white);
                }
                releaseLocks();
                notificationManager.notify(NOTIFICATION_ID, notification);
                remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
                stopForeground(false);
            } catch(Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void playNext() {
        playerManager.selectSetByIndex(
                ((playerManager.selectedSetIndex + 1) >= playerManager.getPlaylistLength()) ? 0
                        : playerManager.selectedSetIndex + 1);
        playSong();
    }

    private void playPrevious() {
        playerManager.selectSetByIndex((playerManager.selectedSetIndex == 0)?
                playerManager.getPlaylistLength() - 1 :
                playerManager.selectedSetIndex - 1);
        playSong();
    }

    private void playSong() {
        try {
            Set song = playerManager.getSelectedSet();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getSongURL());
            mediaPlayer.prepare();
            CountPlaysTask cpTask = new CountPlaysTask(this);
            cpTask.execute(song.getId());

            this.newSong = true;
            play();
        } catch (IOException ioe) {

        }
    }

    private void acquireLocks() {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "setmine_wakelock");
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        if(!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
        WifiManager wm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "setmine_wifilock");
        if(!wifiLock.isHeld()) {
            wifiLock.acquire();
        }
    }

    private void releaseLocks() {
        if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
        }
        if (wifiLock != null && wifiLock.isHeld()) {
                wifiLock.release();
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        playerManager = new PlayerManager();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        super.onRebind(intent);
        if(am == null || notificationManager == null) {
            notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        // Cancel the persistent notification.
        notificationManager.cancel(NOTIFICATION_ID);
        am.unregisterMediaButtonEventReceiver(receiver);
        releaseLocks();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");

        return playerBinder;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ) {
        // Pause playback
        pause();
    } else if(focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
        // lower volume
        oldVolume = (float)am.getStreamVolume(AudioManager.STREAM_MUSIC)/am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mediaPlayer.setVolume(LOW_VOLUME, LOW_VOLUME);
    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
        // Resume playback
        if(oldVolume != 0.0f) {
            mediaPlayer.setVolume(oldVolume, oldVolume);
        }
        play();
    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
        am.unregisterMediaButtonEventReceiver(receiver);
        am.abandonAudioFocus(this);
        pause();
//        wifiLock.release();
        // Stop playback
    }
    }

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    @TargetApi(5)
    private void removeNotification() {
        stopForeground(true);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void remoteControl(String artist, String event) {
        if(remoteControlClient == null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.setComponent(receiver);
            // create and register the remote control client
            PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            remoteControlClient = new RemoteControlClient(mediaPendingIntent);
            remoteControlClient.setTransportControlFlags(RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                            | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
                            | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                            | RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                            | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
            );
            am.registerRemoteControlClient(remoteControlClient);
        }
        if(artist != null && event != null && lockscreenImage != null) {
            remoteControlClient.editMetadata(true)
                    .putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, lockscreenImage)
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, event)
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, artist)
                    .apply();
            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
            am.registerRemoteControlClient(remoteControlClient);
        }
    }

    @TargetApi(16)
    private void showNotification() {

        // reusable variables
        PendingIntent pendingIntent = null;
        Intent intent = null;
        Set song = playerManager.getSelectedSet();

        //Inflate a remote view with a layout which you want to display in the notification bar.
        if (remoteViews == null) {
            remoteViews = new RemoteViews(getPackageName(),
                    R.layout.notification_control_bar);
        }

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.logo_small)
                .showImageForEmptyUri(R.drawable.logo_small)
                .showImageOnFail(R.drawable.logo_small)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .build();

        remoteViews.setTextViewText(R.id.text_artist, song.getArtist());
        remoteViews.setTextViewText(R.id.text_event, song.getEvent());



        // Play/pause intent
        intent = new Intent(getApplicationContext(), PlayerService.class)
                .setAction("PLAY_PAUSE");
        pendingIntent = PendingIntent.getService(getApplicationContext(),
                REQUEST_CODE_STOP, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.button_play_pause,
                pendingIntent);

        // Open player intent
        intent = new Intent(this, SetMineMainActivity.class)
                .setAction("com.setmine.android.OPEN_PLAYER")
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this,
                REQUEST_CODE_STOP, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.notification_background, pendingIntent);

        // Remove notification intent
        intent = new Intent(getApplicationContext(), PlayerService.class)
                .setAction("NOTIFICATION_OFF");
        pendingIntent = PendingIntent.getService(getApplicationContext(),
                REQUEST_CODE_STOP, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.button_close,
                pendingIntent);

        //Create the notification instance.
        notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.logo_small_white).setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setContent(remoteViews)
                .build();

        ImageLoader.getInstance().loadImage(Constants.S3_ROOT_URL + song.getArtistImage(), options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                remoteViews.setImageViewBitmap(R.id.notification_image, loadedImage);
                try {
                    notificationManager.notify(NOTIFICATION_ID, notification);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Show the notification in the notification bar.

        try {
            notificationManager.notify(NOTIFICATION_ID, notification);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}