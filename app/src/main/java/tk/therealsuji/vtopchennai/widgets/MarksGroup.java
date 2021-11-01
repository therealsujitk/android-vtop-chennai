package tk.therealsuji.vtopchennai.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.adapters.MarksItemAdapter;
import tk.therealsuji.vtopchennai.models.Mark;

public class MarksGroup extends LinearLayout {
    public static final int TYPE_LAB = 0;
    public static final int TYPE_PROJECT = 1;
    public static final int TYPE_THEORY = 2;

    AppCompatTextView sectionType;
    RecyclerView marks;

    public MarksGroup(Context context) {
        super(context);

        this.initialize();
    }

    void initialize() {
        float pixelDensity = this.getContext().getResources().getDisplayMetrics().density;

        TypedValue colorOnBackground = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorOnBackground, colorOnBackground, true);

        this.sectionType = new AppCompatTextView(this.getContext());
        LayoutParams sectionTypeParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        );
        sectionTypeParams.setMargins(
                (int) (50 * pixelDensity),
                (int) (10 * pixelDensity),
                (int) (50 * pixelDensity),
                (int) (10 * pixelDensity)
        );
        this.sectionType.setLayoutParams(sectionTypeParams);
        this.sectionType.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        this.sectionType.setTextColor(colorOnBackground.data);
        this.sectionType.setTextSize(15);
        this.sectionType.setTypeface(this.sectionType.getTypeface(), Typeface.BOLD);

        this.addView(this.sectionType);

        this.marks = new RecyclerView(this.getContext());
        RecyclerView.LayoutParams announcementsParams = new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
        this.marks.setLayoutParams(announcementsParams);
        this.marks.setLayoutManager(new LinearLayoutManager(this.getContext()));

        this.addView(this.marks);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        this.setLayoutParams(params);
        this.setOrientation(VERTICAL);
    }

    public void setMarksSection(int sectionType, List<Mark.AllData> marks) {
        if (sectionType == TYPE_LAB) {
            this.sectionType.setText(this.getContext().getString(R.string.lab));
        } else if (sectionType == TYPE_PROJECT) {
            this.sectionType.setText(this.getContext().getString(R.string.project));
        } else {
            this.sectionType.setText(this.getContext().getString(R.string.theory));
        }

        this.marks.setAdapter(new MarksItemAdapter(marks));
    }
}
