package br.pucpr.jvlppm.classicmix.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public class SimpleFrameBuffer extends View implements FrameBuffer {
    private final GameActivity game;
    private long currentTime;
    private float pendingUpdate;
    private final Rect frameBufferRect, canvasRect;

    private final Bitmap frameBuffer;
    private final Canvas frameBufferCanvas;

    public SimpleFrameBuffer(GameActivity game, int width, int height) {
        super(game);
        this.game = game;
        this.frameBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        this.frameBufferCanvas = new Canvas(frameBuffer);
        currentTime = System.nanoTime();
        canvasRect = new Rect();
        frameBufferRect = new Rect(0, 0, width, height);
    }

    public Canvas getCanvas() {
        return frameBufferCanvas;
    }

    public void pause() {
    }

    public void resume() {
    }

    float seconds(long nanoTime) {
        return nanoTime / 1000000000.0f;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.getClipBounds(canvasRect);

        long newTime = System.nanoTime();
        float frameTime = seconds(newTime - currentTime);
        currentTime = newTime;

        pendingUpdate += frameTime;

        float desiredUpdateTime = 0.01f;
        while (pendingUpdate >= desiredUpdateTime)
        {
            game.update();
            pendingUpdate -= desiredUpdateTime;
        }
        game.draw();

        canvas.drawBitmap(frameBuffer, frameBufferRect, canvasRect, null);

        invalidate();
    }

    public int getFrameBufferWidth() {
        return frameBuffer.getWidth();
    }

    public int getFrameBufferHeight() {
        return frameBuffer.getHeight();
    }

    @Override
    public View getView() {
        return this;
    }
}
