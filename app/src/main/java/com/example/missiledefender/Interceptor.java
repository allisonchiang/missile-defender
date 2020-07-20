package com.example.missiledefender;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

class Interceptor {

    private static final String TAG = "Interceptor";
    private MainActivity mainActivity;
    private ImageView imageView;
    private ObjectAnimator xAnim, yAnim;
    static final int INTERCEPTOR_BLAST = 180;

    Interceptor(MainActivity ma, Base b, float touchX, float touchY) {
        this.mainActivity = ma;

        float endX = touchX;
        float endY = touchY;

        imageView = new ImageView(mainActivity);
        imageView.setImageResource(R.drawable.interceptor);

        float startX = b.getX();
        float startY = MainActivity.screenHeight - 70;

        int w = imageView.getDrawable().getIntrinsicWidth();
        endX -= w * 0.5;
        endY -= w * 0.5;

        double angle = Math.toDegrees(Math.atan2(endX - startX, endY - startY));
        // Keep angle between 0 and 360
        angle = angle + Math.ceil(-angle / 360) * 360;
        float rAngle = (float) (190.0f - angle);

        imageView.setX(startX);
        imageView.setY(startY);
        imageView.setZ(-10);
        imageView.setRotation(rAngle);

        mainActivity.getLayout().addView(imageView);

        double distance = Math.hypot((endY - imageView.getY()), endX - imageView.getX());

        xAnim = ObjectAnimator.ofFloat(imageView, "x", startX, endX);
        xAnim.setInterpolator(new AccelerateInterpolator());
        xAnim.setDuration((long) distance * 2);

        yAnim = ObjectAnimator.ofFloat(imageView, "y", startY, endY);
        yAnim.setInterpolator(new AccelerateInterpolator());
        yAnim.setDuration((long) distance * 2);

        xAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.getLayout().removeView(imageView);
                        MainActivity.interceptorCount--;
                        makeBlast();
                    }
                });
            }
        });
    }

    private void makeBlast() {
        SoundPlayer.getInstance().start("interceptor_blast");
        final ImageView iv = new ImageView(mainActivity);
        iv.setImageResource(R.drawable.i_explode);
        iv.setTransitionName("Interceptor blast");

        float w = iv.getDrawable().getIntrinsicWidth();
        Log.d(TAG, "makeBlast: width " + w);
        iv.setX(this.getX() - (w/2));
        iv.setY(this.getY() - (w/2));
        iv.setZ(-15);

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

        // checks if interceptor hit missile
        mainActivity.applyInterceptorBlast(this, iv.getId());
    }

    // starts the interceptor's animations. called from MainActivity
    void launch() {
        xAnim.start();
        yAnim.start();
    }

    // returns center x of interceptor ImageView
    float getX() {
        int w = imageView.getDrawable().getIntrinsicWidth() / 2;
        return imageView.getX() + w;
    }

    // returns center y of interceptor ImageView
    float getY() {
        int w = imageView.getDrawable().getIntrinsicHeight() / 2;
        return imageView.getY() + w;
    }
}