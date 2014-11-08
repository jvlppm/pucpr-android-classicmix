package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import br.pucpr.jvlppm.classicmix.core.GameTime;

public class Score extends GameEntity {
    private int score;
    private int highScore;

    private String highScoreText;
    private String scoreText;

    private final Paint paint;
    private final Rect rectHiScore, rectScore;

    public Score() {
        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(22);
        paint.setShadowLayer(6, 2, 2, Color.BLACK);
        rectHiScore = new Rect();
        rectScore = new Rect();
        setScore(0);
    }

    public int getScore() {
        return score;
    }

    public void setHighScore(int score) {
        this.highScore = score;
        highScoreText = "HI: " + score;

        paint.getTextBounds(highScoreText, 0, highScoreText.length(), rectHiScore);
    }

    private void setScore(int score) {
        this.score = score;
        scoreText = "" + score;
        paint.getTextBounds(scoreText, 0, scoreText.length(), rectScore);

        if(this.score >= this.highScore)
            setHighScore(score);
    }

    public void reset() {
        this.setScore(0);
    }

    public void add(int score) {
        setScore(this.score + score);
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        canvas.drawText(highScoreText, 12, canvas.getHeight() - 24 - rectScore.height(), paint);
        canvas.drawText(scoreText, 12 + (rectHiScore.width() - rectScore.width()), canvas.getHeight() - 12, paint);
    }
}
