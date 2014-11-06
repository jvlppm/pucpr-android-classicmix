package br.pucpr.jvlppm.classicmix.core;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

public abstract class GameActivity extends Activity {
    private GameTime gameTime;
    private GameView gameView;
    private Canvas gameViewCanvas;
    private GameScreen currentScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestFullScreen();

        gameTime = new GameTime();
        setGameView(createGameView());
    }

    private void requestFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    void tick() {
        gameTime.tick();

        if(currentScreen != null) {
            currentScreen.update(gameTime);
            currentScreen.draw(gameTime, gameViewCanvas);
        }
    }

    public void setCurrentScreen(GameScreen currentScreen) {
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
        return currentScreen != null && currentScreen.handleTouch(event, touchX, touchY);
    }
}
