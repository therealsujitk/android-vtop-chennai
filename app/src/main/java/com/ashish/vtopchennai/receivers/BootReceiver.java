package com.ashish.vtopchennai.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import com.ashish.vtopchennai.helpers.AppDatabase;
import com.ashish.vtopchennai.helpers.SettingsRepository;
import com.ashish.vtopchennai.interfaces.TimetableDao;
import com.ashish.vtopchennai.models.Timetable;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            return;
        }

        if (!SettingsRepository.isSignedIn(context)) {
            return;
        }

        SettingsRepository.clearTimetableNotifications(context);

        AppDatabase appDatabase = AppDatabase.getInstance(context);
        TimetableDao timetableDao = appDatabase.timetableDao();

        timetableDao
                .getTimetable()
                .subscribeOn(Schedulers.single())
                .subscribe(new SingleObserver<List<Timetable>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(@NonNull List<Timetable> timetable) {
                        for (int i = 0; i < timetable.size(); ++i) {
                            try {
                                SettingsRepository.setTimetableNotifications(context, timetable.get(i));
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }
                });
    }
}
