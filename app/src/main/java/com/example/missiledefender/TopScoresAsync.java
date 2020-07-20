package com.example.missiledefender;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TopScoresAsync extends AsyncTask<String, Void, String> {

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    @SuppressLint("StaticFieldLeak")
    private MainActivity context;
    private boolean gameEndedBoolean;

    private Connection conn;
    private static final String TAG = "TopScoresAsync";
    private static final String SCORE_TABLE = "AppScores";
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
    static ArrayList<Integer> topScoresList = new ArrayList<>();
    static String topScoreString;

    TopScoresAsync(MainActivity ctx, boolean gameEnded) {
        context = ctx;
        gameEndedBoolean = gameEnded;
    }

    protected String doInBackground(String... values) {
        String dbURL = "jdbc:mysql://christopherhield.com:3306/chri5558_missile_defense";
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(dbURL, "chri5558_student", "ABC.123");

            return getAll();
        } catch (Exception e) {
            Log.d(TAG, "doInBackground: Exception");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Log.d(TAG, "onPostExecute: " + s);
        topScoreString = s;

        // If game has ended, show the scores after completion. Otherwise, do nothing.
        if (gameEndedBoolean) {
            context.showTopScores();
        }
    }

    private String getAll() throws SQLException {
        Log.d(TAG, "getAll: ");
        
        Statement stmt = conn.createStatement();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.getDefault(),
                "%3s %6s %7s %6s %14s%n", "#", "Init", "Level", "Score", "Date/Time"));

        String sql = "SELECT * FROM " + SCORE_TABLE + " ORDER BY Score DESC LIMIT 10";
//        String sql = "SELECT * FROM " + SCORE_TABLE + " WHERE INITIALS = 'AC' ORDER BY Score DESC LIMIT 10";
        ResultSet rs = stmt.executeQuery(sql);

        int place = 1;
        while (rs.next()) {
            long millis = rs.getLong(1);
            String initial = rs.getString(2);
            int score = rs.getInt(3);
            int level = rs.getInt(4);

            sb.append(String.format(Locale.getDefault(),
                    "%3s %6s %7s %6s %14s%n", place, initial, level, score, sdf.format(new Date(millis))));

            topScoresList.add(score);
            place++;
        }
        rs.close();
        stmt.close();

        return sb.toString();
    }
}