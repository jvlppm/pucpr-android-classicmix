package br.pucpr.jvlppm.classicmix;

import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Rect;

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
import br.pucpr.jvlppm.classicmix.core.TouchEvent;
import br.pucpr.jvlppm.classicmix.core.Vector;
import br.pucpr.jvlppm.classicmix.entities.Ball;
import br.pucpr.jvlppm.classicmix.entities.Brick;
import br.pucpr.jvlppm.classicmix.entities.CenterMessage;
import br.pucpr.jvlppm.classicmix.entities.ExtraLifeCounter;
import br.pucpr.jvlppm.classicmix.entities.Paddle;
import br.pucpr.jvlppm.classicmix.entities.Score;

public class GamePlayScreen extends GameScreen {
    private static enum State { WAITING, PLAYING, GAME_OVER };

    static final String Tag = "GamePlayScreen";

    private final Score score;
    private final ExtraLifeCounter lifeCounter;
    private final Paddle paddle;
    private final List<Ball> balls;
    private final List<Brick> bricks;
    private final CenterMessage msgMoveToBegin, msgGameOver;
    private final Rect tmpRectObj, tmpRectBall;
    private final Vector tmpVector;
    private final Random random;

    private int currentLevel;

    private int trackTouchId;
    private float ballRadius;
    private float brickRadiusX, brickRadiusY;

    private State state;

    public GamePlayScreen(GameActivity game, FinishListener finishListener) {
        super(game, finishListener);

        score = new Score();
        add(score);

        lifeCounter = new ExtraLifeCounter();
        lifeCounter.setPosition(game.getFrameBufferWidth(), game.getFrameBufferHeight());
        add(lifeCounter);

        paddle = new Paddle();
        add(paddle);
        balls = new ArrayList<Ball>();
        bricks = new ArrayList<Brick>();

        Assets assets = Assets.getInstance();
        ballRadius = assets.ballBlue.texture.getWidth() / 2;
        brickRadiusX = assets.brickBlue.texture.getWidth() / 2;
        brickRadiusY = assets.brickBlue.texture.getHeight() / 2;
        msgMoveToBegin = new CenterMessage(assets.msgMoveToBegin);
        msgGameOver = new CenterMessage(assets.msgGameOver);

        tmpRectObj = new Rect();
        tmpRectBall = new Rect();
        tmpVector = new Vector();

        random = new Random(System.nanoTime());

        reset();
    }

    void startLevel(int level) {
        currentLevel = level;
        resetBall();
        loadLevelData(level);
    }

    private void setState(State state) {
        if(this.state == state)
            return;

        if(this.state != null) {
            switch (this.state) {
                case WAITING:
                    remove(msgMoveToBegin);
                    break;
                case GAME_OVER:
                    remove(msgGameOver);
                    break;
            }
        }

        switch (state) {
            case WAITING: add(msgMoveToBegin); break;
            case PLAYING:
                startBallMovement();
                remove(msgMoveToBegin);
                break;
            case GAME_OVER: add(msgGameOver); break;
        }
        this.state = state;
    }

    private void resetPaddle() {
        paddle.setPosition(
                game.getFrameBufferWidth() / 2,
                game.getFrameBufferHeight() * 0.8f);
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
            if(level > 0)
                startLevel(0);
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
        if(balls.isEmpty()) {
            if(lifeCounter.getExtraLives() > 0) {
                lifeCounter.setExtraLives(lifeCounter.getExtraLives() - 1);
                resetBall();
            }
            else
                setState(State.GAME_OVER);
        }
    }

    private void resetBall() {
        for(Ball ball : balls)
            remove(ball);
        balls.clear();

        Ball ball = new Ball();
        ball.x = game.getFrameBufferWidth() / 2;
        ball.y = game.getFrameBufferHeight() * 0.6f;
        balls.add(ball);
        add(ball);
        setState(State.WAITING);
    }

    private void reset() {
        score.reset();
        lifeCounter.setExtraLives(3);
        resetPaddle();
        startLevel(0);
    }

    private void startBallMovement() {
        paddle.getRect(tmpRectObj);

        for(Ball ball : balls) {
            float targetX = tmpRectObj.left + (random.nextFloat() * 0.2f + 0.4f) * tmpRectObj.width();
            float targetY = tmpRectObj.top;

            tmpVector.dx = targetX - ball.x;
            tmpVector.dy = targetY - ball.y;
            tmpVector.setLength(500);
            ball.setVelocity(tmpVector.dx, tmpVector.dy);
        }
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

                if(tmpRectBall.intersects(tmpRectObj.left, tmpRectObj.top, tmpRectObj.right, tmpRectObj.bottom)) {
                    ball.onBrickCollision(brick, tmpRectBall, tmpRectObj);
                    score.add(30);
                }
            }
        }

        for(int i = bricks.size() - 1; i >= 0; i--) {
            if (bricks.get(i).strength <= 0) {
                remove(bricks.get(i));
                bricks.remove(i);
                score.add(100);
            }
        }

        if(bricks.size() <= 0) {
            startLevel(currentLevel + 1);
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

            float speed = tmpVector.getLength();
            float position = (ball.x - tmpRectObj.left) / tmpRectObj.width();
            float variation = random.nextFloat() * 0.1f - 0.05f;

            float degrees = 180 - (position * 0.95f + variation) * 180;
            degrees = Math.min(160, Math.max(20, degrees));
            Vector.fromDegrees(degrees, tmpVector);
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
    public void handleTouch(TouchEvent event) {
        if (state == State.GAME_OVER) {
            if(event.type == TouchEvent.Type.RELEASE) {
                reset();
            }
            return;
        }

        if (trackTouchId < 0 && event.type == TouchEvent.Type.PRESS) {
            if (Math.abs(paddle.getX() - event.x) < 50 &&
                    event.y > paddle.getY() - 50)
                trackTouchId = event.pointerId;
        }

        if (trackTouchId == event.pointerId) {
            if (event.type == TouchEvent.Type.RELEASE) {
                if(state == State.WAITING)
                    setState(State.PLAYING);
                trackTouchId = -1;
            }
            paddle.setPosition(event.x, paddle.getY());
        }
    }
}
