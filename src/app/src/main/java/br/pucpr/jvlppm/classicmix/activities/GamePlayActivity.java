package br.pucpr.jvlppm.classicmix.activities;

import android.os.Bundle;

import br.pucpr.jvlppm.classicmix.core.FrameBuffer;
import br.pucpr.jvlppm.classicmix.core.GameActivity;
import br.pucpr.jvlppm.classicmix.core.Scene;
import br.pucpr.jvlppm.classicmix.core.SimpleFrameBuffer;
import br.pucpr.jvlppm.classicmix.core.ThreadedFrameBuffer;
import br.pucpr.jvlppm.classicmix.screens.GamePlayScreen;
import br.pucpr.jvlppm.classicmix.services.Settings;

public class GamePlayActivity extends GameActivity {
    @Override
    protected FrameBuffer createFrameBuffer()
    {
        if(Settings.Graphics.useThreadedFrameBuffer())
            return new ThreadedFrameBuffer(this, 512, 720);
        return new SimpleFrameBuffer(this, 512, 720);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCurrentScreen(new GamePlayScreen(this, new Scene.FinishListener() {
            @Override
            public void onFinished(Object result) {
                finish();
            }
        }));
    }

    @Override
    public void onBackPressed() {
        keepMusic = true;
        super.onBackPressed();
    }
}
