package tk.therealsuji.vtopchennai.widgets;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import tk.therealsuji.vtopchennai.R;

public class TimetableItem extends RelativeLayout {
    public static final int CLASS_THEORY = 0;
    public static final int CLASS_LAB = 1;

    public static final int STATUS_PAST = 0;
    public static final int STATUS_PRESENT = 1;
    public static final int STATUS_FUTURE = 2;

    Context context;
    private ProgressBar classProgress;
    private ImageView courseType;
    private AppCompatTextView courseCode, timings;
    private int status;

    public TimetableItem(Context context) {
        super(context);

        this.context = context;
        this.initialize(context);
    }

    public TimetableItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        this.initialize(context);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimetableItem, 0, 0);

        try {
            this.setCourseType(a.getInt(R.styleable.TimetableItem_course_type, 0));
            this.setCourseCode(a.getString(R.styleable.TimetableItem_course_code));
            this.setTimings(a.getString(R.styleable.TimetableItem_start_time), a.getString(R.styleable.TimetableItem_end_time));
        } finally {
            a.recycle();
        }
    }

    private void initialize(Context context) {
        float pixelDensity = context.getResources().getDisplayMetrics().density;

        int containerId = View.generateViewId();

        LinearLayout container = new LinearLayout(context);
        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        container.setLayoutParams(containerParams);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setId(containerId);
        container.setPadding(
                (int) (20 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (10 * pixelDensity)
        );
        container.setOrientation(LinearLayout.HORIZONTAL);

        this.classProgress = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        RelativeLayout.LayoutParams classProgressParams = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        classProgressParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        classProgressParams.addRule(RelativeLayout.ALIGN_BOTTOM, containerId);
        this.classProgress.setLayoutParams(classProgressParams);
        this.classProgress.setClickable(true);
        this.classProgress.setFocusable(true);
        this.classProgress.setOnClickListener(view -> this.onClick());
        this.classProgress.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.background_class_item));

        this.courseType = new ImageView(context);
        this.courseType.setPadding(
                (int) (10 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (10 * pixelDensity)
        );
        this.courseType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_lab));

        LinearLayout classInfo = new LinearLayout(context);
        LinearLayout.LayoutParams classInfoParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                1
        );
        classInfo.setLayoutParams(classInfoParams);
        classInfo.setOrientation(LinearLayout.VERTICAL);
        classInfo.setPadding(
                (int) (5 * pixelDensity),
                0,
                (int) (10 * pixelDensity),
                0
        );

        this.courseCode = new AppCompatTextView(context);
        this.courseCode.setTextSize(20);

        this.timings = new AppCompatTextView(context);
        this.timings.setTextSize(16);

        classInfo.addView(this.courseCode);
        classInfo.addView(this.timings);

        ImageView moreInfo = new ImageView(context);
        moreInfo.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_chevron_right));
        moreInfo.setPadding(
                (int) (10 * pixelDensity),
                0,
                0,
                0
        );

        TypedValue colorSecondary = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorSecondary, colorSecondary, true);
        ColorStateList colorSecondaryTintList = ColorStateList.valueOf(colorSecondary.data);

        this.courseType.setImageTintList(colorSecondaryTintList);
        this.courseCode.setTextColor(colorSecondary.data);
        this.timings.setTextColor(colorSecondary.data);
        moreInfo.setImageTintList(colorSecondaryTintList);

        container.addView(this.courseType);
        container.addView(classInfo);
        container.addView(moreInfo);

        this.addView(this.classProgress);
        this.addView(container);
    }

    private void onClick() {

    }

    private void setClassProgressDuration(long duration) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this.classProgress, "progress", this.classProgress.getProgress(), this.classProgress.getMax());
        objectAnimator.setDuration(duration);
        objectAnimator.setInterpolator(new LinearInterpolator());
        objectAnimator.start();
    }

    private void setMaxClassProgress(long duration) {
        int minutes = (int) duration / (1000 * 60);
        this.classProgress.setMax(minutes);
    }

    private void setClassProgress(long duration) {
        int minutes = (int) duration / (1000 * 60);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.classProgress.setProgress(minutes, true);
        } else {
            this.classProgress.setProgress(minutes);
        }
    }

    public void setCourseType(int courseType) {
        if (courseType == CLASS_LAB) {
            this.courseType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_lab));
        } else {
            this.courseType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_theory));
        }
    }

    public void setCourseCode(String courseCode) {
        this.courseCode.setText(courseCode);
    }

    public void setTimings(String startTime, String endTime) {
        String timings = startTime + " - " + endTime;

        if (!DateFormat.is24HourFormat(this.context)) {
            SimpleDateFormat hour24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            SimpleDateFormat hour12 = new SimpleDateFormat("h:mm a", Locale.ENGLISH);

            try {
                Date startTimeDate = hour24.parse(startTime);
                Date endTimeDate = hour24.parse(endTime);

                if (startTimeDate != null && endTimeDate != null) {
                    timings = hour12.format(startTimeDate) + " - " + hour12.format(endTimeDate);

                    if (this.status == STATUS_PAST) {
                        this.classProgress.setProgress(100);
                    } else if (this.status == STATUS_PRESENT) {
                        Date now = hour24.parse(hour24.format(Calendar.getInstance().getTime()));

                        if (now != null) {
                            if (now.after(endTimeDate)) {
                                this.classProgress.setProgress(100);
                            } else if (now.after(startTimeDate)) {
                                long duration = endTimeDate.getTime() - startTimeDate.getTime();
                                long durationComplete = now.getTime() - startTimeDate.getTime();
                                long durationPending = endTimeDate.getTime() - now.getTime();

                                this.setMaxClassProgress(duration);
                                this.setClassProgress(durationComplete);
                                this.setClassProgressDuration(durationPending);
                            } else {
                                long duration = endTimeDate.getTime() - startTimeDate.getTime();
                                long durationPending = startTimeDate.getTime() - now.getTime();

                                setMaxClassProgress(duration);
                                new Handler().postDelayed(() -> this.setClassProgressDuration(duration), durationPending);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        this.timings.setText(timings);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setPadding(int padding) {
        this.setPadding(padding, padding, padding, padding);
    }
}
