package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;

import java.util.List;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.services.Assets;
import br.pucpr.jvlppm.classicmix.services.Settings;

public class BackgroundManager extends GameEntity {
    private static enum AnimationState {GOING_RIGHT, ZOOM_IN, GOING_LEFT_ZOOMED, ZOOM_OUT}

    private final float MOVE_SPEED = 1 / 120f;
    private final float ZOOM_IN_SPEED = 1 / 10f;
    private final float ZOOM_OUT_SPEED = 1 / 2f;

    private final int width;
    private final int height;
    private final boolean animationsEnabled;

    private BackgroundImage currentBackgroundImage;
    private BackgroundImage oldBackgroundImage;
    private final float backgroundOpacity;

    private AnimationState state;
    private float xRatio, yRatio, zoomRatio;

    public BackgroundManager(int width, int height) {
        this.width = width;
        this.height = height;
        this.backgroundOpacity = Settings.Graphics.getBackgroundOpacity();

        this.state = AnimationState.GOING_RIGHT;
        this.xRatio = 0;
        this.yRatio = 0.5f;
        this.zoomRatio = 0;

        this.animationsEnabled = Settings.Graphics.areBackgroundAnimationsEnabled();
    }

    public void set(int level) {
        if (backgroundOpacity <= 0)
            return;

        List<Frame> backgrounds = Assets.getInstance().background;
        if(level >= backgrounds.size())
            level = 0;
        Frame backgroundImage = backgrounds.get(level);
        if (backgroundImage != null) {
            BackgroundImage background = new BackgroundImage(
                    backgroundImage,
                    width, height);

            if(currentBackgroundImage != null) {
                oldBackgroundImage = currentBackgroundImage;
                background.fadeIn();
                oldBackgroundImage.fadeOut();
            }

            currentBackgroundImage = background;
        }
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        if(oldBackgroundImage != null)
            oldBackgroundImage.draw(gameTime, canvas);
        if(currentBackgroundImage != null)
            currentBackgroundImage.draw(gameTime, canvas);
        super.draw(gameTime, canvas);
    }

    @Override
    public void update(GameTime gameTime) {
        if(currentBackgroundImage == null && oldBackgroundImage == null)
            return;

        if(animationsEnabled)
            updateAnimation(gameTime);

        if(oldBackgroundImage != null) {
            oldBackgroundImage.update(gameTime);
            if(oldBackgroundImage.alpha <= 0)
                oldBackgroundImage = null;
        }
        if(currentBackgroundImage != null)
            currentBackgroundImage.update(gameTime);
    }

    private void updateAnimation(GameTime gameTime) {
        switch (state) {
            case GOING_RIGHT:
                xRatio += gameTime.getElapsedTime() * MOVE_SPEED;
                if (xRatio >= 1) {
                    xRatio = 1;
                    state = AnimationState.ZOOM_IN;
                }
                break;
            case ZOOM_IN:
                zoomRatio += gameTime.getElapsedTime() * ZOOM_IN_SPEED;
                if (zoomRatio >= 1) {
                    zoomRatio = 1;
                    state = AnimationState.GOING_LEFT_ZOOMED;
                }
                break;
            case GOING_LEFT_ZOOMED:
                xRatio -= gameTime.getElapsedTime() * MOVE_SPEED;
                if (xRatio <= 0) {
                    xRatio = 0;
                    state = AnimationState.ZOOM_OUT;
                }
                break;
            case ZOOM_OUT:
                zoomRatio -= gameTime.getElapsedTime() * ZOOM_OUT_SPEED;
                if (zoomRatio <= 0) {
                    zoomRatio = 0;
                    state = AnimationState.GOING_RIGHT;
                }
                break;
        }
        if(currentBackgroundImage != null)
            currentBackgroundImage.setPosition(xRatio, yRatio, zoomRatio);
        if(oldBackgroundImage != null)
            oldBackgroundImage.setPosition(xRatio, yRatio, zoomRatio);
    }
}
