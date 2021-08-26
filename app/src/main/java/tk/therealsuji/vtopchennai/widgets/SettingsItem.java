package tk.therealsuji.vtopchennai.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import tk.therealsuji.vtopchennai.R;

public class SettingsItem extends LinearLayout {
    Context context;
    AppCompatTextView title;

    public SettingsItem(Context context) {
        super(context);

        this.context = context;
        this.initialize();
    }

    public SettingsItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        this.initialize();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SettingsItem, 0, 0);

        try {
            this.setTitle(a.getString(R.styleable.SettingsItem_title));
        } finally {
            a.recycle();
        }
    }

    private void initialize() {
        float pixelDensity = context.getResources().getDisplayMetrics().density;

        this.title = new AppCompatTextView(this.context);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT,
                1
        );
        titleParams.setMarginEnd((int) (20 * pixelDensity));
        this.title.setLayoutParams(titleParams);
        this.title.setTextSize(18);

        ImageView rightChevron = new ImageView(context);
        LinearLayout.LayoutParams rightChevronParams = new LinearLayout.LayoutParams(
                (int) (40 * pixelDensity),
                (int) (40 * pixelDensity)
        );
        rightChevron.setLayoutParams(rightChevronParams);
        rightChevron.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_chevron_right));
        rightChevron.setPadding(
                (int) (10 * pixelDensity),
                0,
                0,
                0
        );

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        this.setLayoutParams(params);
        this.setClickable(true);
        this.setFocusable(true);
        this.setGravity(Gravity.CENTER_VERTICAL);
        this.setPadding(
                (int) (20 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (10 * pixelDensity)
        );

        TypedValue selectableItemBackground = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, selectableItemBackground, true);
        this.setBackgroundResource(selectableItemBackground.resourceId);

        this.addView(title);
        this.addView(rightChevron);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }
}
