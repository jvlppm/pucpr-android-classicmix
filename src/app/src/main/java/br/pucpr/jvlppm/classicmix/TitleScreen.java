package br.pucpr.jvlppm.classicmix;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class TitleScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);
    }

    public void onClicksettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onClickPlay(View view) {
        Intent intent = new Intent(this, ClassicMixActivity.class);
        startActivity(intent);
    }
}
