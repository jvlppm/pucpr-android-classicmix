package br.pucpr.jvlppm.classicmix.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {
    private final GameActivity game;
    private SurfaceHolder holder;

    private final Bitmap frameBuffer;
    private final Canvas frameBufferCanvas;

    private boolean running;
    private Thread renderThread;

    public GameView(GameActivity game, int width, int height) {
        super(game);
        this.game = game;
        this.frameBuffer = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        this.frameBufferCanvas = new Canvas(frameBuffer);
        this.holder = getHolder();
    }

    public Canvas getCanvas() {
        return frameBufferCanvas;
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

    @Override
    public void run() {
        Rect dstRect = new Rect();
        while(running) {
            if(!holder.getSurface().isValid())
                continue;

            game.tick();

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
