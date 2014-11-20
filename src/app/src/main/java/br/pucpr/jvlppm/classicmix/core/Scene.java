package br.pucpr.jvlppm.classicmix.core;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import br.pucpr.jvlppm.classicmix.entities.GameEntity;

public class Scene<T> {
    public void handleTouch(TouchEvent event) { }

    public static interface FinishListener<T> {
        public void onFinished(T result);
    }

    public final GameActivity game;
    private final FinishListener<T> finishListener;
    private final TreeMap<Integer, List<GameEntity>> entities;
    private final List<Runnable> pendingOperations;
    private boolean ignorePendingOperations;

    public Scene(GameActivity game, FinishListener<T> finishListener) {
        this.game = game;
        this.finishListener = finishListener;
        entities = new TreeMap<Integer, List<GameEntity>>();
        pendingOperations = new ArrayList<Runnable>();
    }

    protected void update(GameTime gameTime) {
        for (Integer layer : entities.keySet()) {
            List<GameEntity> layerEntities = entities.get(layer);
            for(int i  = 0; i < layerEntities.size(); i++) {
                GameEntity entity = layerEntities.get(i);
                entity.update(gameTime);
            }
        }
        runPendingOperations();
    }

    public void post(Runnable runnable) {
        pendingOperations.add(runnable);
    }

    private void runPendingOperations() {
        int max = pendingOperations.size();
        for(int i = 0; i < max; i++) {
            if(ignorePendingOperations)
                break;
            pendingOperations.get(0).run();
            pendingOperations.remove(0);
        }
        ignorePendingOperations = false;
    }

    protected void clearPendingOperations() {
        ignorePendingOperations = true;
        pendingOperations.clear();
    }

    protected void draw(GameTime gameTime, Canvas canvas) {
        for (Integer layer : entities.keySet()) {
            for (GameEntity entity : entities.get(layer))
                entity.draw(gameTime, canvas);
        }
    }

    protected void exit(T result) {
        finishListener.onFinished(result);
    }

    public void add(GameEntity entity, int layer) {
        if(entities.containsKey(layer))
            entities.get(layer).add(entity);
        else {
            List<GameEntity> layerEntities = new ArrayList<GameEntity>();
            layerEntities.add(entity);
            entities.put(layer, layerEntities);
        }
    }

    public void remove(GameEntity entity, int layer) {
        if(entities.containsKey(layer))
            entities.get(layer).remove(entity);
    }
}
