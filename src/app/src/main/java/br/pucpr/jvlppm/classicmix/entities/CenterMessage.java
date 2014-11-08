package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;

public class CenterMessage extends GameEntity {
    private final Frame message;

    public CenterMessage(Frame message) {
        this.message = message;
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        draw(canvas, message, canvas.getWidth() / 2, canvas.getHeight() / 2, 0.5f, 0.5f);
    }
}
