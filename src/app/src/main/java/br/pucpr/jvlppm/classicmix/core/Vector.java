package br.pucpr.jvlppm.classicmix.core;

public class Vector {
    public float dx, dy;

    public static void fromDegrees(float degrees, Vector dest) {
        fromRadians((float)Math.toRadians(degrees) , dest);
    }

    public static void fromRadians(float radians, Vector dest) {
        dest.dx = (float)Math.cos(radians);
        dest.dy = -(float)Math.sin(radians);
    }

    public float getLength() {
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    public void setLength(float length) {
        normalize();
        dx *= length;
        dy *= length;
    }

    public void normalize() {
        float length = getLength();
        dx /= length;
        dy /= length;
    }
}
