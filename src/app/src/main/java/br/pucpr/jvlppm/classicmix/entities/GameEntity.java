package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Rect;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;

public class GameEntity {
    private final Rect tmpRect;

    public GameEntity() {
        tmpRect = new Rect();
    }

    public void update(GameTime gameTime) {

    }

    public void draw(GameTime gameTime, Canvas canvas) {

    }

    protected void draw(Canvas canvas, Frame frame, float x, float y, float originX, float originY) {
        draw(canvas, frame, x, y, originX, originY, 1);
    }

    protected void draw(Canvas canvas, Frame frame, float x, float y, float originX, float originY, float scale) {
        if(frame == null)
            return;
        float drawW = frame.rect.width() * scale;
        float drawH = frame.rect.height() * scale;
        float drawX = x - drawW * originX * scale;
        float drawY = y - drawH * originY * scale;

        tmpRect.set((int)drawX, (int)drawY, (int)(drawX + drawW), (int)(drawY + drawH));
        canvas.drawBitmap(frame.texture, frame.rect, tmpRect, null);
    }
}
