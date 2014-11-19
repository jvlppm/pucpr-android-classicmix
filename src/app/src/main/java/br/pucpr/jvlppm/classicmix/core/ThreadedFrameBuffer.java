package br.pucpr.jvlppm.classicmix.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class ThreadedFrameBuffer extends SurfaceView implements FrameBuffer, Runnable {
    private final GameActivity game;
    private SurfaceHolder holder;

    private final Bitmap frameBuffer;
    private final Canvas frameBufferCanvas;

    private boolean running;
    private Thread renderThread;

    public ThreadedFrameBuffer(GameActivity game, int width, int height) {
        super(game);
        this.game = game;
        this.frameBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        this.frameBufferCanvas = new Canvas(frameBuffer);
        this.holder = getHolder();
    }

    public Canvas getCanvas() {
        return frameBufferCanvas;
    }

    @Override
    public View getView() {
        return this;
    }

    public void pause() {
        running = false;
        while(true) {
            try {
                renderThread.join();
                break;
            } catch (InterruptedException e) {
                // retry
            }
        }
    }

    public void resume() {
        running = true;
        renderThread = new Thread(this);
        renderThread.start();
    }

    float seconds(long nanoTime) {
        return nanoTime / 1000000000.0f;
    }

    @Override
    public void run() {
        Rect dstRect = new Rect();

        while ( running )
        {
            if(!holder.getSurface().isValid())
                continue;

            game.update();
            game.draw();

            Canvas canvas = holder.lockCanvas();
            canvas.getClipBounds(dstRect);
            canvas.drawBitmap(frameBuffer, null, dstRect, null);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public int getFrameBufferWidth() {
        return frameBuffer.getWidth();
    }

    public int getFrameBufferHeight() {
        return frameBuffer.getHeight();
    }
}
