package io.siggi.cubecore.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class SimpleIterator<T> implements Iterator<T> {
    private boolean determinedNext = false;
    private T nextValue = null;
    private boolean hasNext = false;

    public SimpleIterator() {
    }

    public abstract T getNextValue() throws NoSuchElementException;

    private void determineNext() {
        if (determinedNext)
            return;
        try {
            nextValue = getNextValue();
            hasNext = true;
        } catch (NoSuchElementException e) {
            nextValue = null;
            hasNext = false;
        }
        determinedNext = true;
    }

    @Override
    public boolean hasNext() {
        determineNext();
        return hasNext;
    }

    @Override
    public T next() {
        determineNext();
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        determinedNext = false;
        return nextValue;
    }
}
