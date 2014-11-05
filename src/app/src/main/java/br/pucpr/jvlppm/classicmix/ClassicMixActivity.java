package br.pucpr.jvlppm.classicmix;

import android.graphics.Canvas;
import android.os.Bundle;

import br.pucpr.jvlppm.classicmix.framework.GameActivity;
import br.pucpr.jvlppm.classicmix.framework.GameTime;

public class ClassicMixActivity extends GameActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void update(GameTime gameTime) {

    }

    @Override
    protected void draw(GameTime gameTime, Canvas canvas) {
        if (gameTime.getTotalTime() < 4)
            canvas.drawARGB(255, 255, 0, 0);
        else if (gameTime.getTotalTime() < 5)
            canvas.drawARGB(255, 255, 255, 0);
        else
            canvas.drawARGB(255, 255, 255, 255);
    }
}
