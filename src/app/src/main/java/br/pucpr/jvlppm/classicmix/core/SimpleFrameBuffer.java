package br.pucpr.jvlppm.classicmix.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public class SimpleFrameBuffer extends View implements FrameBuffer {
    private final GameActivity game;
    //private long currentTime;
    //private float pendingUpdate;
    private final Rect canvasRect;

    private final Bitmap frameBuffer;
    private final Canvas frameBufferCanvas;

    public SimpleFrameBuffer(GameActivity game, int width, int height) {
        super(game);
        this.game = game;
        this.frameBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        this.frameBufferCanvas = new Canvas(frameBuffer);
        //currentTime = System.nanoTime();
        canvasRect = new Rect();
    }

    public Canvas getCanvas() {
        return frameBufferCanvas;
    }

    public void pause() {
    }

    public void resume() {
    }

//    float seconds(long nanoTime) {
//        return nanoTime / 1000000000.0f;
//    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        //long newTime = System.nanoTime();
        //currentTime = newTime;

        //pendingUpdate += seconds(newTime - currentTime);

        //float desiredUpdateTime = 0.01f;
        //while (pendingUpdate >= desiredUpdateTime)
        //{
            game.update();
        //    pendingUpdate -= desiredUpdateTime;
        //}
        game.draw();

        canvas.getClipBounds(canvasRect);
        canvas.drawBitmap(frameBuffer, null, canvasRect, null);

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
