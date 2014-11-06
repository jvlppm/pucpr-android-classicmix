package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Rect;

import br.pucpr.jvlppm.classicmix.Assets;
import br.pucpr.jvlppm.classicmix.Side;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.core.Vector;

public class Ball extends GameEntity {
    public float x, y;
    private final Vector velocity;
    private Assets assets;

    public Ball() {
        assets = Assets.getInstance();
        velocity = new Vector();
    }

    @Override
    public void update(GameTime gameTime) {
        super.update(gameTime);
        x += velocity.dx * gameTime.getElapsedTime();
        y += velocity.dy * gameTime.getElapsedTime();
    }

    @Override
    public void draw(GameTime gameTime, Canvas canvas) {
        super.draw(gameTime, canvas);

        draw(canvas, assets.ballShadow, x + 2, y + 4, 0.5f, 0.5f);
        draw(canvas, assets.ballGray, x, y, 0.5f, 0.5f);
    }

    public void onScreenLimit(Side limit) {
        onCollision(limit);
    }

    private void onCollision(Side side) {
        switch (side) {
            case Left:
                velocity.dx = Math.abs(velocity.dx);
                break;
            case Right:
                velocity.dx = -Math.abs(velocity.dx);
                break;
            case Top:
                velocity.dy = Math.abs(velocity.dy);
                break;
            case Bottom:
                velocity.dy = -Math.abs(velocity.dy);
                break;
        }
    }

    public void onBrickCollision(Brick brick, Rect ballRect, Rect brickRect) {
        if(ballRect.right > brickRect.right)
            onCollision(Side.Left);
        else if(ballRect.left < brickRect.left)
            onCollision(Side.Right);

        if(ballRect.top < brickRect.top)
            onCollision(Side.Bottom);
        else if(ballRect.bottom > brickRect.bottom)
            onCollision(Side.Top);

        brick.strength--;
    }

    public void setVelocity(float vx, float vy) {
        this.velocity.dx = vx;
        this.velocity.dy = vy;
    }
}