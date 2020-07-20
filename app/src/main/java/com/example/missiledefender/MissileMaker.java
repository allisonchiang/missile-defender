package com.example.missiledefender;

import android.animation.AnimatorSet;
import android.util.Log;

public class MissileMaker implements Runnable {

    private static final String TAG = "MissileMaker";
    private MainActivity mainActivity;
    private boolean isRunning;
    private int screenWidth, screenHeight;
    private static final int NUM_LEVELS = 5;
    private int count;
    private int missilesPerLevel = 5;
    private int level = 1;
    private long delayBetweenMissiles = NUM_LEVELS * 1000;

    MissileMaker(MainActivity mainActivity, int screenWidth, int screenHeight) {
        this.mainActivity = mainActivity;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    void setRunning(boolean running) {
        isRunning = running;
//        ArrayList<Missile> temp = new ArrayList<>(activeMissiles);
//        for (Missile m : temp) {
//            m.stop();
//        }
    }

    @Override
    public void run() {
        setRunning(true);

        try {
            Thread.sleep((long) (0.5 * delayBetweenMissiles));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while (isRunning) {
            // make missile
            long missileTime = (long) ((delayBetweenMissiles * 0.5) + (Math.random() * delayBetweenMissiles));
            final Missile missile = new Missile(screenWidth, screenHeight, missileTime, mainActivity);
            Log.d(TAG, "run: new missile created");
            mainActivity.addMissile(missile);
            SoundPlayer.getInstance().start("launch_missile");
            final AnimatorSet as = missile.getAnimatorSet();
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    as.start();
                }
            });

            count++;
            if (count > missilesPerLevel) {
                increaseLevel();
                count = 0;
            }

            try {
                Thread.sleep((long) getSleepTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void increaseLevel() {
        level++;
        delayBetweenMissiles -= 500;
        if (delayBetweenMissiles <= 0) {
            delayBetweenMissiles = 1;
        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivity.setLevel(level);
            }
        });
        Log.d(TAG, "increaseLevel: LEVEL " + level);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private double getSleepTime() {
        double num = Math.random();
        if (num < 0.1) {
            return 1;
        } else if (num < 0.2) {
            return (0.5 * delayBetweenMissiles);
        } else {
            return delayBetweenMissiles;
        }
    }
}