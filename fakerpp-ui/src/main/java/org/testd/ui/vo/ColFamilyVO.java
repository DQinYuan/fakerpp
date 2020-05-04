package org.testd.ui.vo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import org.testd.ui.model.ColProperty;
import org.testd.ui.model.TableProperty;
import org.testd.ui.util.BindingUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ColFamilyVO {

    private final ObservableSet<ColProperty> cols;

    private final ObservableList<TableProperty.GeneratorInfoProperty> generatorInfos;

    private final BooleanProperty visible = new SimpleBooleanProperty();

    /**
     * virtual colFamilyVo not related with a colFamilyProperty
     *
     * @param cols
     */
    public ColFamilyVO(ObservableSet<ColProperty> cols) {
        this.cols = cols;
        this.generatorInfos = FXCollections.observableArrayList();
    }

    public ColFamilyVO(TableProperty.ColFamilyProperty colFamilyProperty, Function<String, ColProperty> colPropertyCreater) {
        cols = FXCollections.observableSet(
                colFamilyProperty.getCols().stream().map(colPropertyCreater)
                        .collect(Collectors.toCollection(HashSet::new))
        );
        BindingUtil.mapContent(colFamilyProperty.getCols(),
                cols, ColProperty::getColName);
        if (colFamilyProperty.getGeneratorInfos().size() == 0) {
            // add default generator
            colFamilyProperty.getGeneratorInfos().add(TableProperty.GeneratorInfoProperty.defaultProperty());
        }

        generatorInfos = colFamilyProperty.getGeneratorInfos();
    }

    public Set<String> colsStr() {
        return cols.stream().map(ColProperty::getColName).collect(Collectors.toSet());
    }

    public ObservableSet<ColProperty> colsProperty() {
        return cols;
    }

    public ObservableList<TableProperty.GeneratorInfoProperty> getGeneratorInfos() {
        return generatorInfos;
    }

    public void deleteCols(Collection<String> colNames) {
        Set<ColProperty> needDeleted = cols.stream()
                .filter(cp -> colNames.contains(cp.getColName()))
                .collect(ImmutableSet.toImmutableSet());
        cols.removeAll(needDeleted);
        needDeleted.forEach(ColProperty::deleted);
    }


    public Set<ColProperty> moveCols(Collection<String> colNames) {
        Set<ColProperty> needDeleted = cols.stream()
                .filter(cp -> colNames.contains(cp.getColName()))
                .collect(ImmutableSet.toImmutableSet());
        cols.removeAll(needDeleted);
        return needDeleted;
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
