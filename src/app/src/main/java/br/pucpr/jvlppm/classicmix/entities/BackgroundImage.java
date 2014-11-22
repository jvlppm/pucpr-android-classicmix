package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.services.Settings;

public class BackgroundImage extends GameEntity {
    private final float FADE_SPEED = 1 / 2f;
    private final float screenRadiusX, screenRadiusY;

    private final Frame frame;

    public final float maxAlpha;
    private final boolean animationsEnabled;

    private final Rect srcRect, destRect;
    private final Paint paint;
    public float alpha;


    private boolean fadeIn, fadeOut;

    public BackgroundImage(Frame frame, int screenWidth, int screenHeight) {
        this.frame = frame;

        this.screenRadiusX = screenWidth / 2;
        this.screenRadiusY = screenHeight / 2;

        this.destRect = new Rect(0, 0, screenWidth, screenHeight);
        this.srcRect = new Rect();

        this.paint = new Paint();
        alpha = maxAlpha = Settings.Graphics.getBackgroundOpacity();
        this.paint.setAlpha((int) (alpha * 255));

        animationsEnabled = Settings.Graphics.areBackgroundAnimationsEnabled();
        setPosition(0.5f, 0.5f, 1);
    }

    public void setPosition(float xRatio, float yRatio, float zoomRatio) {
        float minZoom = Math.max(
                screenRadiusY / (frame.rect.height() / 2),
                screenRadiusX / (frame.rect.width() / 2)
        );
        float maxZoom = minZoom * 1.3f;
        float zoom = (maxZoom - minZoom) * zoomRatio + minZoom;

        float radiusX = screenRadiusX / zoom;
        float radiusY = screenRadiusY / zoom;

        float xMax = frame.rect.width() - radiusX;
        float x = (xMax - radiusX) * xRatio + radiusX;

        float yMax = frame.rect.height() - radiusY;
        float y = (yMax - radiusY) * yRatio + radiusY;

        srcRect.set((int) (x - radiusX), (int) (y - radiusY), (int) (x + radiusX), (int) (y + radiusY));
    }

    @Override
    public void update(GameTime gameTime) {
        if(fadeIn || fadeOut) {
            if (fadeIn) {
                if(animationsEnabled)
                    alpha += gameTime.getElapsedTime() * FADE_SPEED;
                else alpha = maxAlpha;

                if (alpha >= maxAlpha) {
                    alpha = maxAlpha;
                    fadeIn = false;
                }

            } else {
                if(animationsEnabled)
                    alpha -= gameTime.getElapsedTime() * FADE_SPEED;
                else alpha = 0;

                if (alpha <= 0) {
                    alpha = 0;
                    fadeOut = false;
                }
            }
            paint.setAlpha((int) (alpha * 255));
        }
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        canvas.drawBitmap(frame.texture, srcRect, destRect, paint);
    }

    public void fadeIn() {
        fadeIn = true;
        this.alpha = 0;
        paint.setAlpha(0);
    }

    public void fadeOut() {
        fadeOut = true;
    }
}
