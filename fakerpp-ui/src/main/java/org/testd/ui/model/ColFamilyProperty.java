package org.testd.ui.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.util.*;
import java.util.stream.Collectors;

public class ColFamilyProperty {

    private final ObservableSet<ColProperty> cols;

    private final StringProperty field;
    private final StringProperty generator;

    private final BooleanProperty visible = new SimpleBooleanProperty();

    public ColFamilyProperty() {
        cols = FXCollections.observableSet(new LinkedHashSet<>());
        field = new SimpleStringProperty();
        generator = new SimpleStringProperty();
    }

    public ColFamilyProperty(ObservableSet<ColProperty> cols) {
        this.cols = cols;
        field = new SimpleStringProperty();
        generator = new SimpleStringProperty();
    }

    public Set<String> colsStr() {
        return cols.stream().map(ColProperty::getColName).collect(Collectors.toSet());
    }

    public ObservableSet<ColProperty> colsProperty() {
        return cols;
    }

    public StringProperty fieldProperty() {
        return field;
    }

    public StringProperty generatorProperty() {
        return generator;
    }

    public void deleteCols(Collection<String> colNames) {
        Set<ColProperty> needDeleted = cols.stream()
                .filter(cp -> colNames.contains(cp.getColName()))
                .collect(ImmutableSet.toImmutableSet());
        cols.removeAll(needDeleted);
        needDeleted.forEach(ColProperty::deleted);
    }

    public void deleteCol(String colName) {
        Optional<ColProperty> findCol = cols.stream()
                .filter(colProperty -> Objects.equals(colProperty.getColName(),
                        colName))
                .findFirst();

        findCol.ifPresent(
                colProperty -> {
                    cols.remove(colProperty);
                    colProperty.deleted();
                });
    }

    public void clear() {
        cols.forEach(ColProperty::deleted);
        cols.clear();
    }

    public void addCols(Collection<ColProperty> colNames) {
        cols.addAll(colNames);
    }

    public void replace(Set<ColProperty> newCols) {
        Set<ColProperty> deleted = Sets.difference(cols, newCols);
        deleted.forEach(ColProperty::deleted);

        addCols(newCols);
        cols.removeAll(deleted);
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }
}
