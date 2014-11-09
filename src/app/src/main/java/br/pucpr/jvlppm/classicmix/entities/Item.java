package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Rect;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;

public class Item extends GameEntity {
    public final Frame frame;
    private float x;
    private float y;
    private final Rect rect;
    private final float radiusX, radiusY;

    public Item(Frame frame, float x, float y) {
        this.frame = frame;
        rect = new Rect();
        radiusX = frame.rect.width() / 2;
        radiusY = frame.rect.height() / 2;
        setPosition(x, y);
    }

    void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        rect.set((int)(x - radiusX),
                 (int)(y - radiusY),
                 (int)(x + radiusX),
                 (int)(y + radiusY));
    }

    @Override
    public void update(GameTime gameTime) {
        super.update(gameTime);
        setPosition(x, y + gameTime.getElapsedTime() * 150);
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        draw(canvas, frame, x, y, 0.5f, 0.5f);
    }

    public void getRect(Rect dest) {
        dest.set(rect);
    }
}
