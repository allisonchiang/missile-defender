package com.example.missiledefender;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import static com.example.missiledefender.MainActivity.screenHeight;
import static com.example.missiledefender.MainActivity.screenWidth;

class ScrollingClouds {

    private Context context;
    private ViewGroup layout;
    private ImageView backImageA;
    private ImageView backImageB;
    private long duration;
    private int resId;

    ScrollingClouds(Context context, ViewGroup layout, int resId, long duration) {
        this.context = context;
        this.layout = layout;
        this.resId = resId;
        this.duration = duration;

        setupBackground();
    }

    private void setupBackground() {
        backImageA = new ImageView(context);
        backImageB = new ImageView(context);

        LinearLayout.LayoutParams params = new LinearLayout
                .LayoutParams(screenWidth, screenHeight);
        backImageA.setLayoutParams(params);
        backImageB.setLayoutParams(params);

        layout.addView(backImageA);
        layout.addView(backImageB);

        Bitmap backBitmapA = BitmapFactory.decodeResource(context.getResources(), resId);
        Bitmap backBitmapB = BitmapFactory.decodeResource(context.getResources(), resId);

        backImageA.setImageBitmap(backBitmapA);
        backImageB.setImageBitmap(backBitmapB);

        backImageA.setScaleType(ImageView.ScaleType.FIT_XY);
        backImageB.setScaleType(ImageView.ScaleType.FIT_XY);

        backImageA.setZ(-1);
        backImageB.setZ(-1);

        animateBack();

        increaseAlpha();
    }

    private void animateBack() {
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(duration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                float width = screenWidth;

                float a_translationX = width * progress;
                float b_translationX = width * progress - width;

                backImageA.setTranslationX(a_translationX);
                backImageB.setTranslationX(b_translationX);
            }
        });
        animator.start();
    }

    private void increaseAlpha() {
        final ObjectAnimator alphaA = ObjectAnimator.ofFloat(backImageA, "alpha", 0.25f, 0.9f);
        final ObjectAnimator alphaB = ObjectAnimator.ofFloat(backImageB, "alpha", 0.25f, 0.9f);

        AnimatorSet aSet = new AnimatorSet();
        aSet.playTogether(alphaA, alphaB);

        aSet.setInterpolator(new LinearInterpolator());
        aSet.setDuration(10000);
        aSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                decreaseAlpha();
            }
        });
        aSet.start();
    }

    private void decreaseAlpha() {
        final ObjectAnimator alphaA = ObjectAnimator.ofFloat(backImageA, "alpha", 0.9f, 0.25f);
        final ObjectAnimator alphaB = ObjectAnimator.ofFloat(backImageB, "alpha", 0.9f, 0.25f);

        AnimatorSet aSet = new AnimatorSet();
        aSet.playTogether(alphaA, alphaB);

        aSet.setInterpolator(new LinearInterpolator());
        aSet.setDuration(10000);
        aSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                increaseAlpha();
            }
        });
        aSet.start();
    }
}