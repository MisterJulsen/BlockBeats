package de.mrjulsen.blockbeats.core;

import java.util.ArrayList;

public class OrderedArrayList<T> extends ArrayList<T> {

    public void moveForth(int srcIndex, int amount) {
        T obj = remove(srcIndex);
        add(srcIndex + amount, obj);
    }

    public void moveBack(int srcIndex, int amount) {
        T obj = remove(srcIndex);
        add(srcIndex - amount, obj);
    }

    public void moveToStart(int srcIndex) {
        moveBack(srcIndex, srcIndex);
    }

    public void moveToEnd(int srcIndex) {
        moveForth(srcIndex, size() - 1 - srcIndex);
    }
}
