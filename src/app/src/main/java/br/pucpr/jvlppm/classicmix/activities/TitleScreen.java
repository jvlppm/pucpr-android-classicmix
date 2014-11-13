package br.pucpr.jvlppm.classicmix.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import br.pucpr.jvlppm.classicmix.R;
import br.pucpr.jvlppm.classicmix.services.Assets;
import br.pucpr.jvlppm.classicmix.services.Sound;

public class TitleScreen extends SoundActivity implements Assets.LoadListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);
        Assets.getInstance().loadAssetsAsync(this, this);
        Sound.getInstance().playLevelMusic(0);
    }

    public void onClickSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onClickHighscore(View view) {
        Intent intent = new Intent(this, HighscoreActivity.class);
        startActivity(intent);
    }

    public void onClickPlay(View view) {
        Intent intent = new Intent(this, GamePlayActivity.class);
        startActivity(intent);
    }

    @Override
    public void onLoadCompleted() {
        ProgressBar progress = (ProgressBar)findViewById(R.id.load_progress);
        progress.setVisibility(View.GONE);
        Button playButton = (Button)findViewById(R.id.play_button);
        playButton.setVisibility(View.VISIBLE);
    }
}
