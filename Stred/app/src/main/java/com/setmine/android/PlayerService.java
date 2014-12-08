package com.setmine.android;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.google.android.gms.common.api.Api;
import com.setmine.android.R;
import com.setmine.android.fragment.PlayerFragment;

import java.io.IOException;

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
                showNotification(intent.getStringExtra("ARTIST"), intent.getStringExtra("EVENT"));
            } else if(intent.getAction().equals("FAST_FORWARD")) {
                // do fast forward
            }
        }
        return START_STICKY;
    }

    private void playPause() {
        if(mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mRemoteViews.setImageViewResource(R.id.button_play_pause, R.drawable.ic_action_play_white);
            } else {
                mMediaPlayer.start();
                mRemoteViews.setImageViewResource(R.id.button_play_pause, R.drawable.ic_action_pause_white);
            }
        }
        m_NM.notify(NOTIFICATION_ID, mNotification);
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

    @TargetApi(16)
    private void showNotification(String artist, String event) {
        PendingIntent pendingIntent = null;
        Intent intent = null;


        //Inflate a remote view with a layout which you want to display in the notification bar.
        if (mRemoteViews == null) {
            mRemoteViews = new RemoteViews(getPackageName(),
                    R.layout.notification_control_bar);
        }

        intent = new Intent(getApplicationContext(), PlayerService.class).setAction("PLAY_PAUSE");
        pendingIntent = PendingIntent.getService(getApplicationContext(),
                REQUEST_CODE_STOP, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mRemoteViews.setOnClickPendingIntent(R.id.button_play_pause,
                pendingIntent);

        mRemoteViews.setTextViewText(R.id.text_artist, artist);
        mRemoteViews.setTextViewText(R.id.text_event, event);

//        intent = new Intent(getApplicationContext(), SetMineMainActivity.class).setAction("com.stredm.android.OPEN_PLAYER");
//        pendingIntent = PendingIntent.getActivity(getApplicationContext(),
//                REQUEST_CODE_STOP, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        mRemoteViews.setOnClickPendingIntent(R.id.notification_background, pendingIntent);
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