package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;

import java.util.List;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.services.Assets;

public class Laser extends GameEntity {
    private final List<Frame> frames;
    private int frameIndex;
    private float frameTimeCount = 0;
    public float x, y;
    public final float speed = 500;
    private boolean destroy;
    public float strength;

    public Laser() {
        frames = Assets.getInstance().laser;
    }

    @Override
    public void update(GameTime gameTime) {
        if(!destroy)
            y -= gameTime.getElapsedTime() * speed;
        super.update(gameTime);
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        float scale = 1 + (strength - 1) / 3;
        draw(canvas, frames.get(frameIndex), x, y, 0.5f, 0.5f, scale);
        frameTimeCount += gameTime.getElapsedTime();
        if(!destroy && frameIndex < frames.size() - 1 && frameTimeCount > 0.1f) {
            frameIndex++;
            frameTimeCount = 0;
        }
        super.draw(gameTime, canvas);
    }

    public void onHit() {
        destroy = true;
    }

    public boolean destroyed() {
        return destroy;
    }

    public void reset() {
        destroy = false;
        frameTimeCount = 0;
        frameIndex = 0;
    }
}
