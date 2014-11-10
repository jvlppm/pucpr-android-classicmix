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
import br.pucpr.jvlppm.classicmix.core.GameScreen;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.core.TouchEvent;
import br.pucpr.jvlppm.classicmix.core.Vector;
import br.pucpr.jvlppm.classicmix.entities.Background;
import br.pucpr.jvlppm.classicmix.entities.Ball;
import br.pucpr.jvlppm.classicmix.entities.Brick;
import br.pucpr.jvlppm.classicmix.entities.Image;
import br.pucpr.jvlppm.classicmix.entities.ExtraLifeCounter;
import br.pucpr.jvlppm.classicmix.entities.Item;
import br.pucpr.jvlppm.classicmix.entities.Paddle;
import br.pucpr.jvlppm.classicmix.entities.Score;
import br.pucpr.jvlppm.classicmix.services.Assets;

public class GamePlayScreen extends GameScreen {
    private static enum State { WAITING, PLAYING, GAME_OVER };
    private final static int LAYER_BACKGROUND = 0;
    private final static int LAYER_WORLD = 1;
    private final static int LAYER_GUI = 2;

    static final String Tag = "GamePlayScreen";

    private final Score score;
    private final ExtraLifeCounter lifeCounter;
    private final Paddle paddle;
    private final List<Ball> balls;
    private final List<Brick> bricks;
    private final List<Item> fallingItems;
    private Background currentBackground;
    private Background oldBackground;

    private final Image msgMoveToBegin, msgGameOver;
    private final Rect tmpRectPaddle, tmpRectObj;
    private final Vector tmpVector;
    private final Random random;

    private int currentLevel;

    private int trackTouchId;
    private float ballRadius;
    private float brickRadiusX, brickRadiusY;

    private float ballSlowEffect;
    private boolean piercing;

    private State state;

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

        ballRadius = assets.ballBlue.texture.getWidth() / 2;
        brickRadiusX = assets.brickBlue.texture.getWidth() / 2;
        brickRadiusY = assets.brickBlue.texture.getHeight() / 2;
        msgMoveToBegin = new Image(assets.msgMoveToBegin, Image.Alignment.Center);
        msgGameOver = new Image(assets.msgGameOver, Image.Alignment.Center);

        tmpRectPaddle = new Rect();
        tmpRectObj = new Rect();
        tmpVector = new Vector();

        random = new Random(System.nanoTime());

        reset();
    }

    void startLevel(int level) {
        currentLevel = level;
        resetBall();
        resetItems();
        removeFallingItems();
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
        Frame backgroundImage = Assets.getInstance().byName.get("background" + (level + 1));
        if (backgroundImage != null) {
            Background background = new Background(
                    backgroundImage,
                    game.getFrameBufferWidth(),
                    game.getFrameBufferHeight());

            if(currentBackground != null) {
                oldBackground = currentBackground;
                background.fadeIn(oldBackground);
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
    }

    private void reset() {
        score.reset();
        lifeCounter.setExtraLives(3);
        resetPaddle();
        startLevel(0);
    }

    private void startBallMovement() {
        paddle.getRect(tmpRectPaddle);

        for(Ball ball : balls) {
            float targetX = tmpRectPaddle.left + (random.nextFloat() * 0.2f + 0.4f) * tmpRectPaddle.width();
            float targetY = tmpRectPaddle.top;

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
            tmpRectObj.set((int) (ball.x - ballRadius),
                    (int) (ball.y - ballRadius),
                    (int) (ball.x + ballRadius),
                    (int) (ball.y + ballRadius));
            for(int i = bricks.size() - 1; i >= 0; i--) {
                Brick brick = bricks.get(i);
                tmpRectPaddle.set(
                        (int) (brick.x - brickRadiusX),
                        (int) (brick.y - brickRadiusY),
                        (int) (brick.x + brickRadiusX),
                        (int) (brick.y + brickRadiusY));

                if(tmpRectObj.intersects(tmpRectPaddle.left, tmpRectPaddle.top, tmpRectPaddle.right, tmpRectPaddle.bottom)) {
                    if(brick.strength > 1)
                        score.add(30);

                    if(piercing)
                        brick.strength = 0;
                    else
                        ball.onBrickCollision(brick, tmpRectObj, tmpRectPaddle);

                    if (brick.strength <= 0) {
                        remove(brick, LAYER_WORLD);
                        dropItems(brick);
                        bricks.remove(i);
                        score.add(100);
                    }
                }
            }
        }

        if(bricks.size() <= 0) {
            startLevel(currentLevel + 1);
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
        paddle.getRect(tmpRectPaddle);

        for (Ball ball : balls) {
            if(ball.y > paddle.getY() ||
               !tmpRectPaddle.intersects(
                    (int) (ball.x - ballRadius),
                    (int) (ball.y - ballRadius),
                    (int) (ball.x + ballRadius),
                    (int) (ball.y + ballRadius)))
                continue;

            ball.getVelocity(tmpVector);

            float speed = tmpVector.getLength();
            float position = (ball.x - tmpRectPaddle.left) / tmpRectPaddle.width();
            float variation = random.nextFloat() * 0.1f - 0.05f;

            float degrees = 180 - (position * 0.95f + variation) * 180;
            degrees = Math.min(160, Math.max(20, degrees));
            Vector.fromDegrees(degrees, tmpVector);
            ball.setVelocity(tmpVector.dx * speed, tmpVector.dy * speed);
        }
    }

    private void checkFallingItems() {
        paddle.getRect(tmpRectPaddle);

        for(int i = fallingItems.size() - 1; i >= 0; i--) {
            Item item = fallingItems.get(i);
            item.getRect(tmpRectObj);
            if(tmpRectObj.top > game.getFrameBufferHeight()) {
                fallingItems.remove(i);
                remove(item, LAYER_WORLD);
                continue;
            }

            if(!tmpRectPaddle.intersects(tmpRectObj.left, tmpRectObj.top, tmpRectObj.right, tmpRectObj.bottom))
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

    @Override
    protected void update(GameTime gameTime) {
        super.update(gameTime);
        checkWallCollisions();
        checkBrickCollisions();
        checkPaddleCollisions();
        checkFallingItems();
        removeOldBackground();
    }

    private void removeOldBackground() {
        if(oldBackground != null && currentBackground.alpha >= 1) {
            remove(oldBackground, LAYER_BACKGROUND);
            oldBackground = null;
        }
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
