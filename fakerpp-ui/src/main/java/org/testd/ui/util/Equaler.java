package org.testd.ui.util;

import java.util.function.Function;

@FunctionalInterface
public interface Equaler<T> {

    static <T> Equaler<T> natural() {
        return Object::equals;
    }

    static <T, F> Equaler<T> withExtrator(Function<T, F> extrator) {
        return (t1, t2) ->
                extrator.apply(t1).equals(extrator.apply(t2));
    }

    boolean equal(T t1, T t2);
}
