package org.testd.ui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ColFamilyProperty {

    private final ObservableList<String> cols;

    private final StringProperty field;
    private final StringProperty generator;

    public ColFamilyProperty() {
        cols = FXCollections.observableArrayList();
        field = new SimpleStringProperty();
        generator = new SimpleStringProperty();
    }

    public ObservableList<String> colsProperty() {
        return cols;
    }

    public StringProperty fieldProperty() {
        return field;
    }

    public StringProperty generatorProperty() {
        return generator;
    }

    public void deleteCols(Collection<String> colNames) {
        List<String> needDeleted = cols.stream()
                .filter(col -> colNames.contains(col))
                .collect(Collectors.toList());
        needDeleted.forEach(cols::remove);
    }

    public void addCols(Collection<String> colNames) {
        cols.addAll(colNames);
    }

    public void clearCols() {
        cols.clear();
    }
}
