package tk.therealsuji.vtopchennai.animations;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class ResizeAnimation extends Animation {
    public static final int DIRECTION_X = 1;

    int animationDirection, width, height, marginStart, marginEnd;
    int endMarginStart, endMarginEnd, endValue;
    View view;

    public ResizeAnimation(@NonNull View view, int animationDirection, int endValue, @NonNull JSONObject defaultValues) throws JSONException {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

        this.animationDirection = animationDirection;
        this.width = view.getMeasuredWidth();
        this.height = view.getMeasuredHeight();
        this.marginStart = layoutParams.getMarginStart();
        this.marginEnd = layoutParams.getMarginEnd();
        this.endMarginStart = defaultValues.getInt("marginStart") * endValue / defaultValues.getInt("cardWidth");
        this.endMarginEnd = defaultValues.getInt("marginEnd") * endValue / defaultValues.getInt("cardWidth");
        this.endValue = endValue;
        this.view = view;

        this.setDuration(200L * Math.abs(this.width - this.endValue) / defaultValues.getInt("cardWidth"));
        this.setInterpolator(new LinearInterpolator());
        ((View) this.view.getParent()).invalidate();    // Animations don't work unless this is present
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) this.view.getLayoutParams();

        if (this.animationDirection == DIRECTION_X) {
            if (this.endValue >= this.width) {
                layoutParams.width = this.width + (int) ((this.endValue - this.width) * interpolatedTime);
                layoutParams.setMarginStart(this.marginStart + (int) ((this.endMarginStart - this.marginStart) * interpolatedTime));
                layoutParams.setMarginEnd(this.marginEnd + (int) ((this.endMarginEnd - this.marginEnd) * interpolatedTime));
            } else {
                layoutParams.width = (int) ((this.width - this.endValue) * (1 - interpolatedTime));
                layoutParams.setMarginStart((int) ((this.marginStart - this.endMarginStart) * (1 - interpolatedTime)));
                layoutParams.setMarginEnd((int) ((this.marginEnd - this.endMarginEnd) * (1 - interpolatedTime)));
            }

            layoutParams.height = this.height;
        }

        this.view.setLayoutParams(layoutParams);
    }
}
