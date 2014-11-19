package br.pucpr.jvlppm.classicmix.core;

import android.graphics.Canvas;
import android.view.View;

public interface FrameBuffer {
    public void pause();
    public void resume();
    public Canvas getCanvas();
    public int getFrameBufferWidth();
    public int getFrameBufferHeight();
    public View getView();
}
