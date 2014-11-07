package br.pucpr.jvlppm.classicmix.core;

public class Vector {
    public float dx, dy;

    public float length() {
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    public static void fromDegrees(float degrees, Vector dest) {
        fromRadians((float)Math.toRadians(degrees) , dest);
    }

    public static void fromRadians(float radians, Vector dest) {
        dest.dx = (float)Math.cos(radians);
        dest.dy = -(float)Math.sin(radians);
    }
}
