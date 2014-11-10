package br.pucpr.jvlppm.classicmix.core;

public class GameTime {
    private float total;
    private float elapsed;
    private long lastNanoTime;
    private Float maxElapsedTime;

    public GameTime() {
        total = 0;
        elapsed = 0;
        lastNanoTime = System.nanoTime();
    }

    public void tick() {
        long nanoTime = System.nanoTime();
        elapsed = (nanoTime-lastNanoTime) / 1000000000.0f;
        if (elapsed > maxElapsedTime)
            elapsed = maxElapsedTime;
        total += elapsed;
        lastNanoTime = nanoTime;
    }

    public void setMaxElapsedTime(Float maxElapsedTime) {
        this.maxElapsedTime = maxElapsedTime;
    }

    public float getTotalTime() {
        return total;
    }

    public float getElapsedTime() {
        return elapsed;
    }
}
