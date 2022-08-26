package com.ashish.vtopchennai.receivers;

import static android.content.Context.AUDIO_SERVICE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG="Alarm Receiver";
    public static final String ACTION_RINGER_NORMAL="normal";
    public static final String ACTION_RINGER_SILENT="silent";
    public static final String ACTION_RINGER_VIBRATE="vibrate";


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_RINGER_NORMAL)) {
            AudioManager manager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
            manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
        if (intent.getAction().equals(ACTION_RINGER_SILENT)) {
            ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE))
                    .setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
        if (intent.getAction().equals(ACTION_RINGER_VIBRATE)) {
            ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE))
                    .setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
    }
}


