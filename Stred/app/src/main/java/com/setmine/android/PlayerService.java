package com.setmine.android;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class PlayerService extends Service {
    private static final String SERVICE_COMMAND = "SERVICE_COMMAND";
    private NotificationManager m_NM;
    public MediaPlayer mMediaPlayer = new MediaPlayer();
    public IBinder playerBinder = new PlayerBinder();
    private int NOTIFICATION_ID = 1212;
    private RemoteViews mRemoteViews;
    private int REQUEST_CODE_STOP = 3434;
    private Notification mNotification;

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
//            mMediaPlayer = ... // initialize it here
            if(intent.getAction().equals("PLAY_PAUSE")) {
                playPause();
            } else if(intent.getAction().equals("NOTIFICATION_ON")) {
                showNotification(intent.getStringExtra("ARTIST"), intent.getStringExtra("EVENT"), intent.getStringExtra("IMAGE"));
            } else if(intent.getAction().equals("NOTIFICATION_OFF")) {
                pause();
                removeNotification();
            } else if(intent.getAction().equals("FAST_FORWARD")) {
                // do fast forward
            }
        }
        return START_STICKY;
    }

    private void playPause() {
        if(mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                pause();
                mRemoteViews.setImageViewResource(R.id.button_play_pause, R.drawable.ic_action_play_white);
            } else {
                startForeground(NOTIFICATION_ID, mNotification);
                mMediaPlayer.start();
                mRemoteViews.setImageViewResource(R.id.button_play_pause, R.drawable.ic_action_pause_white);
            }
        }
        m_NM.notify(NOTIFICATION_ID, mNotification);
    }

    private void pause() {
        if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            stopForeground(false);
        }
    }

    @Override
    public void onCreate() {
        m_NM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//        showNotification();
        //show notification here?
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        m_NM.cancel(NOTIFICATION_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return playerBinder;
    }

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    private void removeNotification() {
        stopForeground(true);
        m_NM.cancel(NOTIFICATION_ID);
    }

    @TargetApi(16)
    private void showNotification(String artist, String event, String image) {
        PendingIntent pendingIntent = null;
        Intent intent = null;


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

        mRemoteViews.setTextViewText(R.id.text_artist, artist);
        mRemoteViews.setTextViewText(R.id.text_event, event);

        ImageLoader.getInstance().loadImage(image, options, new SimpleImageLoadingListener() {
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
        intent = new Intent(getApplicationContext(), SetMineMainActivity.class)
                .setAction("com.setmine.android.OPEN_PLAYER")
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(getApplicationContext(),
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
                .setSmallIcon(R.drawable.logo_small).setOngoing(true)
                .setWhen(System.currentTimeMillis())
                .setContent(mRemoteViews)
                .build();

        //Show the notification in the notification bar.
        m_NM.notify(NOTIFICATION_ID, mNotification);
    }
}