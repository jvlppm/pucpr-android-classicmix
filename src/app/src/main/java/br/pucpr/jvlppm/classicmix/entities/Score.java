package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import br.pucpr.jvlppm.classicmix.core.GameTime;

public class Score extends GameEntity {
    private int score;
    private final Paint paint;

    public Score() {
        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(26);
        paint.setShadowLayer(6, 2, 2, Color.BLACK);
    }

    public int getScore() {
        return score;
    }

    public void reset() {
        this.score = 0;
    }

    public void add(int score) {
        this.score += score;
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        String score = "" + this.score;
        canvas.drawText(score, 12, canvas.getHeight() - 12, paint);
    }
}
