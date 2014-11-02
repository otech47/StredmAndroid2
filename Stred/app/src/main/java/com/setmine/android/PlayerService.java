package com.setmine.android;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by oscarlafarga on 10/31/14.
 */

public class PlayerService extends Service {

    public MediaPlayer mediaPlayer = new MediaPlayer();
    public IBinder playerBinder = new PlayerBinder();

    public class PlayerBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return playerBinder;
    }

}
