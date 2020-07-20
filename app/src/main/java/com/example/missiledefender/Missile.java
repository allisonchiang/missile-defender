package com.example.missiledefender;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.media.Image;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

class Missile {

    private MainActivity mainActivity;
    private ImageView imageView;
    private AnimatorSet aSet = new AnimatorSet();
    private int screenHeight;
    private float startX, startY, endX, endY;
    private long screenTime;
    private static final String TAG = "Missile";

    Missile(int screenWidth, int screenHeight, long screenTime, final MainActivity mainActivity) {
        this.screenTime = screenTime;
        this.mainActivity = mainActivity;
        this.screenHeight = screenHeight;

        imageView = new ImageView(mainActivity);
        imageView.setImageResource(R.drawable.missile);

        startX = (float) (Math.random() * screenWidth);
        endX = (float) (Math.random() * screenWidth);
        startY = -100;
        endY = (float) (screenHeight * 0.85);

        int w = imageView.getDrawable().getIntrinsicWidth();

        startX -= w * 0.5;
        startY -= w * 0.5;

        double angle = Math.toDegrees(Math.atan2(endX - startX, endY - startY));
        // Keep angle between 0 and 360
        angle = angle + Math.ceil(-angle / 360) * 360;
        float rAngle = (float) (190.0f - angle);

        imageView.setX(startX);
        imageView.setY(startY);
        imageView.setZ(-10);
        imageView.setRotation(rAngle);

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.getLayout().addView(imageView);
            }
        });

        createObjectAnimators();
    }

    private void createObjectAnimators() {
        final ObjectAnimator xAnim = ObjectAnimator.ofFloat(imageView, "x", startX, endX);
        xAnim.setInterpolator(new LinearInterpolator());
        xAnim.setDuration(screenTime);

        final ObjectAnimator yAnim = ObjectAnimator.ofFloat(imageView, "y", startY, endY);
        yAnim.setInterpolator(new LinearInterpolator());
        yAnim.setDuration(screenTime);

        xAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        makeGroundBlast();
                        xAnim.cancel();
                        yAnim.cancel();
                        mainActivity.removeMissile(Missile.this);
                        Log.d(TAG, "run: NUM VIEWS " + mainActivity.getLayout().getChildCount());
                    }
                });
            }
        });

        // WARNING: is called soooo many times. Don't think this is right
//        xAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                mainActivity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (getY() > (screenHeight * 0.85)) {
//                            makeGroundBlast();
//                            xAnim.cancel();
//                            yAnim.cancel();
//                            mainActivity.removeMissile(Missile.this);
//                        }
//                        Log.d(TAG, "run: NUM VIEWS " +
//                                mainActivity.getLayout().getChildCount());
//                    }
//                });
//            }
//        });

        aSet.playTogether(xAnim, yAnim);
    }

    AnimatorSet getAnimatorSet() {
        return aSet;
    }

    private void makeGroundBlast() {
        Log.d(TAG, "makeGroundBlast: ");
        final ImageView explodeImageView = new ImageView(mainActivity);
        explodeImageView.setImageResource(R.drawable.explode);

        int w = explodeImageView.getDrawable().getIntrinsicWidth();
        float offset = (float) (w * 0.5);

        explodeImageView.setX(this.getX() - offset);
        explodeImageView.setY(this.getY() - offset);
        explodeImageView.setZ(-15);

        mainActivity.getLayout().addView(explodeImageView);

        final ObjectAnimator alpha = ObjectAnimator.ofFloat(explodeImageView, "alpha", 1.0f, 0.0f);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(3000);
        alpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.getLayout().removeView(explodeImageView);
            }
        });
        alpha.start();

        // check if the missile hit a base
        mainActivity.applyMissileBlast(explodeImageView.getX(), explodeImageView.getY());
    }

    void interceptorBlast() {
        final ImageView iv = new ImageView(mainActivity);
        iv.setImageResource(R.drawable.explode);

        iv.setTransitionName("Missile Intercepted Blast");

        int w = iv.getDrawable().getIntrinsicWidth();
        float offset = (float) (w * 0.5);

        iv.setX(this.getX() - offset);
        iv.setY(this.getY() - offset);

        aSet.cancel();

        mainActivity.getLayout().removeView(imageView); // remove missile ImageView
        mainActivity.getLayout().addView(iv);   // add missile explosion ImageView

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

    void stop() {
        aSet.pause();
    }

    float getX() {
        return (float) (imageView.getX() + (0.5 * imageView.getDrawable().getIntrinsicWidth()));
    }

    float getY() {
        return (float) (imageView.getY() + (0.5 * imageView.getDrawable().getIntrinsicHeight()));
    }

    ImageView getImageView() {
        return imageView;
    }
}
