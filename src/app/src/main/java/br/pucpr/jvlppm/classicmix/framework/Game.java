package br.pucpr.jvlppm.classicmix.framework;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.os.Bundle;

public abstract class Game extends Activity {
    GameTime gameTime;
    FrameBuffer frameBuffer;
    Canvas frameBufferCanvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameTime = new GameTime();
        setFrameBuffer(createFrameBuffer());
    }

    final void setFrameBuffer(FrameBuffer frameBuffer) {
        this.frameBuffer = frameBuffer;
        this.frameBufferCanvas = frameBuffer.getCanvas();
        setContentView(frameBuffer);
    }

    protected FrameBuffer createFrameBuffer() {
        boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        int width = isLandscape ? 480 : 320;
        int height = isLandscape ? 320 : 480;
        return new FrameBuffer(this, width, height);
    }

    void tick() {
        gameTime.tick();
        update(gameTime);
        draw(gameTime, frameBufferCanvas);
    }

    protected abstract void update(GameTime gameTime);
    protected abstract void draw(GameTime gameTime, Canvas canvas);
}
