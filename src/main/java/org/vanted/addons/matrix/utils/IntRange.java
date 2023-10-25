package org.vanted.addons.matrix.utils;

import java.util.Iterator;

/**
 * @author Benjamin Moser.
 */
public class IntRange implements Iterable<Integer> {

    private int startInclusive;
    private int endInclusive;

    public IntRange(int startInclusive, int endInclusive) {
        this.startInclusive = startInclusive;
        this.endInclusive = endInclusive;
    }

    @Override
    public Iterator<Integer> iterator() {

        Iterator<Integer> myIt = new Iterator<Integer>() {

            int i = startInclusive;

            @Override
            public boolean hasNext() {
                return i <= endInclusive;
            }

            @Override
            public Integer next() {
                return i++; // increment but return old value
            }
        };

        return myIt;
    }
}
