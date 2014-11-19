package br.pucpr.jvlppm.classicmix.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_highscore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
