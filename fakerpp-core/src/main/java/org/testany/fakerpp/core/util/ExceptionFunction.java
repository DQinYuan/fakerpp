package org.testany.fakerpp.core.util;

import java.util.function.Function;

@FunctionalInterface
public interface ExceptionFunction<T, R, E extends Exception> {

    R apply(T t);

    static <T, R, E extends Exception> Function<T, R> sneakyFunction(ExceptionFunction<T, R, E> exceptionFunction)
            throws E {

        return i -> {
            try {
                return exceptionFunction.apply(i);
            } catch (Exception ex) {
                ThrowUtil.throwCheckedUnchecked(ex);
            }
            return null;
        };
    }
}
