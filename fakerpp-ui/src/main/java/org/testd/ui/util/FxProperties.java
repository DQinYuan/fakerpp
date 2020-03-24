package org.testd.ui.util;

import javafx.beans.property.Property;

import java.util.function.Consumer;

public class FxProperties {

    public static <T> void runIfExists(Property<T> property, Consumer<T> consumer) {
        if (property.getValue() != null) {
            consumer.accept(property.getValue());
        }
    }

}
