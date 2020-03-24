package org.testd.ui.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import org.testd.ui.view.dynamic.ColFamilyView;
import org.testd.ui.view.dynamic.MyTableView;

import java.util.LinkedHashSet;
import java.util.function.Function;
import java.util.function.Predicate;

public class ConnectionProperty {

    private final ObjectProperty<MyTableView> source;
    private Function<MyTableView, Predicate<String>> recvChecker = t -> s -> true;

    private final ObjectProperty<MyTableView> target = new SimpleObjectProperty<>();

    private final ObjectProperty<JoinType> joinType = new SimpleObjectProperty<>(JoinType.LEFT);

    private final BooleanProperty visible = new SimpleBooleanProperty(false);

    // bind with join send view
    private final ObservableSet<String> sendSet = FXCollections.observableSet(new LinkedHashSet<>());
    // bind with join receive view
    private final ObservableSet<String> recvSet = FXCollections.observableSet(new LinkedHashSet<>());

    public ConnectionProperty(MyTableView source) {
        this.source = new SimpleObjectProperty<>(source);
    }


    public ObjectProperty<MyTableView> sourceProperty() {
        return source;
    }

    public ObjectProperty<MyTableView> targetProperty() {
        return target;
    }

    public ObjectProperty<JoinType> joinTypeProperty() {
        return joinType;
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }

    public ObservableSet<String> sendSet() {
        return sendSet;
    }

    public ObservableSet<String> recvSet() {
        return recvSet;
    }

    public void setRecvChecker(Function<MyTableView, Predicate<String>> recvChecker) {
        this.recvChecker = recvChecker;
    }

    public Predicate<String> recvChecker(MyTableView targetTable) {
        return recvChecker.apply(targetTable);
    }
}
