package br.pucpr.jvlppm.classicmix.core;

public class TouchEvent {
    public Type type;
    public float x, y;
    public int pointerId;

    public static enum Type {
        PRESS,
        MOVE,
        RELEASE
    }
}
