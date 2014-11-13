package br.pucpr.jvlppm.classicmix.activities;

import android.os.Bundle;

import br.pucpr.jvlppm.classicmix.core.GameActivity;
import br.pucpr.jvlppm.classicmix.core.GameView;
import br.pucpr.jvlppm.classicmix.core.Scene;
import br.pucpr.jvlppm.classicmix.screens.GamePlayScreen;

public class GamePlayActivity extends GameActivity {
    @Override
    protected GameView createGameView() {
        return new GameView(this, 512, 720);
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
