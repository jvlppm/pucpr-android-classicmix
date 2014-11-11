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
        draw(canvas, frames.get(frameIndex), x, y, 0.5f, 0.5f);
        frameTimeCount += gameTime.getElapsedTime();
        if(!destroy && frameIndex < frames.size() - 1 && frameTimeCount > 0.1f) {
            frameIndex++;
            frameTimeCount = 0;
        }
        super.draw(gameTime, canvas);
    }

    public void onHit() {
        frameIndex = 0;
        frameTimeCount = 0;
        destroy = true;
    }

    public boolean destroyed() {
        return destroy && frameTimeCount > 0.03f;
    }

    public void reset() {
        destroy = false;
        frameTimeCount = 0;
        frameIndex = 0;
    }
}
