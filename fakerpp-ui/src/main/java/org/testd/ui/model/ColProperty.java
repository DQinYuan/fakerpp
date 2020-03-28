package org.testd.ui.model;

import java.util.Objects;
import java.util.function.Consumer;

public class ColProperty {

    private final String colName;
    private Consumer<String> deleteListener = colName -> {};

    public ColProperty(String colName) {
        this.colName = colName;
    }

    public String getColName() {
        return colName;
    }

    public void deleted() {
        if (deleteListener != null) {
            deleteListener.accept(colName);
        }
    }

    public void addDeleteListener(Consumer<String> listener) {
        deleteListener = deleteListener.andThen(listener);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColProperty)) return false;
        ColProperty that = (ColProperty) o;
        return colName.equals(that.colName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(colName);
    }
}
