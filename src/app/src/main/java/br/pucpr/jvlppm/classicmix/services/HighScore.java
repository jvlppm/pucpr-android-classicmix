package br.pucpr.jvlppm.classicmix.services;

import android.content.Context;

public class HighScore {
    private static HighScore instance;

    public static synchronized HighScore getInstance() {
        if(instance == null)
            throw new UnsupportedOperationException("HighScore not initialized");
        return instance;
    }

    private HighScore(Context context) {
        this.context = context;
    }

    public static void init(Context context) {
        instance = new HighScore(context);
        instance.highScore = context.getSharedPreferences("HighScore", 0).getInt("highScore", 0);
    }

    private Context context;
    private int highScore;

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
        context.getSharedPreferences("HighScore", 0)
                .edit()
                .putInt("highScore", highScore)
                .commit();
    }
}
