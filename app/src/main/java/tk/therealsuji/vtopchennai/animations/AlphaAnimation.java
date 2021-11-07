package tk.therealsuji.vtopchennai.animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class AlphaAnimation extends Animation {
    float finalAlpha, initialAlpha;
    View view;

    public AlphaAnimation(View view, float finalAlpha) {
        this.finalAlpha = finalAlpha;
        this.initialAlpha = view.getAlpha();
        this.view = view;

        this.setDuration((long) (200 * Math.abs(this.finalAlpha - this.initialAlpha)));
        this.setInterpolator(new LinearInterpolator());
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (finalAlpha >= initialAlpha) {
            this.view.setAlpha(initialAlpha + (finalAlpha - initialAlpha) * interpolatedTime);
        } else {
            this.view.setAlpha((initialAlpha - finalAlpha) * (1 - interpolatedTime));
        }
    }
}
