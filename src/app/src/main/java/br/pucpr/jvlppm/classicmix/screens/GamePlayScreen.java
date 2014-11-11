package br.pucpr.jvlppm.classicmix.screens;

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

import br.pucpr.jvlppm.classicmix.Side;
import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameActivity;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.core.Pool;
import br.pucpr.jvlppm.classicmix.core.Scene;
import br.pucpr.jvlppm.classicmix.core.TouchEvent;
import br.pucpr.jvlppm.classicmix.core.Vector;
import br.pucpr.jvlppm.classicmix.entities.Background;
import br.pucpr.jvlppm.classicmix.entities.Ball;
import br.pucpr.jvlppm.classicmix.entities.Brick;
import br.pucpr.jvlppm.classicmix.entities.ExtraLifeCounter;
import br.pucpr.jvlppm.classicmix.entities.Image;
import br.pucpr.jvlppm.classicmix.entities.Item;
import br.pucpr.jvlppm.classicmix.entities.Laser;
import br.pucpr.jvlppm.classicmix.entities.Paddle;
import br.pucpr.jvlppm.classicmix.entities.Score;
import br.pucpr.jvlppm.classicmix.services.Assets;
import br.pucpr.jvlppm.classicmix.services.Settings;

public class GamePlayScreen extends Scene {
    private static enum State { WAITING, PLAYING, GAME_OVER };
    private final static int LAYER_BACKGROUND = 0;
    private final static int LAYER_WORLD = 1;
    private final static int LAYER_GUI = 2;

    static final String Tag = "GamePlayScreen";

    // Game Objects
    private final Score score;
    private final ExtraLifeCounter lifeCounter;
    private final Paddle paddle;
    private final List<Ball> balls;
    private final List<Brick> bricks;
    private final List<Item> fallingItems;
    private final Pool<Laser> lasers;
    private Background currentBackground;
    private Background oldBackground;
    private final Image msgMoveToBegin;
    private final Image msgGameOver;

    // Game Objects info
    private final float ballRadius;
    private final float brickRadiusX, brickRadiusY;

    // Difficulty modifiers
    private final float defaultBallSpeed;
    private final int defaultLives;
    private final float defaultScoreMultiplier;

    // Constants
    private final float laserDelay = 1;

    // Game State
    private State state;
    private int currentLevel;
    private boolean piercing;
    private float ballSlowEffect;
    private int trackTouchId;
    public boolean shootingLaser;
    private float timeToLaser;

    // Auxiliar objects
    private final Random random;
    private final Vector tmpVector;
    private final Rect tmpRect2, tmpRect1;

    public GamePlayScreen(GameActivity game, FinishListener finishListener) {
        super(game, finishListener);

        Assets assets = Assets.getInstance();
        add(new Image(assets.bottomBar, Image.Alignment.Bottom), LAYER_GUI);

        score = new Score();
        add(score, LAYER_GUI);

        lifeCounter = new ExtraLifeCounter();
        lifeCounter.setPosition(game.getFrameBufferWidth(), game.getFrameBufferHeight() - 8);
        add(lifeCounter, LAYER_GUI);

        paddle = new Paddle();
        add(paddle, LAYER_WORLD);
        balls = new ArrayList<Ball>();
        bricks = new ArrayList<Brick>();
        fallingItems = new ArrayList<Item>();
        lasers = new Pool<Laser>(Laser.class);

        ballRadius = assets.ballBlue.texture.getWidth() / 2;
        brickRadiusX = assets.brickBlue.texture.getWidth() / 2;
        brickRadiusY = assets.brickBlue.texture.getHeight() / 2;
        msgMoveToBegin = new Image(assets.msgMoveToBegin, Image.Alignment.Center);
        msgGameOver = new Image(assets.msgGameOver, Image.Alignment.Center);

        tmpRect2 = new Rect();
        tmpRect1 = new Rect();
        tmpVector = new Vector();

        random = new Random(System.nanoTime());

        switch (Settings.Gameplay.getDifficulty()) {
            case Easy:
                defaultLives = 5;
                defaultBallSpeed = 300;
                defaultScoreMultiplier = 0.5f;
                break;
            case Hard:
                defaultBallSpeed = 600;
                defaultLives = 2;
                defaultScoreMultiplier = 1.2f;
                break;
            default:
                defaultBallSpeed = 500;
                defaultLives = 3;
                defaultScoreMultiplier = 1f;
                break;
        }

        reset();
    }

    void startLevel(int level) {
        currentLevel = level;
        resetBall();
        resetItems();
        removeFallingItems();
        removeLasers();
        loadLevelData(level);
        setBackground(level);
    }

    private void setState(State state) {
        if(this.state == state)
            return;

        if(this.state != null) {
            switch (this.state) {
                case WAITING:
                    remove(msgMoveToBegin, LAYER_GUI);
                    break;
                case GAME_OVER:
                    remove(msgGameOver, LAYER_GUI);
                    break;
            }
        }

        switch (state) {
            case WAITING: add(msgMoveToBegin, LAYER_GUI); break;
            case PLAYING:
                startBallMovement();
                remove(msgMoveToBegin, LAYER_GUI);
                break;
            case GAME_OVER: add(msgGameOver, LAYER_GUI); break;
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
            remove(brick, LAYER_WORLD);
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
        add(bEntity, LAYER_WORLD);
        bEntity.itemCode = item;
        bricks.add(bEntity);
    }

    private void setBackground(int level) {
        List<Frame> backgrounds = Assets.getInstance().background;
        if(level >= backgrounds.size())
            level = 0;
        Frame backgroundImage = backgrounds.get(level);
        if (backgroundImage != null) {
            Background background = new Background(
                    backgroundImage,
                    game.getFrameBufferWidth(),
                    game.getFrameBufferHeight());

            if(currentBackground != null) {
                oldBackground = currentBackground;
                background.fadeIn(oldBackground);
                oldBackground.fadeOut();
            }

            currentBackground = background;
            add(background, LAYER_BACKGROUND);
        }
    }

    private void loseLife() {
        lifeCounter.setExtraLives(lifeCounter.getExtraLives() - 1);
        removeFallingItems();
        resetItems();
        resetBall();
    }

    private void resetBall() {
        for(Ball ball : balls)
            remove(ball, LAYER_WORLD);
        balls.clear();

        Ball ball = new Ball();
        ball.x = game.getFrameBufferWidth() / 2;
        ball.y = game.getFrameBufferHeight() * 0.6f;
        balls.add(ball);
        add(ball, LAYER_WORLD);
        setState(State.WAITING);
    }

    private void removeFallingItems() {
        for(Item item : fallingItems) {
            remove(item, LAYER_WORLD);
        }
        fallingItems.clear();
    }

    private void resetItems() {
        ballSlowEffect = 1;
        piercing = false;
        shootingLaser = false;
    }

    private void reset() {
        score.reset();
        lifeCounter.setExtraLives(defaultLives);
        resetPaddle();
        startLevel(0);
    }

    private void startBallMovement() {
        paddle.getRect(tmpRect2);

        for(Ball ball : balls) {
            float targetX = tmpRect2.left + (random.nextFloat() * 0.2f + 0.4f) * tmpRect2.width();
            float targetY = tmpRect2.top;

            tmpVector.dx = targetX - ball.x;
            tmpVector.dy = targetY - ball.y;
            tmpVector.setLength(defaultBallSpeed);
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
            tmpRect1.set((int) (ball.x - ballRadius),
                    (int) (ball.y - ballRadius),
                    (int) (ball.x + ballRadius),
                    (int) (ball.y + ballRadius));
            for(int i = bricks.size() - 1; i >= 0; i--) {
                Brick brick = bricks.get(i);
                tmpRect2.set(
                        (int) (brick.x - brickRadiusX),
                        (int) (brick.y - brickRadiusY),
                        (int) (brick.x + brickRadiusX),
                        (int) (brick.y + brickRadiusY));

                if(tmpRect1.intersects(tmpRect2.left, tmpRect2.top, tmpRect2.right, tmpRect2.bottom)) {
                    if(piercing)
                        brick.strength = 0;
                    else
                        ball.onBrickCollision(brick, tmpRect1, tmpRect2);
                    hitBrick(brick);
                }
            }
        }

        if(bricks.size() <= 0) {
            startLevel(currentLevel + 1);
        }
    }

    private void hitBrick(Brick brick) {
        brick.strength--;
        if(brick.strength > 1)
            score.add((int)(30 * defaultScoreMultiplier));

        if (brick.strength <= 0) {
            remove(brick, LAYER_WORLD);
            dropItems(brick);
            bricks.remove(brick);
            score.add((int)(100 * defaultScoreMultiplier));
        }
    }

    private void dropItems(Brick brick) {
        Frame frame;
        switch (brick.itemCode) {
            case 's':
                frame = Assets.getInstance().itemSlowBall;
                break;
            case 'l':
                frame = Assets.getInstance().itemLaser;
                break;
            case 'p':
                frame = Assets.getInstance().itemPierce;
                break;
            case 'e':
                frame = Assets.getInstance().itemEnlarge;
                break;
            default: return;
        }

        Item item = new Item(frame, brick.x, brick.y);
        add(item, LAYER_WORLD);
        fallingItems.add(item);
    }

    private void checkPaddleCollisions() {
        paddle.getRect(tmpRect2);

        for (Ball ball : balls) {
            if(!tmpRect2.intersects(
                    (int) (ball.x - ballRadius),
                    (int) (ball.y - ballRadius),
                    (int) (ball.x + ballRadius),
                    (int) (ball.y + ballRadius)))
                continue;

            ball.getVelocity(tmpVector);

            float speed = tmpVector.getLength();
            float position = (ball.x - tmpRect2.left) / tmpRect2.width();
            float variation = random.nextFloat() * 0.1f - 0.05f;
            position = position * 0.95f + variation;

            float degrees = (1 - position) * 180;
            degrees = Math.min(135, Math.max(45, degrees));
            Vector.fromDegrees(degrees, tmpVector);
            ball.setVelocity(tmpVector.dx * speed, tmpVector.dy * speed);
        }
    }

    private void checkFallingItems() {
        paddle.getRect(tmpRect2);

        for(int i = fallingItems.size() - 1; i >= 0; i--) {
            Item item = fallingItems.get(i);
            item.getRect(tmpRect1);
            if(tmpRect1.top > game.getFrameBufferHeight()) {
                fallingItems.remove(i);
                remove(item, LAYER_WORLD);
                continue;
            }

            if(!tmpRect2.intersects(tmpRect1.left, tmpRect1.top, tmpRect1.right, tmpRect1.bottom))
                continue;
            remove(item, LAYER_WORLD);
            fallingItems.remove(i);

            Assets assets = Assets.getInstance();
            if(item.frame == assets.itemSlowBall) {
                for(Ball ball : balls)
                    ball.setVelocity(ball.getVelocity() * (1 - 0.4f * ballSlowEffect));
                ballSlowEffect *= 0.6f;
            }
            else if(item.frame == assets.itemPierce) {
                piercing = true;
            }
            else if(item.frame == assets.itemLaser) {
                shootingLaser = true;
            }
        }
    }

    private void destroyBall(Ball ball) {
        balls.remove(ball);
        remove(ball, LAYER_WORLD);
        if(balls.isEmpty()) {
            if(lifeCounter.getExtraLives() > 0) {
                loseLife();
            }
            else
                setState(State.GAME_OVER);
        }
    }

    private void createLaser(float x, float y) {
        Laser laser = lasers.getNew();
        laser.x = x;
        laser.y = y;
        add(laser, LAYER_WORLD);
    }

    private void updateLaserShot(GameTime gameTime) {
        if(shootingLaser) {
            timeToLaser -= gameTime.getElapsedTime();
            if (timeToLaser <= 0) {
                timeToLaser = laserDelay;
                paddle.getRect(tmpRect1);
                createLaser(tmpRect1.right - 8, paddle.getY());
                createLaser(tmpRect1.left + 8, paddle.getY());
            }
        }
        for(int li = lasers.inUse.size() - 1; li >= 0; li--) {
            Laser laser = lasers.inUse.get(li);
            if (laser.y < 0 || laser.destroyed()) {
                remove(laser, LAYER_WORLD);
                laser.reset();
                lasers.remove(laser);
            }

            for(int bi = bricks.size() - 1; bi >= 0; bi--) {
                Brick brick = bricks.get(bi);
                tmpRect2.set(
                        (int) (brick.x - brickRadiusX),
                        (int) (brick.y - brickRadiusY),
                        (int) (brick.x + brickRadiusX),
                        (int) (brick.y + brickRadiusY));

                if(tmpRect2.intersects((int)laser.x - 1, (int)laser.y - 4, (int)laser.x + 1, (int)laser.y + 8)) {
                    hitBrick(brick);
                    laser.onHit();
                }
            }
        }
    }

    private void removeLasers() {
        for(int i = lasers.inUse.size() - 1; i >= 0; i--) {
            Laser laser = lasers.inUse.get(i);
            lasers.remove(laser);
            remove(laser, LAYER_WORLD);
        }
    }

    @Override
    protected void update(GameTime gameTime) {
        super.update(gameTime);
        checkWallCollisions();
        checkBrickCollisions();
        checkPaddleCollisions();
        checkFallingItems();
        removeOldBackground();
        updateLaserShot(gameTime);
    }

    private void removeOldBackground() {
        if(oldBackground != null && oldBackground.alpha <= 0) {
            remove(oldBackground, LAYER_BACKGROUND);
            oldBackground = null;
        }
    }

    @Override
    protected void draw(GameTime gameTime, Canvas canvas) {
        canvas.drawARGB(255, 255, 255, 255);
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
