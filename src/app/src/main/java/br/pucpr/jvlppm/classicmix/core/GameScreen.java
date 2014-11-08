package br.pucpr.jvlppm.classicmix.core;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

import br.pucpr.jvlppm.classicmix.entities.GameEntity;

public class GameScreen <T> {
    public void handleTouch(TouchEvent event) { }

    public static interface FinishListener<T> {
        public void onFinished(T result);
    }

    public final GameActivity game;
    private final FinishListener<T> finishListener;
    private final List<GameEntity> entities;

    public GameScreen(GameActivity game, FinishListener<T> finishListener) {
        this.game = game;
        this.finishListener = finishListener;
        entities = new ArrayList<GameEntity>();
    }

    protected void update(GameTime gameTime) {
        for (GameEntity entity : entities)
            entity.update(gameTime);
    }

    protected void draw(GameTime gameTime, Canvas canvas) {
        for (GameEntity entity : entities)
            entity.draw(gameTime, canvas);
    }

    protected void exit(T result) {
        finishListener.onFinished(result);
    }

    public void add(GameEntity entity) {
        entities.add(entity);
    }

    public void remove(GameEntity entity) {
        entities.remove(entity);
    }
}
