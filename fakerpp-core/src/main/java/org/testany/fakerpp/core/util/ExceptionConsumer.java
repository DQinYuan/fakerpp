package org.testany.fakerpp.core.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface ExceptionConsumer<T, E extends Exception> {
    void accept(T t) throws E;

    static <T, E extends Exception> Consumer<T> sneakyConsumer(ExceptionConsumer<T, E> exceptionConsumer)
            throws E {

        return i -> {
            try {
                exceptionConsumer.accept(i);
            } catch (Exception ex) {
                ThrowUtil.throwCheckedUnchecked(ex);
            }
        };
    }
}