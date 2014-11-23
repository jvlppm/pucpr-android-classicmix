package br.pucpr.jvlppm.classicmix.entities;

import android.graphics.Canvas;
import android.graphics.Rect;

import br.pucpr.jvlppm.classicmix.core.Frame;
import br.pucpr.jvlppm.classicmix.core.GameTime;
import br.pucpr.jvlppm.classicmix.core.NinePatch;

public class GameEntity {
    private final Rect tmpRect;

    public GameEntity() {
        tmpRect = new Rect();
    }

    public void update(GameTime gameTime) {

    }

    public void draw(GameTime gameTime, Canvas canvas) {

    }

    protected void draw(Canvas canvas, NinePatch image, float x, float y, float originX, float originY, float width, float height) {
        float rowTopHeight = image.centerTop.rect.height();
        float rowBottomHeight = image.centerBottom.rect.height();
        float rowCenterHeight = height - rowTopHeight - rowBottomHeight;

        float startY = y - height * originY;
        float rowCenterTop = startY + rowTopHeight;
        float rowBottomTop = startY + height - rowBottomHeight;

        float colLeftWidth = image.leftCenter.rect.width();
        float colRightWidth = image.rightCenter.rect.width();
        float colCenterWidth = width - colLeftWidth - colRightWidth;

        float startX = x - width * originX;
        float colCenterLeft = startX + colLeftWidth;
        float colRightLeft = startX + width - colRightWidth;

        drawSized(canvas, image.leftTop,    startX,       startY, colLeftWidth, rowTopHeight);
        drawSized(canvas, image.leftCenter, startX, rowCenterTop, colLeftWidth, rowCenterHeight);
        drawSized(canvas, image.leftBottom, startX, rowBottomTop, colLeftWidth, rowBottomHeight);

        drawSized(canvas, image.centerTop,    colCenterLeft,       startY, colCenterWidth, rowTopHeight);
        drawSized(canvas, image.center,       colCenterLeft, rowCenterTop, colCenterWidth, rowCenterHeight);
        drawSized(canvas, image.centerBottom, colCenterLeft, rowBottomTop, colCenterWidth, rowBottomHeight);

        drawSized(canvas, image.rightTop,    colRightLeft,       startY, colRightWidth, rowTopHeight);
        drawSized(canvas, image.rightCenter, colRightLeft, rowCenterTop, colRightWidth, rowCenterHeight);
        drawSized(canvas, image.rightBottom, colRightLeft, rowBottomTop, colRightWidth, rowBottomHeight);
    }

    private void drawSized(Canvas canvas, Frame frame, float x, float y, float width, float height) {
        tmpRect.set((int)x, (int)y, (int)(x + width), (int)(y + height));
        canvas.drawBitmap(frame.texture, frame.rect, tmpRect, null);
    }

    protected void draw(Canvas canvas, Frame frame, float x, float y, float originX, float originY) {
        draw(canvas, frame, x, y, originX, originY, 1);
    }

    protected void draw(Canvas canvas, Frame frame, float x, float y, float originX, float originY, float scale) {
        if(frame == null)
            return;
        float drawW = frame.rect.width() * scale;
        float drawH = frame.rect.height() * scale;
        float drawX = x - drawW * originX;
        float drawY = y - drawH * originY;

        tmpRect.set((int)drawX, (int)drawY, (int)(drawX + drawW), (int)(drawY + drawH));
        canvas.drawBitmap(frame.texture, frame.rect, tmpRect, null);
    }
}
