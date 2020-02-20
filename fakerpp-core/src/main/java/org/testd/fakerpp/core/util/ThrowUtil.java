package org.testd.fakerpp.core.util;

public class ThrowUtil {

    @SuppressWarnings("unchecked")
    static <X extends Throwable> void throwCheckedUnchecked(Throwable t) throws X {
        throw (X) t;
    }

}
