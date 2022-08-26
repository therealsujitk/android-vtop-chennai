package com.ashish.vtopchennai.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.ashish.vtopchennai.R;

public class InfoCard extends LinearLayout {
    private final AppCompatTextView key, value;

    public InfoCard(Context context) {
        super(context);

        this.key = new AppCompatTextView(context);
        this.value = new AppCompatTextView(context);
        this.initialize(context);
    }

    public InfoCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.key = new AppCompatTextView(context);
        this.value = new AppCompatTextView(context);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.InfoCard, 0, 0);

        try {
            this.key.setText(a.getString(R.styleable.InfoCard_key));
            this.value.setText(a.getString(R.styleable.InfoCard_value));
        } finally {
            a.recycle();
        }

        this.initialize(context);
    }

    private void initialize(Context context) {
        float pixelDensity = context.getResources().getDisplayMetrics().density;

        TypedValue colorPrimary = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);

        this.key.setTextColor(colorPrimary.data);
        this.key.setTextSize(16);

        this.value.setTextColor(colorPrimary.data);
        this.value.setTextSize(42);

        this.setGravity(Gravity.BOTTOM);
        this.setOrientation(VERTICAL);
        this.setPadding(
                (int) (20 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (20 * pixelDensity)
        );

        this.setBackground(ContextCompat.getDrawable(context, R.drawable.background_card));
        this.addView(this.value);
        this.addView(this.key);
    }

    public void setKey(String key) {
        this.key.setText(key);
    }

    public void setValue(String value) {
        this.value.setText(value);
    }
}
