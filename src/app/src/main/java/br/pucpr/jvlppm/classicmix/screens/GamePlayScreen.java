package br.pucpr.jvlppm.classicmix.screens;

import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import br.pucpr.jvlppm.classicmix.entities.FPS;
import br.pucpr.jvlppm.classicmix.entities.Image;
import br.pucpr.jvlppm.classicmix.entities.Item;
import br.pucpr.jvlppm.classicmix.entities.Laser;
import br.pucpr.jvlppm.classicmix.entities.Paddle;
import br.pucpr.jvlppm.classicmix.entities.Score;
import br.pucpr.jvlppm.classicmix.services.Assets;
import br.pucpr.jvlppm.classicmix.services.Settings;
import br.pucpr.jvlppm.classicmix.services.Sound;

public class GamePlayScreen extends Scene {
    private static enum State { WAITING_RELEASE, POSITION, PLAYING, GAME_OVER };
    private final static int LAYER_BACKGROUND = 0;
    private final static int LAYER_WORLD = 1;
    private final static int LAYER_GUI = 2;
    private final static int LASER_MARGIN = 12;

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
    private final Frame laserReadyEffect;

    // Game Objects info
    private final float ballRadius;

    // Difficulty modifiers
    private final float defaultBallSpeed;
    private final int defaultLives;
    private final float defaultScoreMultiplier;

    // Constants
    private final float LASER_DELAY = 1;

    // Game State
    private State state;
    private int currentLevel;

    // Items
    private int piercing;
    public int shootingLaser;

    private float ballSlowEffect;
    private int trackTouchId = -1;
    private float timeToLaser;
    private boolean lostLife;

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
        msgMoveToBegin = new Image(assets.msgMoveToBegin, Image.Alignment.Center);
        msgGameOver = new Image(assets.msgGameOver, Image.Alignment.Center);
        laserReadyEffect = assets.laser.get(0);

        tmpRect2 = new Rect();
        tmpRect1 = new Rect();
        tmpVector = new Vector();

        random = new Random(System.nanoTime());

        switch (Settings.Gameplay.getDifficulty()) {
            case Easy:
                defaultLives = 5;
                defaultBallSpeed = 300;
                defaultScoreMultiplier = 0.5f;
                paddle.setRetractionRate(0.5f);
                break;
            case Hard:
                defaultBallSpeed = 600;
                defaultLives = 2;
                defaultScoreMultiplier = 1.2f;
                paddle.setRetractionRate(0.75f);
                break;
            default:
                defaultBallSpeed = 500;
                defaultLives = 3;
                defaultScoreMultiplier = 1f;
                paddle.setRetractionRate(1);
                break;
        }

        if (Settings.Graphics.displayFps())
            add(new FPS(), LAYER_GUI);

        reset();
    }

    void startLevel(int level) {
        currentLevel = level;
        resetItems(true);
        removeFallingItems();
        removeLasers();
        removeAllBalls();
        loadLevelData(level);
        setBackground(level);
        Sound.getInstance().playLevelMusic(level);

        if (trackTouchId >= 0)
            setState(State.WAITING_RELEASE);
        else {
            replaceBall(false);
        }
    }

    private void setState(State state) {
        if(this.state == state)
            return;

        if(this.state != null) {
            switch (this.state) {
                case POSITION:
                    remove(msgMoveToBegin, LAYER_GUI);
                    break;
                case GAME_OVER:
                    remove(msgGameOver, LAYER_GUI);
                    break;
            }
        }

        paddle.setPlaying(state == State.PLAYING);

        switch (state) {
            case POSITION:
                add(msgMoveToBegin, LAYER_GUI);
                break;
            case PLAYING:
                startBallMovement();
                remove(msgMoveToBegin, LAYER_GUI);
                break;
            case GAME_OVER: add(msgGameOver, LAYER_GUI); break;
        }
        this.state = state;
    }

    private void resetPaddle() {
        paddle.resetWidth();
        paddle.setPosition(
                game.getFrameBufferWidth() / 2,
                game.getFrameBufferHeight() * 0.8f);
    }

    private void loadLevelData(int level) {
        paddle.setRetract(false);

        for (int brickI = 0; brickI < bricks.size(); brickI++) {
            Brick brick = bricks.get(brickI);
            remove(brick, LAYER_WORLD);
        }

        String word = "([^\\s\\[\\]=]+)";
        Pattern configPattern = Pattern.compile("^\\[" + word + "(=" + word + ")?]\\s*$");

        bricks.clear();

        AssetManager am = game.getAssets();
        try {
            InputStream is = am.open("levels/level" + (level + 1) + ".txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            int row = 0;
            int minColumns = 8;
            int maxColumns = minColumns;
            while(true) {
                String line = reader.readLine();
                if (line == null)
                    break;

                Matcher m = configPattern.matcher(line);

                if (m.matches()) {
                    String key = m.group(1);
                    String value = m.group(3);
                    if("retract".equals(key))
                        paddle.setRetract(!"false".equals(value));
                    continue;
                }

                int lineColumns = (int)Math.ceil((float)line.length() / 2);

                if(lineColumns > maxColumns)
                    maxColumns = lineColumns;

                for (int i = 0; i < line.length(); i += 2) {
                    char brick = line.charAt(i);
                    char item = '\0';
                    if(i + 1 < line.length())
                        item = line.charAt(i + 1);
                    createBrick(brick, item, i / 2, row);
                }

                row++;
            }

            float scale = minColumns / (float)maxColumns;

            for(Brick brick : bricks) {
                brick.setScale(scale);
            }

        } catch (IOException e) {
            if(level > 0)
                loadLevelData(0);
            e.printStackTrace();
        }
    }

    private void createBrick(char brick, char item, int col, int row) {
        Frame frame = null;
        int strength = 1;
        switch (brick) {
            case 'b': frame = Assets.getInstance().brickBlue; strength = 1; break;
            case 'g': frame = Assets.getInstance().brickGreen; strength = 2; break;
            case 'y': frame = Assets.getInstance().brickYellow; strength = 3; break;
            case 'r': frame = Assets.getInstance().brickRed; strength = 4; break;
            case 'p': frame = Assets.getInstance().brickPurple; strength = 5; break;
            case '=':
                frame = Assets.getInstance().brickGray;
                strength = 6;
                break;
        }
        if(frame == null)
            return;
        Brick bEntity = new Brick(frame, strength);
        bEntity.x = col * frame.rect.width();
        bEntity.y = row * frame.rect.height();
        add(bEntity, LAYER_WORLD);
        bEntity.itemCode = item;
        bricks.add(bEntity);
    }

    private void setBackground(int level) {
        if (Settings.Graphics.getBackgroundOpacity() <= 0)
            return;

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
        removeFallingItems();
        resetItems(false);
        resetPaddle();
        removeAllBalls();
        lostLife = true;
        Sound.getInstance().playExplosion();

        if (trackTouchId >= 0)
            setState(State.WAITING_RELEASE);
        else
            replaceBall(true);
    }

    private void replaceBall(boolean loseLife) {
        lostLife = false;
        if(!loseLife || lifeCounter.getExtraLives() > 0) {
            if (loseLife)
                lifeCounter.setExtraLives(lifeCounter.getExtraLives() - 1);

            Ball ball = new Ball();
            ball.x = game.getFrameBufferWidth() / 2;
            ball.y = game.getFrameBufferHeight() * 0.6f;
            balls.add(ball);
            add(ball, LAYER_WORLD);

            setState(State.POSITION);
        }
        else {
            setState(State.GAME_OVER);
        }
    }

    private void removeAllBalls() {
        for (int brickI = 0; brickI < balls.size(); brickI++) {
            Ball ball = balls.get(brickI);
            remove(ball, LAYER_WORLD);
        }
        balls.clear();
    }

    private void removeFallingItems() {
        for (int itemI = 0; itemI < fallingItems.size(); itemI++) {
            Item item = fallingItems.get(itemI);
            remove(item, LAYER_WORLD);
        }
        fallingItems.clear();
    }

    private void resetItems(boolean dropAll) {
        ballSlowEffect = 1;
        if(dropAll) {
            piercing = 0;
            shootingLaser = 0;
        }
        else {
            piercing = Math.max(0, piercing - 1);
            shootingLaser = Math.max(0, shootingLaser - 1);
        }
    }

    private void reset() {
        clearPendingOperations();
        score.reset();
        lifeCounter.setExtraLives(defaultLives);
        resetPaddle();
        startLevel(3);
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
        for (int ballI = 0; ballI < balls.size(); ballI++) {
            Ball ball = balls.get(ballI);
            if (ball.x - ballRadius < 0)
                ball.onScreenLimit(Side.Left);
            else if (ball.x + ballRadius > game.getFrameBufferWidth())
                ball.onScreenLimit(Side.Right);

            if (ball.y - ballRadius < 0)
                ball.onScreenLimit(Side.Top);
            else if (ball.y - ballRadius > game.getFrameBufferHeight()) {
                destroyBall(ball);
            }
        }
    }

    private void checkBrickCollisions() {
        for (int ballI = 0; ballI < balls.size(); ballI++) {
            Ball ball = balls.get(ballI);
            tmpRect1.set((int) (ball.x - ballRadius),
                    (int) (ball.y - ballRadius),
                    (int) (ball.x + ballRadius),
                    (int) (ball.y + ballRadius));
            for (int i = bricks.size() - 1; i >= 0; i--) {
                Brick brick = bricks.get(i);
                brick.getRect(tmpRect2);
                float ignoreDistance = ballRadius + tmpRect2.width();
                if (ball.y - ignoreDistance > tmpRect2.centerY() ||
                        ball.y + ignoreDistance < tmpRect2.centerY() ||
                        ball.x + ignoreDistance < tmpRect2.centerX() ||
                        ball.x - ignoreDistance > tmpRect2.centerX())
                    continue;

                if (tmpRect1.intersects(tmpRect2.left, tmpRect2.top, tmpRect2.right, tmpRect2.bottom)) {
                    hitBrick(brick);
                    if(piercing <= 0 || brick.strength > 0)
                        ball.onObjectCollision(tmpRect1, tmpRect2, false);
                }
            }
        }

        if(bricks.size() <= 0) {
            startLevel(currentLevel + 1);
        }
    }

    private void hitBrick(Brick brick) {
        brick.onBalHit();
        if(brick.strength > 1)
            score.add((int)(30 * defaultScoreMultiplier));

        if (brick.strength <= 0) {
            remove(brick, LAYER_WORLD);
            dropItems(brick);
            bricks.remove(brick);
            score.add((int)(100 * defaultScoreMultiplier));
            Sound.getInstance().playDestroy();
        }
        else
            Sound.getInstance().playHit();
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

        brick.getRect(tmpRect1);
        Item item = new Item(frame, tmpRect1.centerX(), tmpRect1.centerY());
        add(item, LAYER_WORLD);
        fallingItems.add(item);
    }

    private void checkPaddleCollisions() {
        paddle.getRect(tmpRect2);
        float minDistance = paddle.getWidth() + ballRadius;

        for (int ballI = 0; ballI < balls.size(); ballI++) {
            Ball ball = balls.get(ballI);
            if (ball.y < paddle.getY() - minDistance)
                continue;
            if (!tmpRect2.intersects(
                    (int) (ball.x - ballRadius),
                    (int) (ball.y - ballRadius),
                    (int) (ball.x + ballRadius),
                    (int) (ball.y + ballRadius)))
                continue;

            Sound.getInstance().playBounce();

            if(ball.y > paddle.getY() && (ball.x > tmpRect2.right - ballRadius || ball.x < tmpRect2.left + ballRadius)) {
                tmpRect1.set(
                        (int)(ball.x - ballRadius),
                        (int)(ball.y - ballRadius),
                        (int)(ball.x + ballRadius),
                        (int)(ball.y + ballRadius));
                ball.onObjectCollision(tmpRect1, tmpRect2, true);
                ball.getVelocity(tmpVector);
                float speed = ball.getVelocity();
                float degrees = tmpVector.toDegrees();
                degrees = Math.min(315, Math.max(225, degrees));
                Vector.fromDegrees(degrees, tmpVector);
                ball.setVelocity(tmpVector.dx * speed, tmpVector.dy * speed);
                Log.d(Tag, "degrees: " + degrees);
                continue;
            }

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

            score.add((int)(200 * defaultScoreMultiplier));

            Assets assets = Assets.getInstance();
            if(item.frame == assets.itemSlowBall) {
                for(Ball ball : balls)
                    ball.setVelocity(ball.getVelocity() * (1 - 0.4f * ballSlowEffect));
                ballSlowEffect *= 0.6f;
            }
            else if(item.frame == assets.itemPierce) {
                piercing++;
            }
            else if(item.frame == assets.itemLaser) {
                shootingLaser++;
            }
            else if(item.frame == assets.itemEnlarge) {
                paddle.setWidth(paddle.getWidth() + 30);
            }
        }
    }

    private void destroyBall(Ball ball) {
        balls.remove(ball);
        remove(ball, LAYER_WORLD);
        if(balls.isEmpty()) {
            loseLife();
        }
    }

    private void createLaser(float x, float y) {
        Laser laser = lasers.getNew();
        laser.reset();
        laser.x = x;
        laser.y = y;
        add(laser, LAYER_WORLD);
    }

    private void updateLaserShot(GameTime gameTime) {
        if(shootingLaser > 0) {
            timeToLaser -= gameTime.getElapsedTime();
        }
        for(int li = lasers.inUse.size() - 1; li >= 0; li--) {
            Laser laser = lasers.inUse.get(li);
            for(int bi = bricks.size() - 1; bi >= 0; bi--) {
                Brick brick = bricks.get(bi);
                brick.getRect(tmpRect2);

                if(tmpRect2.intersects((int)laser.x - 1, (int)laser.y - 4, (int)laser.x + 1, (int)laser.y + 8)) {
                    hitBrick(brick);
                    laser.onHit();
                    break;
                }
            }

            if (laser.y < 0 || laser.destroyed()) {
                remove(laser, LAYER_WORLD);
                lasers.remove(laser);
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

    private void removeOldBackground() {
        if(oldBackground != null && oldBackground.alpha <= 0) {
            remove(oldBackground, LAYER_BACKGROUND);
            oldBackground = null;
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

    @Override
    protected void draw(GameTime gameTime, Canvas canvas) {
        canvas.drawARGB(255, 127, 127, 127);
        super.draw(gameTime, canvas);

        if(shootingLaser > 0 && timeToLaser <= 0) {
            paddle.getRect(tmpRect1);
            tmpRect2.set(tmpRect1.left + LASER_MARGIN - laserReadyEffect.rect.width(), tmpRect1.centerY() - laserReadyEffect.rect.height(), tmpRect1.left + LASER_MARGIN + laserReadyEffect.rect.width(), tmpRect1.centerY() + laserReadyEffect.rect.height());
            canvas.drawBitmap(laserReadyEffect.texture, laserReadyEffect.rect, tmpRect2, null);

            tmpRect2.set(tmpRect1.right - LASER_MARGIN - laserReadyEffect.rect.width(), tmpRect1.centerY() - laserReadyEffect.rect.height(), tmpRect1.right - LASER_MARGIN + laserReadyEffect.rect.width(), tmpRect1.centerY() + laserReadyEffect.rect.height());
            canvas.drawBitmap(laserReadyEffect.texture, laserReadyEffect.rect, tmpRect2, null);
        }
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
            if (Math.abs(paddle.getX() - event.x) < paddle.getWidth() &&
                    event.y > paddle.getY() - paddle.getWidth())
                trackTouchId = event.pointerId;

            if(shootingLaser > 0 && timeToLaser < 0) {
                paddle.getRect(tmpRect1);
                createLaser(tmpRect1.right - LASER_MARGIN, paddle.getY());
                createLaser(tmpRect1.left + LASER_MARGIN, paddle.getY());
                timeToLaser = LASER_DELAY;
            }
        }

        if (trackTouchId == event.pointerId) {
            if (event.type == TouchEvent.Type.RELEASE) {
                if(state == State.WAITING_RELEASE) {
                    replaceBall(lostLife);
                }
                else if(state == State.POSITION) {
                    setState(State.PLAYING);
                }
                trackTouchId = -1;
            }
            else if (state != State.WAITING_RELEASE)
                paddle.setPosition(event.x, paddle.getY());
        }
    }
}
