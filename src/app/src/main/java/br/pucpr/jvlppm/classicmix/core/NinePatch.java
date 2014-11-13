package br.pucpr.jvlppm.classicmix.core;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

public class NinePatch {
    static class Line {
        public final int start, end;

        public Line(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    public final Bitmap texture;
    public final Frame leftTop;
    public final Frame leftCenter;
    public final Frame leftBottom;
    public final Frame centerTop;
    public final Frame center;
    public final Frame centerBottom;
    public final Frame rightTop;
    public final Frame rightCenter;
    public final Frame rightBottom;

    public NinePatch(Bitmap texture) {
        this.texture = texture;
        Line topLine = getLine(1, 0, 0, 0);
        Line leftLine = getLine(0, 1, 0, 0);

        leftTop = new Frame(texture, new Rect(1, 1, topLine.start, leftLine.start), 1);
        leftCenter = new Frame(texture, new Rect(1, leftLine.start, topLine.start, leftLine.end), 1);
        leftBottom = new Frame(texture, new Rect(1, leftLine.end, topLine.start, texture.getHeight() - 1), 1);

        centerTop = new Frame(texture, new Rect(topLine.start, 1, topLine.end, leftLine.start), 1);
        center = new Frame(texture, new Rect(topLine.start, leftLine.start, topLine.end, leftLine.end), 1);
        centerBottom = new Frame(texture, new Rect(topLine.start, leftLine.end, topLine.end, texture.getHeight() - 1), 1);

        rightTop = new Frame(texture, new Rect(topLine.end, 1, texture.getWidth() - 1, leftLine.start), 1);
        rightCenter = new Frame(texture, new Rect(topLine.end, leftLine.start, texture.getWidth() - 1, leftLine.end), 1);
        rightBottom = new Frame(texture, new Rect(topLine.end, leftLine.end, texture.getWidth() - 1, texture.getHeight() - 1), 1);
    }

    private Line getLine(int dx, int dy, int x, int y) {
        int start = -1, length = 0;

        for (int i = 0; i < this.texture.getWidth(); i++) {
            int color = texture.getPixel(x + i * dx, y + i * dy);
            if (color == Color.BLACK) {
                length++;
                if (start < 0)
                    start = i;
            } else if (color == Color.TRANSPARENT) {
                if (start >= 0)
                    break;
            } else throw new RuntimeException("Invalid 9-patch image");
        }
        if (start >= 0)
            return new Line(start, start + length);
        return null;
    }

    public int height() {
        return centerTop.rect.height() + center.rect.height() + centerBottom.rect.height();
    }

    public int width() {
        return leftCenter.rect.width() + center.rect.width() + rightCenter.rect.width();
    }
}
