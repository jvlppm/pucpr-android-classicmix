package br.pucpr.jvlppm.classicmix.core;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Frame {
    public final Bitmap texture;
    public final Rect rect;
    public final float durationWeight;

    public Frame(Bitmap texture, Rect rect, float durationWeight) {
        this.texture = texture;
        this.rect = rect;
        this.durationWeight = durationWeight;
    }
}
