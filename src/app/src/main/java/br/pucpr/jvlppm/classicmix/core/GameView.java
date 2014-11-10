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

    private long lastDrawDuration, lastDraw;
    private final long maxRenderDelay = 1000000000 / 45;

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

    float seconds(long nanoTime) {
        return nanoTime / 1000000000.0f;
    }

    @Override
    public void run() {
        Rect dstRect = new Rect();
        long currentTime = System.nanoTime();
        float pendingUpdate = 0;
        float desiredUpdateTime = 0.01f;
        while ( running )
        {
            if(!holder.getSurface().isValid())
                continue;

            long newTime = System.nanoTime();
            float frameTime = seconds(newTime - currentTime);
            currentTime = newTime;

            pendingUpdate += frameTime;

            while (pendingUpdate >= desiredUpdateTime)
            {
                game.update();
                pendingUpdate -= desiredUpdateTime;
            }
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
