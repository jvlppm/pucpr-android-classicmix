package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Rect;

import java.util.Random;

import br.pucpr.jvlppm.classicmix.Side;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.core.Vector;
import br.pucpr.jvlppm.classicmix.services.Assets;
import br.pucpr.jvlppm.classicmix.services.Settings;
import br.pucpr.jvlppm.classicmix.services.Sound;

public class Ball extends GameEntity {
    private final boolean useShadows;
    public float x, y;
    public float oldX, oldY;
    public final float collisionRange;
    private final Vector velocity;
    private Assets assets;
    private final Random random;

    public Ball() {
        random = new Random(System.nanoTime());
        assets = Assets.getInstance();
        collisionRange = assets.ballBlue.rect.width();
        this.useShadows = Settings.Graphics.useShadows();
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

        if (useShadows)
            draw(canvas, assets.ballShadow, x + 2, y + 4, 0.5f, 0.5f);
        draw(canvas, assets.ballGray, x, y, 0.5f, 0.5f);
    }

    public void onScreenLimit(Side limit) {
        onCollision(limit, false);
    }

    private void onCollision(Side side, boolean changeDirection) {
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
        if (changeDirection) {
            float speed = getVelocity();
            float angleChange = random.nextFloat() * 20 - 10;
            float angle = velocity.toDegrees();
            Vector.fromDegrees(angle + angleChange, velocity);
            velocity.setLength(speed);
        }
    }

    public void onObjectCollision(Rect ballRect, Rect objRect, boolean pushBall) {
        checkHorizontalCollision(ballRect, objRect, pushBall);
        checkVerticalCollision(ballRect, objRect, pushBall);
        Sound.getInstance().playBounce();
    }

    private void checkHorizontalCollision(Rect ballRect, Rect objRect, boolean pushBall) {
        if (ballRect.right <= objRect.left + collisionRange) {
            onCollision(Side.Right, !pushBall);
        }
        else if(ballRect.left >= objRect.right - collisionRange) {
            onCollision(Side.Left, !pushBall);
        }
    }

    private void checkVerticalCollision(Rect ballRect, Rect objRect, boolean pushBall) {
        if(ballRect.top >= objRect.bottom - collisionRange) {
            onCollision(Side.Top, !pushBall);
        }
        else if(ballRect.bottom <= objRect.top + collisionRange) {
            onCollision(Side.Bottom, !pushBall);
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