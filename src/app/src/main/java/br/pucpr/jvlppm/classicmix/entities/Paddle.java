package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Rect;

import br.pucpr.jvlppm.classicmix.services.Assets;
import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;

public class Paddle extends GameEntity {
    private float x, y;
    public boolean dragged;
    private final Assets assets;
    private final Rect rect;
    private final float radiusX, radiusY;

    public Paddle() {
        assets = Assets.getInstance();
        radiusX = assets.paddleBlue.texture.getWidth() / 2;
        radiusY = assets.paddleBlue.texture.getHeight() / 2;
        rect = new Rect((int)-radiusX, (int)-radiusY, (int)radiusX, (int)radiusY);
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        super.draw(gameTime, canvas);

        Frame paddle = dragged ? assets.paddleRed : assets.paddleBlue;
        draw(canvas, assets.paddleShadow, x + 2, y + 4, 0.5f, 0.5f);
        draw(canvas, paddle, x, y, 0.5f, 0.5f);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        rect.set((int)(x - radiusX),
                 (int)(y - radiusY),
                 (int)(x + radiusX),
                 (int)(y + radiusY));
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void getRect(Rect dest) {
        dest.set(rect);
    }
}
