package br.pucpr.jvlppm.classicmix.activities;

import android.app.Activity;
import android.content.Intent;

import br.pucpr.jvlppm.classicmix.services.Sound;

public class SoundActivity extends Activity {
    protected boolean keepMusic;

    @Override
    protected void onPause() {
        super.onPause();
        if(!keepMusic)
            Sound.getInstance().pause();
        else if (!isFinishing())
            keepMusic = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sound.getInstance().resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isFinishing() && !keepMusic)
            Sound.getInstance().stop();
    }

    @Override
    public void startActivity(Intent intent) {
        keepMusic = true;
        super.startActivity(intent);
    }
}
