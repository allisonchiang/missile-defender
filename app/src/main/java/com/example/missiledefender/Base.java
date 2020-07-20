package com.example.missiledefender;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.media.Image;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

class Base {

    private MainActivity mainActivity;
    private ImageView imageView;
    private static final String TAG = "Base";

    Base(ImageView imageView, final MainActivity mainActivity) {
        this.imageView = imageView;
        this.mainActivity = mainActivity;
        Log.d(TAG, "Base: " + imageView);
    }

    // returns center x of base image view
    float getX() {
        return (float) (imageView.getX() + (0.5 * imageView.getWidth()));
    }

    // returns center y of base image view
    float getY() {
        return (float) (imageView.getY() + (0.5 * imageView.getHeight()));
    }

    void destruct() {
        SoundPlayer.getInstance().start("base_blast");
        mainActivity.getLayout().removeView(imageView);

        final ImageView iv = new ImageView(mainActivity);
        iv.setImageResource(R.drawable.blast);
        iv.setTransitionName("Missile Hit Base Blast");

        // set the blast ImageView's X,Y to the Base's X,Y
        float blastX = this.getX() - (float)(0.5 * iv.getDrawable().getIntrinsicWidth());
        float blastY = this.getY() - (float)(0.5 * iv.getDrawable().getIntrinsicWidth());
        iv.setX(blastX);
        iv.setY(blastY);

        Log.d(TAG, "destruct: " + iv.getX() + ", " + iv.getY());

        mainActivity.getLayout().addView(iv);

        final ObjectAnimator alpha = ObjectAnimator.ofFloat(iv, "alpha", 1.0f, 0.0f);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(3000);
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.getLayout().removeView(iv);
            }
        });
        alpha.start();
    }
}