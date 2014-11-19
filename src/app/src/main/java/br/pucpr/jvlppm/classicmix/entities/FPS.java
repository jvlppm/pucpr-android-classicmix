package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import br.pucpr.jvlppm.classicmix.core.GameTime;

public class FPS extends GameEntity {
    Paint paint = new Paint();
    float elapsedTime = 0;
    float calcFPS = 0.0f;
    final float fps = 0.5f;

    public FPS() {
        paint.setTextSize(20);
        paint.setColor(Color.WHITE);
        paint.setShadowLayer(2, 2, 2, Color.BLACK);
        paint.setFakeBoldText(true);
    }

    @Override
    public void update(GameTime gameTime) {
        elapsedTime += gameTime.getElapsedTime();
        if (elapsedTime >= fps) {
            elapsedTime = 0;
            calcFPS = Math.round(1 / gameTime.getElapsedTime());
        }
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        canvas.drawText("FPS: " + calcFPS, 10, 20, paint);
    }
}
