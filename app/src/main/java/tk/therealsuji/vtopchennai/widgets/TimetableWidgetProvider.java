package tk.therealsuji.vtopchennai.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.activities.MainActivity;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.interfaces.TimetableDao;
import tk.therealsuji.vtopchennai.models.Timetable;
import tk.therealsuji.vtopchennai.services.TimetableWidgetService;

/**
 * Widget provider for the Timetable widget that displays today's classes.
 */
public class TimetableWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_REFRESH = "tk.therealsuji.vtopchennai.ACTION_REFRESH_WIDGET";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_REFRESH.equals(intent.getAction())) {
            // Refresh all widgets
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, TimetableWidgetProvider.class));

            // Notify data changed for all widgets
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list_view_classes);

            // Update all widgets
            for (int appWidgetId : appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId);
            }
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_widget_timetable);

        // Set the current day and date
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM", Locale.getDefault());
        views.setTextViewText(R.id.text_view_day, dateFormat.format(calendar.getTime()));

        // Set up the intent for the ListView adapter
        Intent serviceIntent = new Intent(context, TimetableWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.list_view_classes, serviceIntent);

        // Set empty view
        views.setEmptyView(R.id.list_view_classes, R.id.text_view_empty);

        // Set up click intent to open the app
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setPendingIntentTemplate(R.id.list_view_classes, mainPendingIntent);

        // Set up refresh button
        Intent refreshIntent = new Intent(context, TimetableWidgetProvider.class);
        refreshIntent.setAction(ACTION_REFRESH);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(
                context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.image_view_refresh, refreshPendingIntent);

        // Clicking the day header opens the app
        views.setOnClickPendingIntent(R.id.text_view_day, mainPendingIntent);

        // Check if there are classes today to show/hide empty state and auto-scroll
        new Thread(() -> {
            try {
                AppDatabase appDatabase = AppDatabase.getInstance(context);
                TimetableDao timetableDao = appDatabase.timetableDao();
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                
                if (dayOfWeek == 6) { // Saturday
                    int assignedDay = SettingsRepository.getAssignedSaturday(context);
                    if (assignedDay != -1) {
                         dayOfWeek = assignedDay;
                    }
                }
                
                List<Timetable.AllData> classes = timetableDao.getForWidgetSync(dayOfWeek);

                if (classes == null || classes.isEmpty()) {
                    views.setViewVisibility(R.id.text_view_empty, View.VISIBLE);
                    views.setViewVisibility(R.id.list_view_classes, View.GONE);
                } else {
                    views.setViewVisibility(R.id.text_view_empty, View.GONE);
                    views.setViewVisibility(R.id.list_view_classes, View.VISIBLE);

                    // Calculate scroll position (first upcoming/ongoing class)
                    int scrollToPosition = -1;
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String currentTime = timeFormat.format(calendar.getTime());
                    java.util.Date current = timeFormat.parse(currentTime);

                    for (int i = 0; i < classes.size(); i++) {
                        Timetable.AllData item = classes.get(i);
                        if (item.endTime != null) {
                            java.util.Date end = timeFormat.parse(item.endTime);
                            // If class ends after current time (meaning it's ongoing or upcoming)
                            if (end != null && end.after(current)) {
                                scrollToPosition = i;
                                break;
                            }
                        }
                    }

                    // Auto-scroll to the current/next class (API 31+)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && scrollToPosition != -1) {
                         views.setScrollPosition(R.id.list_view_classes, scrollToPosition);
                    }
                }

                appWidgetManager.updateAppWidget(appWidgetId, views);
            } catch (Exception e) {
                // If there's an error, just show the widget normally
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }).start();

        // Update immediately while background thread runs
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onEnabled(Context context) {
        // Called when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Called when the last widget is disabled
    }
}
