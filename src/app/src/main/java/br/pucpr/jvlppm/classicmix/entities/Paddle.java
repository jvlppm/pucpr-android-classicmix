package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Rect;

import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.core.NinePatch;
import br.pucpr.jvlppm.classicmix.services.Assets;
import br.pucpr.jvlppm.classicmix.services.Settings;

public class Paddle extends GameEntity {
    private final boolean useShadows;
    private float x, y;
    private final NinePatch blueTexture, redTexture, shadowTexture;
    private NinePatch texture;
    private final Rect rect;
    private final float MIN_WIDTH;

    private final float height;
    private float width;
    private boolean retracting, playing;
    private float retractionRate;

    public Paddle() {
        Assets assets = Assets.getInstance();
        blueTexture = assets.paddleBlue;
        redTexture = assets.paddleRed;
        shadowTexture = assets.paddleShadow;

        width = blueTexture.width();
        height = blueTexture.height();
        rect = new Rect();
        setPosition(0, 0);
        MIN_WIDTH = blueTexture.leftCenter.rect.width() + blueTexture.rightCenter.rect.width();
        setRetract(false);
        this.useShadows = Settings.Graphics.useShadows();
    }

    @Override
    public void update(GameTime gameTime) {
        super.update(gameTime);
        if (retracting && playing) {
            if (MIN_WIDTH > 30) {
                setWidth(width - gameTime.getElapsedTime() * retractionRate);
                if (width < MIN_WIDTH)
                    width = MIN_WIDTH;
            }
        }
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        super.draw(gameTime, canvas);

        if(useShadows)
            draw(canvas, shadowTexture, x + 2, y + 4);
        draw(canvas, texture, x, y);
    }

    protected void draw(Canvas canvas, NinePatch frame, float x, float y) {
        super.draw(canvas, frame, x, y, 0.5f, 0.5f, width, frame.height());
    }

    public void setWidth(float width) {
        this.width = width;
        updateRectangle();
    }

    public void resetWidth() {
        setWidth(blueTexture.width());
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        updateRectangle();
    }

    private void updateRectangle() {
        rect.set((int)(x - width / 2),
                (int) (y - height / 2),
                (int) (x + width / 2),
                (int) (y + height / 2));
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

    public float getWidth() {
        return width;
    }

    public void setRetract(boolean retracting) {
        if(retracting != this.retracting)
            resetWidth();
        this.retracting = retracting;
        texture = retracting? redTexture : blueTexture;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public void setRetractionRate(float retractionRate) {
        this.retractionRate = retractionRate;
    }
}
