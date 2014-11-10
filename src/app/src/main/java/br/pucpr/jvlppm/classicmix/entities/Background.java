package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.services.Settings;

public class Background extends GameEntity {
    private static enum AnimationState {GOING_RIGHT, ZOOM_IN, GOING_LEFT_ZOOMED, ZOOM_OUT}

    private final Frame frame;
    private AnimationState state;

    private final float moveSpeed, zoomInSpeed, zoomOutSpeed, fadeSpeed;
    private final float screenRadiusX, screenRadiusY;
    private final Rect srcRect, destRect;
    private final Paint paint;
    public float alpha = 1;
    public final float maxAlpha;
    private final boolean animationsEnabled;
    private boolean fadeIn, fadeOut;

    private float xRatio, yRatio, zoomRatio;

    public Background(Frame frame, int screenWidth, int screenHeight) {
        this.frame = frame;

        this.moveSpeed = 1 / 120f;
        this.zoomInSpeed = 1 / 10f;
        this.zoomOutSpeed = 1 / 2f;
        this.fadeSpeed = 1 / 2f;

        this.screenRadiusX = screenWidth / 2;
        this.screenRadiusY = screenHeight / 2;
        this.state = AnimationState.GOING_RIGHT;
        this.destRect = new Rect(0, 0, screenWidth, screenHeight);
        this.srcRect = new Rect();

        this.xRatio = 0;
        this.yRatio = 0.5f;
        this.zoomRatio = 0;
        this.paint = new Paint();
        alpha = maxAlpha = Settings.Graphics.getBackgroundOpacity();
        this.paint.setAlpha((int) (alpha * 255));
        updateRect();

        animationsEnabled = Settings.Graphics.areBackgroundAnimationsEnabled();
        if (!animationsEnabled)
            setPosition(0.5f, 0.5f, 1);
    }

    public void setPosition(float xRatio, float yRatio, float zoomRatio) {
        this.xRatio = xRatio;
        this.yRatio = yRatio;
        this.zoomRatio = zoomRatio;
        updateRect();
    }

    private void updateRect() {
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
                    alpha += gameTime.getElapsedTime() * fadeSpeed;
                else alpha = maxAlpha;

                if (alpha >= maxAlpha) {
                    alpha = maxAlpha;
                    fadeIn = false;
                }

            } else {
                if(animationsEnabled)
                    alpha -= gameTime.getElapsedTime() * fadeSpeed;
                else alpha = 0;

                if (alpha <= 0) {
                    alpha = 0;
                    fadeOut = false;
                }
            }
            paint.setAlpha((int) (alpha * 255));
        }
        if(!animationsEnabled)
            return;

        switch (state) {
            case GOING_RIGHT:
                xRatio += gameTime.getElapsedTime() * moveSpeed;
                if (xRatio >= 1) {
                    xRatio = 1;
                    state = AnimationState.ZOOM_IN;
                }
                break;
            case ZOOM_IN:
                zoomRatio += gameTime.getElapsedTime() * zoomInSpeed;
                if (zoomRatio >= 1) {
                    zoomRatio = 1;
                    state = AnimationState.GOING_LEFT_ZOOMED;
                }
                break;
            case GOING_LEFT_ZOOMED:
                xRatio -= gameTime.getElapsedTime() * moveSpeed;
                if (xRatio <= 0) {
                    xRatio = 0;
                    state = AnimationState.ZOOM_OUT;
                }
                break;
            case ZOOM_OUT:
                zoomRatio -= gameTime.getElapsedTime() * zoomOutSpeed;
                if (zoomRatio <= 0) {
                    zoomRatio = 0;
                    state = AnimationState.GOING_RIGHT;
                }
                break;
        }
        updateRect();
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        canvas.drawBitmap(frame.texture, srcRect, destRect, paint);
    }

    public void fadeIn(Background oldBackground) {
        fadeIn = true;
        this.alpha = 0;
        paint.setAlpha(0);
        state = oldBackground.state;
        zoomRatio = oldBackground.zoomRatio;
        xRatio = oldBackground.xRatio;
        yRatio = oldBackground.yRatio;
    }

    public void fadeOut() {
        fadeOut = true;
    }
}
