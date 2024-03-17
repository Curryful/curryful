package io.github.curryful.rest;

import java.util.function.BiConsumer;

import io.github.curryful.commons.collections.MutableMaybeHashMap;

/**
 * Represents a pair of values.
 */
public final class Pair<A, B> {

    public static final BiConsumer<MutableMaybeHashMap<String, String>, Pair<String, String>> putPairIntoMMHashMap =
			(map, pair) -> map.put(pair.getFirst(), pair.getSecond());

    private final A first;
    private final B second;

    private Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }
}

