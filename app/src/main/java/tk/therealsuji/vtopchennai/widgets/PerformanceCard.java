package tk.therealsuji.vtopchennai.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import tk.therealsuji.vtopchennai.R;
import tk.therealsuji.vtopchennai.animations.AlphaAnimation;
import tk.therealsuji.vtopchennai.animations.ResizeAnimation;

public class PerformanceCard extends LinearLayout {
    AppCompatTextView title, scoreText;
    Context context;
    ProgressBar scoreProgress;

    JSONObject defaultValues;

    public PerformanceCard(Context context) {
        super(context);

        this.context = context;
        this.initialize();
    }

    public PerformanceCard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.title = new AppCompatTextView(context);
        this.scoreText = new AppCompatTextView(context);
        this.scoreProgress = new ProgressBar(context, null, 0, R.style.Widget_VTOP_CircularProgressBar);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PerformanceCard, 0, 0);

        try {
            this.setTitle(a.getString(R.styleable.PerformanceCard_title));
            this.setScore(
                    a.getFloat(R.styleable.PerformanceCard_score, 80),
                    a.getFloat(R.styleable.PerformanceCard_total, 100)
            );
        } finally {
            a.recycle();
        }

        this.context = context;
        this.initialize();
    }

    private void initialize() {
        float pixelDensity = this.context.getResources().getDisplayMetrics().density;
        this.defaultValues = new JSONObject();

        TypedValue colorPrimary = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);

        LayoutParams titleParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(
                0,
                0,
                0,
                (int) (10 * pixelDensity)
        );
        this.title.setLayoutParams(titleParams);
        this.title.setTextColor(colorPrimary.data);
        this.title.setTextSize(15);

        this.addView(this.title);

        /*
            This empty view is used to fill blank space
         */
        View emptyView = new RelativeLayout(this.context);
        LayoutParams outerContainerParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                0,
                1
        );
        emptyView.setLayoutParams(outerContainerParams);

        this.addView(emptyView);

        RelativeLayout container = new RelativeLayout(this.context);
        LayoutParams containerParams = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        container.setLayoutParams(containerParams);

        RelativeLayout.LayoutParams scoreProgressParams = new RelativeLayout.LayoutParams(
                (int) (100 * pixelDensity),
                (int) (100 * pixelDensity)
        );
        this.scoreProgress.setLayoutParams(scoreProgressParams);

        RelativeLayout.LayoutParams scoreTextParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        scoreTextParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        this.scoreText.setLayoutParams(scoreTextParams);
        this.scoreText.setTextColor(colorPrimary.data);
        this.scoreText.setTextSize(16);

        container.addView(this.scoreProgress);
        container.addView(this.scoreText);
        this.addView(container);

        this.setBackground(ContextCompat.getDrawable(this.context, R.drawable.background_card));
        this.setOrientation(VERTICAL);
        this.setPadding(
                (int) (20 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (20 * pixelDensity),
                (int) (20 * pixelDensity)
        );
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setScore(float score, float total) {
        this.scoreProgress.setProgress((int) score);
        this.scoreProgress.setMax((int) total);

        String scoreText = (total == 100) ? score + "%" : score + " / " + total;
        this.scoreText.setText(scoreText);
    }

    public void show() {
        this.clearAnimation();
        this.post(() -> {
            try {
                LinearLayout card = this;
                int cardWidth = this.defaultValues.getInt("cardWidth");

                ResizeAnimation expandAnimation = new ResizeAnimation(card, ResizeAnimation.DIRECTION_X, cardWidth, defaultValues);
                AlphaAnimation alphaAnimation = new AlphaAnimation(card, 1);

                expandAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        card.startAnimation(alphaAnimation);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                card.startAnimation(expandAnimation);
            } catch (Exception ignored) {
            }
        });
    }

    public void hide() {
        this.clearAnimation();
        this.post(() -> {
            try {
                LayoutParams layoutParams = (LayoutParams) this.getLayoutParams();
                LinearLayout card = this;

                if (!this.defaultValues.has("cardWidth")) {
                    this.defaultValues.put("cardWidth", this.getMeasuredWidth());
                    this.defaultValues.put("marginStart", layoutParams.getMarginStart());
                    this.defaultValues.put("marginEnd", layoutParams.getMarginEnd());
                }

                ResizeAnimation compressAnimation = new ResizeAnimation(card, ResizeAnimation.DIRECTION_X, 0, defaultValues);
                AlphaAnimation alphaAnimation = new AlphaAnimation(card, 0);

                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        card.startAnimation(compressAnimation);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                card.startAnimation(alphaAnimation);
            } catch (Exception ignored) {
            }

        });
    }
}
