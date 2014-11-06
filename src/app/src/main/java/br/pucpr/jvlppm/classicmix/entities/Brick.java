package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;

import br.pucpr.jvlppm.classicmix.Assets;
import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;

public class Brick extends GameEntity {
    public float x, y;
    public int strength;
    private Frame brickFrame, reinforcementFrame, shadowFrame;

    public Brick(Frame frame, int strength) {
        this.strength = strength;
        this.brickFrame = frame;
        this.reinforcementFrame = Assets.getInstance().brickReinforcement;
        this.shadowFrame = Assets.getInstance().brickShadow;
    }

    void onBallHit() {
        strength--;
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        super.draw(gameTime, canvas);

        draw(canvas, shadowFrame, x + 2, y + 4, 0.5f, 0.5f);
        draw(canvas, brickFrame, x, y, 0.5f, 0.5f);
        if(strength > 1)
            draw(canvas, reinforcementFrame, x, y, 0.5f, 0.5f);
    }

//    public int getWidth() {
//        return brickFrame.rect.width();
//    }
//
//    public int getHeight() {
//        return brickFrame.rect.height();
//    }
}
