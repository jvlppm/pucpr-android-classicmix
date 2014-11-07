package br.pucpr.jvlppm.classicmix;

import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameActivity;
import br.pucpr.jvlppm.classicmix.core.GameScreen;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.core.Vector;
import br.pucpr.jvlppm.classicmix.entities.Ball;
import br.pucpr.jvlppm.classicmix.entities.Brick;
import br.pucpr.jvlppm.classicmix.entities.Paddle;

public class GamePlayScreen extends GameScreen {
    private final Paddle paddle;
    private final List<Ball> balls;
    private final List<Brick> bricks;
    private final Rect tmpRectObj, tmpRectBall;
    private final Vector tmpVector;
    private final Random random;

    private int currentLevel;

    private int paddleTouchId;
    private float ballRadius;
    private float brickRadiusX, brickRadiusY;

    public GamePlayScreen(GameActivity game, FinishListener finishListener) {
        super(game, finishListener);

        paddle = new Paddle();
        add(paddle);
        balls = new ArrayList<Ball>();
        bricks = new ArrayList<Brick>();

        ballRadius = Assets.getInstance().ballBlue.texture.getWidth() / 2;
        brickRadiusX = Assets.getInstance().brickBlue.texture.getWidth() / 2;
        brickRadiusY = Assets.getInstance().brickBlue.texture.getHeight() / 2;

        tmpRectObj = new Rect();
        tmpRectBall = new Rect();
        tmpVector = new Vector();

        random = new Random(System.nanoTime());

        startLevel(0);
    }

    void startLevel(int level) {
        currentLevel = level;
        resetPaddle();
        resetBalls();
        loadLevelData(level);
    }

    private void resetPaddle() {
        paddle.setPosition(
                game.getFrameBufferWidth() / 2,
                game.getFrameBufferHeight() * 0.9f);
    }

    private void resetBalls() {
        for(Ball ball : balls)
            remove(ball);
        balls.clear();

        Ball ball = new Ball();
        ball.x = game.getFrameBufferWidth() / 2;
        ball.y = game.getFrameBufferHeight() * 0.75f;
        ball.setVelocity(-400, -400);
        balls.add(ball);
        add(ball);
    }

    private void loadLevelData(int level) {
        for(Brick brick : bricks)
            remove(brick);
        bricks.clear();

        AssetManager am = game.getAssets();
        try {
            InputStream is = am.open("level" + (level + 1) + ".txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            int row = 0;
            while(true) {
                String line = reader.readLine();
                if (line == null)
                    break;

                for (int i = 0; i < line.length(); i += 2) {
                    char brick = line.charAt(i);
                    char item = '\0';
                    if(i + 1 < line.length())
                        item = line.charAt(i + 1);
                    createBrick(brick, item, i / 2, row);
                }

                row++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createBrick(char brick, char item, int col, int row) {
        Frame frame = null;
        int strength = 1;
        switch (brick) {
            case 'r': frame = Assets.getInstance().brickRed; break;
            case 'g': frame = Assets.getInstance().brickGreen; break;
            case 'b': frame = Assets.getInstance().brickBlue; break;
            case 'y': frame = Assets.getInstance().brickYellow; break;
            case 'p': frame = Assets.getInstance().brickPurple; break;
            case '=':
                frame = Assets.getInstance().brickGray;
                strength = 2;
                break;
        }
        if(frame == null)
            return;
        Brick bEntity = new Brick(frame, strength);
        bEntity.x = col * frame.rect.width() + frame.rect.width() / 2;
        bEntity.y = row * frame.rect.height() + frame.rect.height() / 2;
        add(bEntity);
        bricks.add(bEntity);
    }

    private void destroyBall(Ball ball) {
        balls.remove(ball);
        remove(ball);
        if(balls.isEmpty())
            resetBalls();
    }

    private void checkWallCollisions() {
        for(Ball ball : balls) {
            if (ball.x - ballRadius < 0)
                ball.onScreenLimit(Side.Left);
            else if(ball.x + ballRadius > game.getFrameBufferWidth())
                ball.onScreenLimit(Side.Right);

            if(ball.y - ballRadius < 0)
                ball.onScreenLimit(Side.Top);
            else if(ball.y - ballRadius > game.getFrameBufferHeight()) {
                destroyBall(ball);
            }
        }
    }

    private void checkBrickCollisions() {
        for(Ball ball : balls) {
            tmpRectBall.set((int) (ball.x - ballRadius),
                    (int) (ball.y - ballRadius),
                    (int) (ball.x + ballRadius),
                    (int) (ball.y + ballRadius));
            for(Brick brick : bricks) {
                tmpRectObj.set(
                        (int) (brick.x - brickRadiusX),
                        (int) (brick.y - brickRadiusY),
                        (int) (brick.x + brickRadiusX),
                        (int) (brick.y + brickRadiusY));

                if(tmpRectBall.intersects(tmpRectObj.left, tmpRectObj.top, tmpRectObj.right, tmpRectObj.bottom))
                    ball.onBrickCollision(brick, tmpRectBall, tmpRectObj);
            }
        }

        for(int i = bricks.size() - 1; i >= 0; i--) {
            if (bricks.get(i).strength <= 0) {
                remove(bricks.get(i));
                bricks.remove(i);
            }
        }
    }

    private void checkPaddleCollisions() {
        paddle.getRect(tmpRectObj);

        for (Ball ball : balls) {
            if(ball.y > paddle.getY() ||
               !tmpRectObj.intersects(
                    (int) (ball.x - ballRadius),
                    (int) (ball.y - ballRadius),
                    (int) (ball.x + ballRadius),
                    (int) (ball.y + ballRadius)))
                continue;

            ball.getVelocity(tmpVector);

            float speed = tmpVector.length();
            float position = (ball.x - tmpRectObj.left) / tmpRectObj.width();
            float variation = random.nextFloat() * 0.1f - 0.05f;

            Vector.fromDegrees(170 - (position * 0.95f + variation) * 160, tmpVector);
            ball.setVelocity(tmpVector.dx * speed, tmpVector.dy * speed);
        }
    }

    @Override
    protected void update(GameTime gameTime) {
        super.update(gameTime);
        checkWallCollisions();
        checkBrickCollisions();
        checkPaddleCollisions();
    }

    @Override
    protected void draw(GameTime gameTime, Canvas canvas) {
        canvas.drawARGB(255, 255, 0, 0);
        super.draw(gameTime, canvas);
    }

    @Override
    public boolean handleTouch(MotionEvent event, float touchX, float touchY) {
        if (paddleTouchId < 0 && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (Math.abs(paddle.getX() - touchX) < 50 &&
                    Math.abs(paddle.getY() - touchY) < 50)
                paddleTouchId = event.getPointerId(0);
        }

        if (paddleTouchId == event.getPointerId(0)) {
            if (event.getAction() == MotionEvent.ACTION_UP)
                paddleTouchId = -1;
            paddle.setPosition(touchX, paddle.getY());
            return true;
        }
        return false;
    }
}
