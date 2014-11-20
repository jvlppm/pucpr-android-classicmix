package br.pucpr.jvlppm.classicmix.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import br.pucpr.jvlppm.classicmix.R;
import br.pucpr.jvlppm.classicmix.services.HighScore;
import br.pucpr.jvlppm.classicmix.services.Settings;

public class HighscoreActivity extends Activity {

    private final MusicController musicController = new MusicController(true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);
        loadScores();
    }

    private void loadScores() {
        HighScore highScore = HighScore.getInstance();

        ((TextView)findViewById(R.id.highscore_easy_value))
                .setText("" + highScore.getHighScore(Settings.Gameplay.Difficulty.Easy));
        ((TextView)findViewById(R.id.highscore_medium_value))
                .setText("" + highScore.getHighScore(Settings.Gameplay.Difficulty.Medium));
        ((TextView)findViewById(R.id.highscore_hard_value))
                .setText("" + highScore.getHighScore(Settings.Gameplay.Difficulty.Hard));
    }

    @Override
    protected void onPause() {
        musicController.pause(isFinishing());
        super.onPause();
    }

    @Override
    protected void onResume() {
        musicController.resume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        musicController.destroy(isFinishing());
        super.onDestroy();
    }
}
