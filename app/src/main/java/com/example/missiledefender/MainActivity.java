package com.example.missiledefender;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import static android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ConstraintLayout layout;
    public static int screenHeight;
    public static int screenWidth;
    public static int interceptorCount = 0;
    private int MAX_ITERCEPTORS = 3;
    private MissileMaker missileMaker;
    private ArrayList<Missile> activeMissiles = new ArrayList<>();
    private ArrayList<Base> activeBases = new ArrayList<>();
    private int scoreValue;
    private TextView score, level;
    private ImageView baseImageView1, baseImageView2, baseImageView3, gameOverText;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.layout);
        score = findViewById(R.id.scoreText);
        level = findViewById(R.id.levelText);
        gameOverText = findViewById(R.id.gameOver);
        gameOverText.setVisibility(GONE);
        baseImageView1 = findViewById(R.id.base1);
        baseImageView2 = findViewById(R.id.base2);
        baseImageView3 = findViewById(R.id.base3);

        setLevel(1);
        getScreenDimensions();
        setupFullScreen();

        // pre-load sounds
        SoundPlayer.getInstance().setupSound(this, "base_blast", R.raw.base_blast);
        SoundPlayer.getInstance().setupSound(this, "interceptor_blast", R.raw.interceptor_blast);
        SoundPlayer.getInstance().setupSound(this, "interceptor_hit_missile", R.raw.interceptor_hit_missile);
        SoundPlayer.getInstance().setupSound(this, "launch_interceptor", R.raw.launch_interceptor);
        SoundPlayer.getInstance().setupSound(this, "launch_missile", R.raw.launch_missile);

        // create/start clouds
        new ScrollingClouds(this, layout, R.drawable.clouds, 30000);

        // make the layout touch-sensitive
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    handleTouch(motionEvent.getX(), motionEvent.getY());
                }
                return false;
            }
        });

        baseImageView1.getViewTreeObserver().addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Layout has happened here.
                    Base base1 = new Base(baseImageView1, MainActivity.this);
                    Log.d(TAG, "onCreate: BASE1 (" + base1.getX() + ", " + base1.getY() + ")");
                    activeBases.add(base1);
                    // Don't forget to remove your listener when you are done with it.
                    baseImageView1.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        );

        baseImageView2.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Layout has happened here.
                        Base base2 = new Base(baseImageView2, MainActivity.this);
                        Log.d(TAG, "onCreate: BASE2 (" + base2.getX() + ", " + base2.getY() + ")");
                        activeBases.add(base2);
                        // Don't forget to remove your listener when you are done with it.
                        baseImageView2.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );

        baseImageView3.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        // Layout has happened here.
                        Base base3 = new Base(baseImageView3, MainActivity.this);
                        Log.d(TAG, "onCreate: BASE3 (" + base3.getX() + ", " + base3.getY() + ")");
                        activeBases.add(base3);
                        // Don't forget to remove your listener when you are done with it.
                        baseImageView3.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
        );

        // create and start MissileMaker in its own thread
        missileMaker = new MissileMaker(this, screenWidth, screenHeight);
        new Thread(missileMaker).start();

        // get the top 10 scores at beginning of game so they are ready to be used when game ends
        TopScoresAsync topScoresAsync = new TopScoresAsync(this, false);
        topScoresAsync.execute();
    }

    private void setupFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public ConstraintLayout getLayout() {
        return layout;
    }

    private void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels + getBarHeight();   // add bar height to width
        Log.d(TAG, "getScreenDimensions: HEIGHT " + screenHeight);  // 1080
        Log.d(TAG, "getScreenDimensions: WIDTH " + screenWidth);    // 1920
    }

    private int getBarHeight() {
        int resourceId = this.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return this.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public void setLevel(final int value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                level.setText(String.format(Locale.getDefault(), "Level %d", value));
            }
        });
    }

    public void addMissile(Missile m) {
        activeMissiles.add(m);
    }

    // only used by Missile class when missile hits ground
    public void removeMissile(Missile m) {
        this.getLayout().removeView(m.getImageView());
        activeMissiles.remove(m);
    }

    // checks if missile hit a base, called by missile's makeGroundBlast() method
    public void applyMissileBlast(float x1, float y1) {
        HashMap<Double, Base> baseSet = new HashMap<>();
        for (Base b : activeBases) {
            float baseX = b.getX();
            float baseY = b.getY();
            double distance = Math.hypot(x1 - baseX, y1 - baseY);
            baseSet.put(distance, b);
        }
        if (!baseSet.isEmpty()) {
            double minDistance = Collections.min(baseSet.keySet());
            Base launcherBase = baseSet.get(minDistance);
            if (minDistance < 250) {
                launcherBase.destruct();
                activeBases.remove(launcherBase);
                if (activeBases.isEmpty()) {
                    endGame();
                }
            }
        } else {
            endGame();
        }
    }

    public void applyInterceptorBlast(Interceptor interceptor, int id) {
        Log.d(TAG, "applyInterceptorBlast: -------------------------- " + id);

        float x1 = interceptor.getX();
        float y1 = interceptor.getY();

        Log.d(TAG, "applyInterceptorBlast: INTERCEPTOR: " + x1 + ", " + y1);

        ArrayList<Missile> nowGoneMissile = new ArrayList<>();
        ArrayList<Missile> temp = new ArrayList<>(activeMissiles);

        // for each missile, calculate the distance from the interceptor to the missile
        for (Missile m : temp) {
            float x2 = m.getX();
            float y2 = m.getY();
            Log.d(TAG, "applyInterceptorBlast:    Missile: " + x2 + ", " + y2);

            double distance = Math.hypot(x2 - x1, y2 - y1);
            Log.d(TAG, "applyInterceptorBlast:    DIST: " + distance);

            if (distance < 120) {
                // increment score
                scoreValue++;
                score.setText(String.format(Locale.getDefault(), "%d", scoreValue));

                // play sound
                SoundPlayer.getInstance().start("interceptor_hit_missile");
                Log.d(TAG, "applyInterceptorBlast:    Hit: " + distance);
                m.interceptorBlast();
                nowGoneMissile.add(m);
            }

            Log.d(TAG, "applyInterceptorBlast: --------------------------");
        }

        for (Missile m : nowGoneMissile) {
            activeMissiles.remove(m);
            this.getLayout().removeView(m.getImageView());
        }


        ArrayList<Base> nowGoneBase = new ArrayList<>();
        ArrayList<Base> tempBase = new ArrayList<>(activeBases);
        // for each base, calculate the distance from the interceptor to the missile
        for (Base b : tempBase) {
            float x2 = b.getX();
            float y2 = b.getY();
            Log.d(TAG, "applyInterceptorBlast:    Base: " + x2 + ", " + y2);

            double distance = Math.hypot(x2 - x1, y2 - y1);
            Log.d(TAG, "applyInterceptorBlast:    DIST: " + distance);

            // If a Base is within the blast radius of the interceptor, the base will be destroyed.
            if (distance < 100) {
                // play sound
                SoundPlayer.getInstance().start("base_blast");
                Log.d(TAG, "applyInterceptorBlast:    Hit: " + distance);
                b.destruct();
                nowGoneBase.add(b);
            }
            Log.d(TAG, "applyInterceptorBlast: --------------------------");
        }

        for (Base b : nowGoneBase) {
            activeBases.remove(b);
            if (activeBases.isEmpty()) {
                endGame();
            }
        }
    }

    // handle user's screen touches
    public void handleTouch(float x1, float y1) {
        if (!activeBases.isEmpty() && interceptorCount < MAX_ITERCEPTORS) {
            HashMap<Double, Base> baseSet = new HashMap<>();
            for (Base b : activeBases) {
                // get center coordinates of base
                float baseX = b.getX();
                float baseY = b.getY();
                double distance = Math.hypot(x1 - baseX, y1 - baseY);
                baseSet.put(distance, b);
            }
            double minDistance = Collections.min(baseSet.keySet());
            Base launcherBase = baseSet.get(minDistance);

            launchInterceptor(launcherBase, x1, y1);
        }
    }

    private void launchInterceptor(Base b, float touchX, float touchY) {
        Interceptor i = new Interceptor(this, b, touchX, touchY);
        interceptorCount++;
        SoundPlayer.getInstance().start("launch_interceptor");
        i.launch();
    }

    public void endGame() {
        Log.d(TAG, "endGame: ");
        missileMaker.setRunning(false);
        ArrayList<Missile> temp = new ArrayList<>(activeMissiles);
        for (Missile m : temp) {
            m.stop();
        }

        gameOverText.setVisibility(VISIBLE);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f) ;
        gameOverText.startAnimation(fadeIn);
        fadeIn.setDuration(3000);
        fadeIn.setFillAfter(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                checkScores();
            }
        }, 3000);
    }

    private void checkScores() {
        final String levelString = level.getText().toString().substring(6);

        int lowestTopScore = Collections.min(TopScoresAsync.topScoresList);

        if (scoreValue > lowestTopScore) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("You are a Top-Player!");
            builder.setMessage("Please enter your initials (up to 3 characters):");
            final EditText input = new EditText(this);
            input.setGravity(Gravity.CENTER_HORIZONTAL);
            input.setInputType(InputType.TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_CAP_CHARACTERS);
            input.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
            input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(3)});
            builder.setView(input);

            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AddScoreAsync addScoreAsync = new AddScoreAsync(MainActivity.this);
                            addScoreAsync.execute(input.getText().toString().toUpperCase(), score.getText().toString(), levelString);
                        }
                    });
            builder.setNegativeButton("CANCEL",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            
        } else {
            getTopScores(true);
        }
    }

    public void getTopScores(boolean gameEnded) {
        TopScoresAsync topScoresAsync = new TopScoresAsync(this, gameEnded);
        topScoresAsync.execute();
    }

    public void showTopScores() {
        Log.d(TAG, "showTopScores: ");
        Intent i = new Intent(MainActivity.this, ScoreActivity.class);
        startActivity(i);

        // close this activity
        finish();
    }
}