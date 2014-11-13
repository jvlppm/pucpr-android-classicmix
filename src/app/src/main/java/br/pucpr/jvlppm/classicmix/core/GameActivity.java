package br.pucpr.jvlppm.classicmix.core;

import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import br.pucpr.jvlppm.classicmix.activities.SoundActivity;

public abstract class GameActivity extends SoundActivity {
    private GameTime updateGameTime, drawGameTime;
    private GameView gameView;
    private Canvas gameViewCanvas;
    private Scene currentScreen;
    private final List<TouchEvent> unhandledTouches, handledTouches;

    protected GameActivity() {
        unhandledTouches = new ArrayList<TouchEvent>();
        handledTouches = new ArrayList<TouchEvent>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestFullScreen();

        updateGameTime = new GameTime();
        updateGameTime.setMaxElapsedTime(0.05f);

        drawGameTime = new GameTime();
        drawGameTime.setMaxElapsedTime(0.5f);

        setGameView(createGameView());
    }

    private void requestFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onPause() {
        gameView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGameTime.tick();
        drawGameTime.tick();
        gameView.resume();
    }

    protected final void setGameView(GameView gameView) {
        this.gameView = gameView;
        this.gameViewCanvas = gameView.getCanvas();
        setContentView(gameView);
    }

    protected int getWidth() {
        return gameView.getWidth();
    }

    protected int getHeight() {
        return gameView.getHeight();
    }

    public int getFrameBufferWidth() {
        return this.gameView.getFrameBufferWidth();
    }

    public int getFrameBufferHeight() {
        return this.gameView.getFrameBufferHeight();
    }

    public float toFrameBufferX(float x) {
        return (x / (float) getWidth()) * getFrameBufferWidth();
    }

    public float toFrameBufferY(float y) {
        return (y / (float) getHeight()) * getFrameBufferHeight();
    }

    protected GameView createGameView() {
        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        int width = isLandscape ? 480 : 320;
        int height = isLandscape ? 320 : 480;
        return new GameView(this, width, height);
    }

    void update() {
        updateGameTime.tick();

        while(unhandledTouches.size() > 0) {
            TouchEvent touch;
            synchronized (unhandledTouches) {
                touch = unhandledTouches.remove(0);
            }
            currentScreen.handleTouch(touch);
            synchronized (handledTouches) {
                handledTouches.add(touch);
            }
        }

        if(currentScreen != null) {
            currentScreen.update(updateGameTime);
        }
    }

    void draw() {
        drawGameTime.tick();

        if(currentScreen != null) {
            currentScreen.draw(drawGameTime, gameViewCanvas);
        }
    }

    public void setCurrentScreen(Scene currentScreen) {
        this.currentScreen = currentScreen;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = toFrameBufferX(event.getX());
        float touchY = toFrameBufferY(event.getY());

        return handleTouch(event, touchX, touchY) ||
                super.onTouchEvent(event);
    }

    protected boolean handleTouch(MotionEvent event, float touchX, float touchY) {
        if(currentScreen == null)
            return false;

        TouchEvent.Type type;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                type = TouchEvent.Type.PRESS;
                break;
            case MotionEvent.ACTION_MOVE:
                type = TouchEvent.Type.MOVE;
                break;
            case MotionEvent.ACTION_UP:
                type = TouchEvent.Type.RELEASE;
                break;

            default: return false;
        }

        TouchEvent touch;
        if(handledTouches.size() > 0) {
            synchronized (handledTouches) {
                touch = handledTouches.remove(0);
            }
        }
        else touch = new TouchEvent();

        touch.type = type;
        touch.x = touchX;
        touch.y = touchY;
        touch.pointerId = event.getPointerId(0);

        synchronized (unhandledTouches) {
            unhandledTouches.add(touch);
        }
        return true;
    }
}
