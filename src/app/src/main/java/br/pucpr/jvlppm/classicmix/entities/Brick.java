package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;

import java.util.List;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.services.Assets;
import br.pucpr.jvlppm.classicmix.services.Settings;

public class Brick extends GameEntity {
    private float hitTimeCount;
    public float x, y;
    public float strength;
    public final float initialStrength;
    private Frame stateFrame;
    private final Frame brickFrame, shadowFrame;
    private final List<Frame> breakingFrames;
    public char itemCode;
    private float scale;
    private final boolean useShadows;
    private Frame cache;
    private float xOffset, yOffset;

    public Brick(Frame frame, float strength, float scale) {
        this.initialStrength = strength;
        this.strength = strength;
        this.brickFrame = frame;
        this.shadowFrame = Assets.getInstance().brickShadow;
        this.breakingFrames = Assets.getInstance().brickStrength;
        this.useShadows = Settings.Graphics.useShadows();
        this.scale = scale;
        if(strength > breakingFrames.size())
            this.stateFrame = Assets.getInstance().brickReinforcement;
        if(scale > 0)
            updateImage();
    }

    public void onBalHit(float force) {
        if(hitTimeCount < 0.3f)
            return;
        hitTimeCount = 0;
        strength -= force;

        updateImage();
    }

    @Override
    public void update(GameTime gameTime) {
        super.update(gameTime);
        hitTimeCount += gameTime.getElapsedTime();
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        super.draw(gameTime, canvas);

        if(cache != null) {
            draw(canvas, cache, (x * scale - xOffset), (y * scale - yOffset), 0, 0, 1);
        }
        else {
            if (useShadows)
                draw(canvas, shadowFrame, x * scale, y * scale, 0, 0, scale);
            draw(canvas, brickFrame, x * scale, y * scale, 0, 0, scale);

            if (stateFrame != null)
                draw(canvas, stateFrame, x * scale, y * scale, 0, 0, scale);
        }
    }

    public void setScale(float scale) {
        this.scale = scale;
        updateImage();
    }

    private void updateImage() {
        Bitmap bmp;
        if(this.cache == null) {
            bmp = Bitmap.createBitmap((int) (shadowFrame.rect.width() * scale), (int) (shadowFrame.rect.height() * scale), Bitmap.Config.ARGB_4444);
            this.cache = new Frame(bmp, new Rect(0, 0, bmp.getWidth(), bmp.getHeight()), 1);
            xOffset = 15;
            yOffset = 15;
        }
        else {
            bmp = this.cache.texture;
        }
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if(Settings.Graphics.useShadows())
            draw(canvas, shadowFrame, bmp.getWidth() / 2, bmp.getHeight() / 2, 0.5f, 0.5f, scale);
        draw(canvas, brickFrame, bmp.getWidth() / 2, bmp.getHeight() / 2, 0.5f, 0.5f, scale);

        if(strength < initialStrength) {
            int state = (int) ((((float) strength / 6) * (breakingFrames.size() + 1)));
            state = Math.max(0, state);

            if (state < breakingFrames.size()) {
                stateFrame = Assets.getInstance().brickStrength.get(state);
                draw(canvas, stateFrame, bmp.getWidth() / 2, bmp.getHeight() / 2, 0.5f, 0.5f, scale);
            }
            else stateFrame = null;
        }
    }

    public void getRect(Rect dest) {
        float width = brickFrame.rect.width() * scale;
        float height = brickFrame.rect.height() * scale;
        dest.set((int) (x * scale),
                 (int) (y * scale),
                 (int) (x * scale + width),
                 (int) (y * scale + height));
    }

    public float centerX() {
        float width = brickFrame.rect.width();
        return (x + width / 2) * scale;
    }

    public float centerY() {
        float height = brickFrame.rect.height();
        return (y + height / 2) * scale;
    }
}
