package tk.therealsuji.vtopchennai.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import tk.therealsuji.vtopchennai.R;

public class TimetableItem extends RelativeLayout {
    public static final int THEORY = 0;
    public static final int LAB = 1;
    Context context;
    private ProgressBar classProgress;
    private ImageView courseType;
    private AppCompatTextView courseCode, timings;
    private String[] rawCourse;

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

        this.setClassProgress();

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

        ColorStateList colorPrimary = ColorStateList.valueOf(context.getColor(R.color.colorPrimary));

        this.courseType.setImageTintList(colorPrimary);
        this.courseCode.setTextColor(colorPrimary);
        this.timings.setTextColor(colorPrimary);
        moreInfo.setImageTintList(colorPrimary);

        container.addView(this.courseType);
        container.addView(classInfo);
        container.addView(moreInfo);

        this.addView(this.classProgress);
        this.addView(container);
    }

    private void onClick() {

    }

    private void setClassProgress() {
        this.classProgress.setProgress(25);
    }

    public void setCourseType(int courseType) {
        if (courseType == LAB) {
            this.courseType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_lab));
        } else {
            this.courseType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_theory));
        }
    }

    private void setCourseCode(String courseCode) {
        this.courseCode.setText(courseCode);
    }

    public void setRawCourse(String rawCourse) {
        this.rawCourse = rawCourse.split("-");
        setCourseCode(this.rawCourse[1]);
    }

    public void setTimings(String startTime, String endTime) {
        String timings = startTime + " - " + endTime;
        this.timings.setText(timings);
    }

    public void setPadding(int padding) {
        this.setPadding(padding, padding, padding, padding);
    }
}
