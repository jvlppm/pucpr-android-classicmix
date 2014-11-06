package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;

import br.pucpr.jvlppm.classicmix.Assets;
import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;

public class Paddle extends GameEntity {
    public float x, y;
    public boolean dragged;
    Assets assets;

    public Paddle() {
        assets = Assets.getInstance();
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        super.draw(gameTime, canvas);

        Frame paddle = dragged? assets.paddleRed : assets.paddleBlue;
        draw(canvas, assets.paddleShadow, x + 2, y + 4, 0.5f, 0.5f);
        draw(canvas, paddle, x, y, 0.5f, 0.5f);
    }
}
