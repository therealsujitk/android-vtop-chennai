package tk.therealsuji.vtopchennai.receivers;

import static android.content.Context.AUDIO_SERVICE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.interfaces.TimetableDao;
import tk.therealsuji.vtopchennai.models.Timetable;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG="Alarm Receiver";
    Intent intent;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(TAG,"Inside alarm receiver");

        this.intent=intent;

        AppDatabase appDatabase = AppDatabase.getInstance(context);
        TimetableDao timetableDao = appDatabase.timetableDao();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

        int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String currentTime = hour24.format(calendar.getTime());

        timetableDao
                .getOngoing(day, currentTime)
                .subscribeOn(Schedulers.single())
                .subscribe(new SingleObserver<Timetable.AllData>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(Timetable.@NonNull AllData timetableItem) {
                        try {
                            setAlarm(timetableItem,context);
                        } catch (Exception exception) {
                            Log.w(TAG,exception.toString());
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }
                });

    }

    void setAlarm(Timetable.AllData timetableItem,Context context){
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timetableItem.startTime.split(":")[0]));
        calendarStart.set(Calendar.MINUTE, Integer.parseInt(timetableItem.startTime.split(":")[1]));

        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timetableItem.endTime.split(":")[0]));
        calendarEnd.set(Calendar.MINUTE, Integer.parseInt(timetableItem.endTime.split(":")[1]));

        Log.w(TAG , calendarStart + "\n" + Calendar.getInstance() + "\n" + calendarEnd);

        if ( ( Calendar.getInstance().after(calendarStart) && Calendar.getInstance().before(calendarEnd) ) || ( calendarStart.equals(Calendar.getInstance() ) ) ){
            AudioManager manager=(AudioManager)context.getSystemService(AUDIO_SERVICE);
            manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            Log.w(TAG,"SILENT");
        }
        if ( Calendar.getInstance().after(calendarEnd) || calendarEnd.equals(Calendar.getInstance())){
            AudioManager manager=(AudioManager)context.getSystemService(AUDIO_SERVICE);
            manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            Log.w(TAG,"NORMAL");
        }
    }
}


