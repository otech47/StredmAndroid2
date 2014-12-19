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
import android.view.View;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.setmine.android.object.Set;
import com.setmine.android.task.CountPlaysTask;

import java.io.IOException;

public class PlayerService extends Service implements AudioManager.OnAudioFocusChangeListener {
    private static final String SERVICE_COMMAND = "SERVICE_COMMAND";
    private NotificationManager m_NM;
    public MediaPlayer mMediaPlayer = new MediaPlayer();
    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;
    public IBinder playerBinder = new PlayerBinder();
    private int NOTIFICATION_ID = 1212;
    private RemoteViews mRemoteViews;
    private int REQUEST_CODE_STOP = 3434;
    private Notification mNotification;
    public AudioManager am;
    private ComponentName mReceiver = new ComponentName(RemoteControlReceiver.class.getPackage().getName(), RemoteControlReceiver.class.getName());
    private float oldVolume = 0.0f;
    private float LOW_VOLUME = 0.2f;
    public RemoteControlClient remoteControlClient;
    public Bitmap lockscreenImage;
    public SetsManager serviceSM;
    public boolean newSong = false;

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
//            mMediaPlayer = ... // initialize it here
            if(intent.getAction().equals("PLAY_PAUSE")) {
                playPause();
            } else if(intent.getAction().equals("START_ALL")) {
                remoteControl(intent.getStringExtra("ARTIST"), intent.getStringExtra("EVENT"));
                showNotification();
                play();
            } else if(intent.getAction().equals("UPDATE_REMOTE")) {
                remoteControl(intent.getStringExtra("ARTIST"), intent.getStringExtra("EVENT"));
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
        if(mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                pause();
            } else {
                play();
            }
        }
        m_NM.notify(NOTIFICATION_ID, mNotification);
    }

    @TargetApi(5)
    private void play() {
        if(mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            int result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                am.registerMediaButtonEventReceiver(mReceiver);
                mMediaPlayer.start();
                acquireLocks();
                mRemoteViews.setImageViewResource(R.id.button_play_pause, R.drawable.ic_action_pause_white);
                m_NM.notify(NOTIFICATION_ID, mNotification);
                remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                startForeground(NOTIFICATION_ID, mNotification);
            }
        }
    }

    @TargetApi(5)
    private void pause() {
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            wifiLock.release();
            mRemoteViews.setImageViewResource(R.id.button_play_pause, R.drawable.ic_action_play_white);
            releaseLocks();
            m_NM.notify(NOTIFICATION_ID, mNotification);
            remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
            stopForeground(false);
        }
    }

    private void playNext() {
        serviceSM.selectSetByIndex((serviceSM.selectedSetIndex >= serviceSM.getPlaylistLength())? 0 : serviceSM.selectedSetIndex + 1);
        playSong();
    }

    private void playPrevious() {
        serviceSM.selectSetByIndex((serviceSM.selectedSetIndex < 0)? serviceSM.getPlaylistLength() - 1 : serviceSM.selectedSetIndex - 1);
        playSong();
    }

    private void playSong() {
        try {
            Set song = serviceSM.getSelectedSet();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(song.getSongURL());
            //            mp.setOnPreparedListener(this);
            mMediaPlayer.prepare();
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
        mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
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
        m_NM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        acquireLocks();
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
//        showNotification();
        //show notification here?
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        m_NM.cancel(NOTIFICATION_ID);
        am.unregisterMediaButtonEventReceiver(mReceiver);
        releaseLocks();
    }

    @Override
    public IBinder onBind(Intent intent) {
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
        mMediaPlayer.setVolume(LOW_VOLUME, LOW_VOLUME);
    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
        // Resume playback
        if(oldVolume != 0.0f) {
            mMediaPlayer.setVolume(oldVolume, oldVolume);
        }
        play();
    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
        am.unregisterMediaButtonEventReceiver(mReceiver);
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
        m_NM.cancel(NOTIFICATION_ID);
    }

    private void remoteControl(String artist, String event) {
        if(remoteControlClient == null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.setComponent(mReceiver);
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
        Set song = serviceSM.getSelectedSet();

        //Inflate a remote view with a layout which you want to display in the notification bar.
        if (mRemoteViews == null) {
            mRemoteViews = new RemoteViews(getPackageName(),
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

        mRemoteViews.setTextViewText(R.id.text_artist, song.getArtist());
        mRemoteViews.setTextViewText(R.id.text_event, song.getEvent());

        ImageLoader.getInstance().loadImage(SetMineMainActivity.S3_ROOT_URL + song.getArtistImage(), options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mRemoteViews.setImageViewBitmap(R.id.notification_image, loadedImage);
                m_NM.notify(NOTIFICATION_ID, mNotification);
            }
        });

        // Play/pause intent
        intent = new Intent(getApplicationContext(), PlayerService.class)
                .setAction("PLAY_PAUSE");
        pendingIntent = PendingIntent.getService(getApplicationContext(),
                REQUEST_CODE_STOP, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mRemoteViews.setOnClickPendingIntent(R.id.button_play_pause,
                pendingIntent);

        // Open player intent
        intent = new Intent(this, SetMineMainActivity.class)
                .setAction("com.setmine.android.OPEN_PLAYER")
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this,
                REQUEST_CODE_STOP, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mRemoteViews.setOnClickPendingIntent(R.id.notification_background, pendingIntent);

        // Remove notification intent
        intent = new Intent(getApplicationContext(), PlayerService.class)
                .setAction("NOTIFICATION_OFF");
        pendingIntent = PendingIntent.getService(getApplicationContext(),
                REQUEST_CODE_STOP, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mRemoteViews.setOnClickPendingIntent(R.id.button_close,
                pendingIntent);

        //Create the notification instance.
        mNotification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.logo_small_white).setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setContent(mRemoteViews)
                .build();

        //Show the notification in the notification bar.
        m_NM.notify(NOTIFICATION_ID, mNotification);
    }
}