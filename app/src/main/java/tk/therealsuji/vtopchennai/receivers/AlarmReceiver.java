package tk.therealsuji.vtopchennai.receivers;

import static android.content.Context.AUDIO_SERVICE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG="Alarm Receiver";
    public static final String ACTION_RINGER_NORMAL="normal";
    public static final String ACTION_RINGER_SILENT="silent";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(TAG, "Inside alarm receiver");
        if (intent.getAction().equals(ACTION_RINGER_NORMAL)) {

            AudioManager manager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
            manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            Log.w(TAG, "NORMAL");
        }
        else if (intent.getAction().equals(ACTION_RINGER_SILENT)) {
            ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE))
                    .setRingerMode(AudioManager.RINGER_MODE_SILENT);
            Log.w(TAG, "SILENT");
        }
//            Calendar calendar = Calendar.getInstance();
//            Calendar calendarFuture = Calendar.getInstance();
//            SharedPreferences sharedPreferences= SettingsRepository.getSharedPreferences(context.getApplicationContext());
//            calendarFuture.add(Calendar.MINUTE , sharedPreferences.getInt("notification_interval",30));
//
//            SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
//
//            int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
//            String currentTime = hour24.format(calendar.getTime());
//
//            AppDatabase appDatabase = AppDatabase.getInstance(context);
//            TimetableDao timetableDao = appDatabase.timetableDao();
//
//            timetableDao
//                    .getOngoing(day, currentTime)
//                    .subscribeOn(Schedulers.single())
//                    .subscribe(new SingleObserver<Timetable.AllData>() {
//                        @Override
//                        public void onSubscribe(@NonNull Disposable d) {
//                        }
//
//                        @Override
//                        public void onSuccess(Timetable.@NonNull AllData timetableItem) {
//
//                        }
//
//                        @Override
//                        public void onError(@NonNull Throwable e) {
//                        }
//                    });

//        AppDatabase appDatabase = AppDatabase.getInstance(context);
//        TimetableDao timetableDao = appDatabase.timetableDao();
//
//        Calendar calendar = Calendar.getInstance();
//        SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
//
//        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
//        String currentTime = hour24.format(calendar.getTime());
//
//        timetableDao
//                .getOngoing(day, currentTime)
//                .subscribeOn(Schedulers.single())
//                .subscribe(new SingleObserver<Timetable.AllData>() {
//                    @Override
//                    public void onSubscribe(@NonNull Disposable d) {
//                    }
//
//                    @Override
//                    public void onSuccess(Timetable.@NonNull AllData timetableItem) {
//                        try {
//                            setAlarm(timetableItem,context);
//                        } catch (Exception exception) {
//                            Log.w(TAG,exception.toString());
//                        }
//                    }
//
//                    @Override
//                    public void onError(@NonNull Throwable e) {
//                    }
//                });
    }
}


