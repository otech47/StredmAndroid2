package com.setmine.android.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            final KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Intent playIntent = new Intent(context, PlayerService.class);

            if(event != null && event.getAction() == KeyEvent.ACTION_UP) {
                if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == event.getKeyCode()) {
                    playIntent.setAction("PLAY_PAUSE");
                } else if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                    playIntent.setAction("PLAY_PAUSE");
                } else if (KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode()) {
                    playIntent.setAction("PLAY_PAUSE");
                } else if (KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()) {
                    playIntent.setAction("NEXT");
                } else if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == event.getKeyCode()) {
                    playIntent.setAction("PREVIOUS");
                }
                context.startService(playIntent);
            }
        }
    }
}