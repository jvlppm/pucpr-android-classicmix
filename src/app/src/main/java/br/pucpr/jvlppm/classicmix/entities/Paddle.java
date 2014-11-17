package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Rect;

import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.core.NinePatch;
import br.pucpr.jvlppm.classicmix.services.Assets;

public class Paddle extends GameEntity {
    private float x, y;
    public boolean dragged;
    private final NinePatch texture, shadowTexture;
    private final Assets assets;
    private final Rect rect;
    private final float MIN_WIDTH;

    private final float height;
    private float width;
    private boolean retracting;

    public Paddle() {
        assets = Assets.getInstance();
        texture = assets.paddleBlue;
        shadowTexture = assets.paddleShadow;

        width = texture.width();
        height = texture.height();
        rect = new Rect();
        setPosition(0, 0);
        MIN_WIDTH = texture.leftCenter.rect.width() + texture.rightCenter.rect.width();
    }

    @Override
    public void update(GameTime gameTime) {
        super.update(gameTime);
        if (retracting) {
            if (MIN_WIDTH > 30) {
                setWidth(width - gameTime.getElapsedTime());
                if (width < MIN_WIDTH)
                    width = MIN_WIDTH;

            }
        }
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        super.draw(gameTime, canvas);

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
        setWidth(texture.width());
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

    public void setRetracting(boolean retracting) {
        this.retracting = retracting;
    }
}
