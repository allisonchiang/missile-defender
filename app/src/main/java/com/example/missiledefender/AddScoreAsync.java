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
import java.util.Locale;

public class AddScoreAsync extends AsyncTask<String, Void, String> {

    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

    @SuppressLint("StaticFieldLeak")
    private MainActivity context;
    private Connection conn;
    private static final String TAG = "AddScoreAsync";
    private static final String SCORE_TABLE = "AppScores";

    AddScoreAsync(MainActivity ctx) {
        context = ctx;
    }

    protected String doInBackground(String... values) {
        String dbURL = "jdbc:mysql://christopherhield.com:3306/chri5558_missile_defense";
        String initials = values[0];
        int score = Integer.parseInt(values[1]);
        int level = Integer.parseInt(values[2]);

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(dbURL, "chri5558_student", "ABC.123");

            StringBuilder sb = new StringBuilder();
            String response = addScore(initials, score, level);
            sb.append(response);
            return sb.toString();
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
        context.getTopScores(true);
    }

    private String addScore(String initials, int score, int level) throws SQLException {
        Log.d(TAG, "addScore: ");
        
        Statement stmt = conn.createStatement();

        String sql = "insert into " + SCORE_TABLE + " values (" +
                System.currentTimeMillis() + ", '" + initials + "', " + score + ", " + level + ")";

        int result = stmt.executeUpdate(sql);

        stmt.close();

        return "Player " + initials + " added (" + result + " record)\n\n";
    }
}