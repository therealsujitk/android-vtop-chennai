package tk.therealsuji.vtopchennai.services;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.helpers.AppDatabase;
import tk.therealsuji.vtopchennai.helpers.SettingsRepository;
import tk.therealsuji.vtopchennai.interfaces.TimetableDao;
import tk.therealsuji.vtopchennai.models.Timetable;

/**
 * RemoteViewsService for the Timetable widget.
 * Provides the data for the ListView in the widget.
 */
public class TimetableWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TimetableRemoteViewsFactory(this.getApplicationContext());
    }

    /**
     * Factory class that creates the RemoteViews for each item in the widget's ListView.
     */
    private static class TimetableRemoteViewsFactory implements RemoteViewsFactory {

        private final Context context;
        private List<Timetable.AllData> timetableItems;

        TimetableRemoteViewsFactory(Context context) {
            this.context = context;
            this.timetableItems = new ArrayList<>();
        }

        @Override
        public void onCreate() {
            // Initial data load
            loadData();
        }

        @Override
        public void onDataSetChanged() {
            // Reload data when widget is refreshed
            loadData();
        }

        private void loadData() {
            try {
                AppDatabase appDatabase = AppDatabase.getInstance(context);
                if (appDatabase == null) {
                    android.util.Log.e("TimetableWidget", "Database is null");
                    timetableItems = new ArrayList<>();
                    return;
                }
                
                TimetableDao timetableDao = appDatabase.timetableDao();

                Calendar calendar = Calendar.getInstance();
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                
                if (dayOfWeek == 6) { // Saturday
                    int assignedDay = SettingsRepository.getAssignedSaturday(context);
                    if (assignedDay != -1) {
                        dayOfWeek = assignedDay;
                    }
                }
                
                android.util.Log.d("TimetableWidget", "Loading data for day: " + dayOfWeek);

                List<Timetable.AllData> classes = timetableDao.getForWidgetSync(dayOfWeek);
                if (classes != null) {
                    timetableItems = new ArrayList<>(classes);
                    android.util.Log.d("TimetableWidget", "Loaded " + classes.size() + " classes");
                } else {
                    timetableItems = new ArrayList<>();
                    android.util.Log.d("TimetableWidget", "No classes found");
                }
            } catch (Exception e) {
                android.util.Log.e("TimetableWidget", "Error loading data: " + e.getMessage(), e);
                timetableItems = new ArrayList<>();
            }
        }

        @Override
        public void onDestroy() {
            timetableItems.clear();
        }

        @Override
        public int getCount() {
            return timetableItems.size();
        }


        @Override
        public RemoteViews getViewAt(int position) {
            if (position >= timetableItems.size()) {
                return null;
            }

            try {
                Timetable.AllData item = timetableItems.get(position);
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_widget_timetable_item);

                // Check if class is completed (ended)
                boolean isCompleted = isClassCompleted(item.endTime);

                // Set course title
                String title = "Class " + (position + 1);
                if (item.courseTitle != null && !item.courseTitle.isEmpty()) {
                    title = item.courseTitle;
                } else if (item.courseCode != null && !item.courseCode.isEmpty()) {
                    title = item.courseCode;
                }
                views.setTextViewText(R.id.text_view_course_title, title);

                // Set venue
                String venue = (item.venue != null && !item.venue.isEmpty()) ? item.venue : "No venue";
                views.setTextViewText(R.id.text_view_venue, venue);

                // Set timing in "Start - End" format
                String startTime = item.startTime != null ? formatTime(item.startTime) : "";
                String endTime = item.endTime != null ? formatTime(item.endTime) : "";
                String timing = startTime + " - " + endTime;
                views.setTextViewText(R.id.text_view_timing, timing);


                // Styling based on completion
                if (isCompleted) {
                    // Past classes: Dull/dimmed look
                    views.setInt(R.id.layout_item_card, "setBackgroundResource", R.drawable.background_widget_item_completed);
                    
                    // Use "Hint" color for dull text
                    int dullColor = context.getColor(R.color.widget_text_hint);
                    views.setTextColor(R.id.text_view_course_title, dullColor);
                    views.setTextColor(R.id.text_view_venue, dullColor);
                    views.setTextColor(R.id.text_view_timing, dullColor);
                } else {
                    // Upcoming/Active classes: Highlighted/Normal look
                    views.setInt(R.id.layout_item_card, "setBackgroundResource", R.drawable.background_widget_item);
                    
                    // Use primary/secondary colors from our theme resources
                    views.setTextColor(R.id.text_view_course_title, context.getColor(R.color.widget_text_primary));
                    views.setTextColor(R.id.text_view_venue, context.getColor(R.color.widget_text_secondary)); 
                    views.setTextColor(R.id.text_view_timing, context.getColor(R.color.widget_text_secondary)); 
                }

                // Click intent (using the day/list template from provider)
                Intent fillInIntent = new Intent();
                views.setOnClickFillInIntent(R.id.layout_item_card, fillInIntent);

                return views;
            } catch (Exception e) {
                android.util.Log.e("TimetableWidget", "Error in getViewAt: " + e.getMessage(), e);
                return null;
            }
        }

        /**
         * Checks if a class has ended based on current time.
         * @param endTime Class end time in "HH:mm" format
         * @return true if class has ended, false otherwise
         */
        private boolean isClassCompleted(String endTime) {
            if (endTime == null || endTime.isEmpty()) {
                return false;
            }

            try {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Calendar now = Calendar.getInstance();
                String currentTime = timeFormat.format(now.getTime());
                
                java.util.Date current = timeFormat.parse(currentTime);
                java.util.Date end = timeFormat.parse(endTime);
                
                return current != null && end != null && current.after(end);
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * Formats 24-hour time string to 12-hour format.
         * Input: "09:00" or "14:30"
         * Output: "9:00 AM" or "2:30 PM"
         */
        private String formatTime(String time) {
            if (time == null || time.isEmpty()) {
                return "";
            }

            try {
                SimpleDateFormat input = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("h:mm a", Locale.getDefault());
                return output.format(input.parse(time));
            } catch (Exception e) {
                return time;
            }
        }

        @Override
        public RemoteViews getLoadingView() {
            // Return null to use the default loading view
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (position < timetableItems.size()) {
                return timetableItems.get(position).slotId;
            }
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
