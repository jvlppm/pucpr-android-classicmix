package br.pucpr.jvlppm.classicmix.core;

import java.util.ArrayList;
import java.util.List;

public class Pool<T> {
    public final List<T> inUse;
    private final List<T> unused;
    private final Class<T> clazz;

    public Pool(Class<T> clazz) {
        this.clazz = clazz;
        inUse = new ArrayList<T>();
        unused = new ArrayList<T>();
    }

    public T getNew() {
        T item;
        try {
            if(unused.size() > 0)
                item = unused.remove(0);
            else
                item = clazz.newInstance();
            inUse.add(item);
            return item;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void remove(T item) {
        inUse.remove(item);
        unused.add(item);
    }
}
