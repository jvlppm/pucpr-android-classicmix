package br.pucpr.jvlppm.classicmix;

import android.os.Bundle;

import br.pucpr.jvlppm.classicmix.core.GameActivity;
import br.pucpr.jvlppm.classicmix.core.GameScreen;
import br.pucpr.jvlppm.classicmix.core.GameView;

public class ClassicMixActivity extends GameActivity {
    @Override
    protected GameView createGameView() {
        return new GameView(this, 512, 720);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCurrentScreen(new GamePlayScreen(this, new GameScreen.FinishListener() {
            @Override
            public void onFinished(Object result) {
                finish();
            }
        }));
    }
}
