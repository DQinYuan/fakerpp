package org.testd.ui.util;

import javafx.collections.ObservableList;

public class TypeUtil {

    @SuppressWarnings("unchecked")
    public static  <E,F> ObservableList<F> saftCast(ObservableList<E> origin, Class<F> castType) {
        return (ObservableList<F>) origin;
    }

}
