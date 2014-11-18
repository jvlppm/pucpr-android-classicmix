package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Rect;

import br.pucpr.jvlppm.classicmix.services.Assets;
import br.pucpr.jvlppm.classicmix.Side;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.core.Vector;
import br.pucpr.jvlppm.classicmix.services.Sound;

public class Ball extends GameEntity {
    public float x, y;
    public float oldX, oldY;
    public final float collisionRange;
    private final Vector velocity;
    private Assets assets;

    public Ball() {
        assets = Assets.getInstance();
        collisionRange = assets.ballBlue.rect.width();
        velocity = new Vector();
    }

    @Override
    public void update(GameTime gameTime) {
        super.update(gameTime);
        oldX = x;
        oldY = y;
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

    public void onObjectCollision(Rect ballRect, Rect objRect, boolean pushBall) {
        checkHorizontalCollision(ballRect, objRect, pushBall);
        checkVerticalCollision(ballRect, objRect, pushBall);
        Sound.getInstance().playBounce();
    }

    private void checkHorizontalCollision(Rect ballRect, Rect objRect, boolean pushBall) {
        if (ballRect.right <= objRect.left + collisionRange) {
            onCollision(Side.Right);
//            if (pushBall || oldX < objRect.left - collisionRange)
//                x = objRect.left - collisionRange;
        }
        else if(ballRect.left >= objRect.right - collisionRange) {
            onCollision(Side.Left);
//            if (pushBall || oldX > objRect.left + collisionRange)
//                x = objRect.right + collisionRange;
        }
    }

    private void checkVerticalCollision(Rect ballRect, Rect objRect, boolean pushBall) {
        if(ballRect.top >= objRect.bottom - collisionRange) {
            onCollision(Side.Top);
//            if (pushBall || oldY > objRect.bottom + collisionRange)
//                y = objRect.bottom + collisionRange;
        }
        else if(ballRect.bottom <= objRect.top + collisionRange) {
            onCollision(Side.Bottom);
//            if (pushBall || oldY < objRect.top - collisionRange)
//                y = objRect.top - collisionRange;
        }
    }

    public void getVelocity(Vector dest) {
        dest.dx = velocity.dx;
        dest.dy = velocity.dy;
    }

    public void setVelocity(float vx, float vy) {
        this.velocity.dx = vx;
        this.velocity.dy = vy;
    }

    public float getVelocity() {
        return velocity.getLength();
    }

    public void setVelocity(float speed) {
        this.velocity.setLength(speed);
    }
}