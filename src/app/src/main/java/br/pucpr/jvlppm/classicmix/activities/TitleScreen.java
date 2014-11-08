package br.pucpr.jvlppm.classicmix.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import br.pucpr.jvlppm.classicmix.services.Assets;
import br.pucpr.jvlppm.classicmix.R;

public class TitleScreen extends Activity implements Assets.LoadListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);
        Assets.getInstance().loadAssetsAsync(this, this);
    }

    public void onClicksettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onClickPlay(View view) {
        Intent intent = new Intent(this, ClassicMixActivity.class);
        startActivity(intent);
    }

    @Override
    public void onLoadCompleted() {
        ProgressBar progress = (ProgressBar)findViewById(R.id.loadProgress);
        progress.setVisibility(View.GONE);
        Button playButton = (Button)findViewById(R.id.playButton);
        playButton.setVisibility(View.VISIBLE);
    }
}
