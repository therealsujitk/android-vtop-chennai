package tk.therealsuji.vtopchennai.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.helpers.NotificationHelper;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.interfaces.ExamsDao;
import tk.therealsuji.vtopchennai.models.Exam;

public class ExamNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendarFuture = Calendar.getInstance();
        calendarFuture.add(Calendar.MINUTE, 30);

        long currentTime = calendar.getTimeInMillis();
        long futureTime = calendarFuture.getTimeInMillis();

        AppDatabase appDatabase = AppDatabase.getInstance(context);
        ExamsDao examsDao = appDatabase.examsDao();

        examsDao
                .getExam(currentTime, futureTime)
                .subscribeOn(Schedulers.single())
                .subscribe(new SingleObserver<Exam.AllData>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onSuccess(Exam.@NonNull AllData examItem) {
                        try {
                            NotificationHelper notificationHelper = new NotificationHelper(context);
                            notificationHelper.getManager().notify(
                                    SettingsRepository.NOTIFICATION_ID_EXAMS,
                                    notificationHelper.notifyExam(examItem).build()
                            );
                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }
                });
    }
}
