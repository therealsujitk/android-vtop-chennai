package tk.therealsuji.vtopchennai.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.models.Marks;

public class MarksItem extends LinearLayout {
    private AppCompatTextView title;
    private LinearLayout score, weightage, average, status;

    public MarksItem(Context context) {
        super(context);

        this.initialize();
    }

    public MarksItem(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.initialize();
    }

    private void initialize() {
        float pixelDensity = this.getContext().getResources().getDisplayMetrics().density;

        TypedValue colorSecondary = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorSecondary, colorSecondary, true);

        this.title = new AppCompatTextView(this.getContext());
        LayoutParams titleParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(
                (int) (20 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (5 * pixelDensity)
        );
        this.title.setLayoutParams(titleParams);
        this.title.setTextColor(colorSecondary.data);
        this.title.setTextSize(20);
        this.title.setTypeface(this.title.getTypeface(), Typeface.BOLD);

        this.addView(this.title);

        LinearLayout container = new LinearLayout(this.getContext());
        LayoutParams containerParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(
                (int) (20 * pixelDensity),
                0,
                (int) (20 * pixelDensity),
                (int) (17 * pixelDensity)
        );
        container.setLayoutParams(containerParams);
        container.setOrientation(VERTICAL);

        this.score = generateBlock(this.getContext().getString(R.string.score));
        this.weightage = generateBlock(this.getContext().getString(R.string.weightage));
        this.average = generateBlock(this.getContext().getString(R.string.average));
        this.status = generateBlock(this.getContext().getString(R.string.status));

        container.addView(this.score);
        container.addView(this.weightage);
        container.addView(this.average);
        container.addView(this.status);

        this.addView(container);

        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        this.setLayoutParams(params);
        this.setBackground(ContextCompat.getDrawable(this.getContext(), R.drawable.background_marks_item));
        this.setGravity(Gravity.CENTER_HORIZONTAL);
        this.setOrientation(VERTICAL);
    }

    private LinearLayout generateBlock(String title) {
        float pixelDensity = this.getContext().getResources().getDisplayMetrics().density;

        LinearLayout linearLayout = new LinearLayout(this.getContext());
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(
                0,
                (int) (3 * pixelDensity),
                0,
                (int) (3 * pixelDensity)
        );
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setOrientation(HORIZONTAL);
        linearLayout.setVisibility(GONE);

        linearLayout.addView(generateTextView(title, Typeface.NORMAL, TEXT_ALIGNMENT_TEXT_START, LayoutParams.WRAP_CONTENT));
        linearLayout.addView(generateTextView("", Typeface.BOLD, TEXT_ALIGNMENT_TEXT_END, LayoutParams.MATCH_PARENT));

        return linearLayout;
    }

    private AppCompatTextView generateTextView(String text, int typeface, int textAlignment, int layoutWidth) {
        TypedValue colorSecondary = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorSecondary, colorSecondary, true);

        AppCompatTextView appCompatTextView = new AppCompatTextView(this.getContext());
        LayoutParams layoutParams = new LayoutParams(
                layoutWidth,
                LayoutParams.WRAP_CONTENT
        );
        appCompatTextView.setLayoutParams(layoutParams);
        appCompatTextView.setText(text);
        appCompatTextView.setTextAlignment(textAlignment);
        appCompatTextView.setTextColor(colorSecondary.data);
        appCompatTextView.setTextSize(16);
        appCompatTextView.setTypeface(appCompatTextView.getTypeface(), typeface);

        return appCompatTextView;
    }

    public void initializeMarksItem(@NonNull Marks marksItem) {
        this.title.setText(marksItem.title);

        if (!marksItem.score.equals("")) {
            ((AppCompatTextView) this.score.getChildAt(1)).setText(marksItem.score);
            this.score.setVisibility(VISIBLE);
        }

        if (!marksItem.weightage.equals("")) {
            ((AppCompatTextView) this.weightage.getChildAt(1)).setText(marksItem.weightage);
            this.weightage.setVisibility(VISIBLE);
        }

        if (!marksItem.average.equals("")) {
            ((AppCompatTextView) this.average.getChildAt(1)).setText(marksItem.average);
            this.average.setVisibility(VISIBLE);
        }

        if (!marksItem.status.equals("")) {
            ((AppCompatTextView) this.status.getChildAt(1)).setText(marksItem.status);
            this.status.setVisibility(VISIBLE);
        }
    }

    public void setMargin(int left, int top, int right, int bottom) {
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) this.getLayoutParams();
        params.setMargins(left, top, right, bottom);
        this.setLayoutParams(params);
    }
}
