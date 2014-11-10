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
    }

    private Context context;

    public int getHighScore(Settings.Gameplay.Difficulty difficulty) {
        return context.getSharedPreferences(difficulty.name(), 0)
                .getInt("highScore", 0);
    }

    public void setHighScore(Settings.Gameplay.Difficulty difficulty, int highScore) {
        context.getSharedPreferences(difficulty.name(), 0)
                .edit()
                .putInt("highScore", highScore)
                .commit();
    }
}
