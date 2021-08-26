package tk.therealsuji.vtopchennai.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import tk.therealsuji.vtopchennai.R;

public class SettingsGroup extends LinearLayout {
    Context context;
    AppCompatTextView title;

    public SettingsGroup(Context context) {
        super(context);

        this.context = context;
        this.initialize();
    }

    public SettingsGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        this.initialize();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SettingsGroup, 0, 0);

        try {
            this.setCategory(a.getString(R.styleable.SettingsGroup_category));
        } finally {
            a.recycle();
        }
    }

    public void initialize() {
        float pixelDensity = context.getResources().getDisplayMetrics().density;
        ColorStateList colorPrimary = ColorStateList.valueOf(context.getColor(R.color.colorPrimary));

        this.title = new AppCompatTextView(this.context);
        this.title.setPadding(
                (int) (20 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (5 * pixelDensity)
        );
        this.title.setTextSize(16);
        this.title.setTextColor(colorPrimary);
        this.title.setTypeface(this.title.getTypeface(), Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        this.setLayoutParams(params);
        this.setOrientation(VERTICAL);

        this.addView(this.title);
    }

    public void setCategory(String category) {
        this.title.setText(category);
    }
}
