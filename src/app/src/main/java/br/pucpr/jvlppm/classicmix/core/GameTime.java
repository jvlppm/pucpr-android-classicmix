package br.pucpr.jvlppm.classicmix.core;

public class GameTime {
    private float total;
    private float elapsed;
    private long lastNanoTime;

    public GameTime() {
        total = 0;
        elapsed = 0;
        lastNanoTime = System.nanoTime();
    }

    public void tick() {
        long nanoTime = System.nanoTime();
        elapsed = (nanoTime-lastNanoTime) / 1000000000.0f;
        total += elapsed;
        lastNanoTime = nanoTime;
    }

    public float getTotalTime() {
        return total;
    }

    public float getElapsedTime() {
        return elapsed;
    }
}
