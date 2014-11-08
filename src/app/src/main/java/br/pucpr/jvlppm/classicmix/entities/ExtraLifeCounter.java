package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;

import br.pucpr.jvlppm.classicmix.services.Assets;
import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;

public class ExtraLifeCounter extends GameEntity {
    public final Frame frame;
    private int lives;
    int x, y;

    public ExtraLifeCounter() {
        frame = Assets.getInstance().lifeIndicator;
    }

    public int getExtraLives() {
        return lives;
    }

    public void setExtraLives(int lives) {
        this.lives = lives;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        float width = frame.rect.width();

        for(int i = 0; i < lives; i++)
            draw(canvas, frame, x - i * width / 2, y, 1, 1);
    }
}
