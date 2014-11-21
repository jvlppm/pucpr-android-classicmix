package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.List;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.services.Assets;
import br.pucpr.jvlppm.classicmix.services.Settings;

public class Brick extends GameEntity {
    private float hitTimeCount;
    public float x, y;
    public int strength;
    public final int initialStrength;
    private Frame stateFrame;
    private final Frame brickFrame, shadowFrame;
    private final List<Frame> breakingFrames;
    public char itemCode;
    private float scale;
    private final boolean useShadows;

    public Brick(Frame frame, int strength) {
        this.initialStrength = strength;
        this.strength = strength;
        this.brickFrame = frame;
        this.shadowFrame = Assets.getInstance().brickShadow;
        this.breakingFrames = Assets.getInstance().brickStrength;
        this.useShadows = Settings.Graphics.useShadows();
        if(strength > breakingFrames.size())
            this.stateFrame = Assets.getInstance().brickReinforcement;
    }

    public void onBalHit(int force) {
        if(hitTimeCount < 0.3f)
            return;
        hitTimeCount = 0;
        strength -= force;
        int state = (int)((((float)strength / 6) * (breakingFrames.size() + 1)));
        state = Math.max(0, state);

        if(state < breakingFrames.size())
            stateFrame = Assets.getInstance().brickStrength.get(state);
        else stateFrame = null;
    }

    @Override
    public void update(GameTime gameTime) {
        super.update(gameTime);
        hitTimeCount += gameTime.getElapsedTime();
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        super.draw(gameTime, canvas);

        if(useShadows)
            draw(canvas, shadowFrame, x * scale, y * scale, 0, 0, scale);
        draw(canvas, brickFrame, x * scale, y * scale, 0, 0, scale);
        if(stateFrame != null)
            draw(canvas, stateFrame, x * scale, y * scale, 0, 0, scale);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void getRect(Rect dest) {
        float width = brickFrame.rect.width() * scale;
        float height = brickFrame.rect.height() * scale;
        dest.set((int) (x * scale),
                 (int) (y * scale),
                 (int) (x * scale + width),
                 (int) (y * scale + height));
    }
}
