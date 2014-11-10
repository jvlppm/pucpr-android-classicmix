package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;

public class Image extends GameEntity {
    private final Alignment alignment;

    public static enum Alignment {
        Center,
        Bottom
    }
    private final Frame message;

    public Image(Frame message, Alignment alignment) {
        this.message = message;
        this.alignment = alignment;
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        switch (alignment) {
            case Center:
                draw(canvas, message, canvas.getWidth() / 2, canvas.getHeight() / 2, 0.5f, 0.5f);
                break;
            case Bottom:
                draw(canvas, message, canvas.getWidth() / 2, canvas.getHeight(), 0.5f, 1);
                break;
        }
    }
}
