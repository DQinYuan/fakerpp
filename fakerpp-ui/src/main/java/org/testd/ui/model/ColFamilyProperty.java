package org.testd.ui.model;

import com.google.common.collect.Sets;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ColFamilyProperty {

    private final ObservableSet<String> cols;

    private final StringProperty field;
    private final StringProperty generator;

    private final BooleanProperty visible = new SimpleBooleanProperty();

    public ColFamilyProperty() {
        cols = FXCollections.observableSet(new LinkedHashSet<>());
        field = new SimpleStringProperty();
        generator = new SimpleStringProperty();
    }

    public ColFamilyProperty(ObservableSet<String> cols) {
        this.cols = cols;
        field = new SimpleStringProperty();
        generator = new SimpleStringProperty();
    }

    public ObservableSet<String> colsProperty() {
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

    public void replace(Set<String> newCols) {
        addCols(newCols);
        removeCols(Sets.difference(cols, newCols));
    }

    public void removeCols(Collection<String> colNames) {
        cols.removeAll(colNames);
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }
}
