package org.testd.ui.model;

import com.google.common.collect.Sets;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import org.testd.ui.view.dynamic.ColFamilyView;
import org.testd.ui.view.dynamic.MyTableView;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConnectionProperty {

    private final ObjectProperty<MyTableView> source;
    private Function<MyTableView, Predicate<String>> recvChecker = t -> s -> true;

    private final ObjectProperty<MyTableView> target = new SimpleObjectProperty<>();

    private final ObjectProperty<JoinType> joinType = new SimpleObjectProperty<>(JoinType.LEFT);

    private final BooleanProperty visible = new SimpleBooleanProperty(false);

    // bind with join send view
    private final ObservableSet<ColProperty> sendSet = FXCollections.observableSet(new LinkedHashSet<>());
    // bind with join receive view
    private final ObservableSet<ColProperty> recvSet = FXCollections.observableSet(new LinkedHashSet<>());

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

    public ObservableSet<ColProperty> sendSet() {
        return sendSet;
    }

    public void replaceSendSet(Set<ColProperty> replacer) {
        // cols in send set is redundant, so needn't call cols' deleted listener
        sendSet.addAll(replacer);
        sendSet.removeAll(Sets.difference(sendSet, replacer));
    }

    public ObservableSet<ColProperty> recvSet() {
        return recvSet;
    }

    public void replaceRecvSet(Set<ColProperty> replacer) {
        Set<ColProperty> deleted = Sets.difference(recvSet, replacer);
        deleted.forEach(ColProperty::deleted);
        recvSet.addAll(replacer);
        recvSet.removeAll(deleted);
    }

    public void setRecvChecker(Function<MyTableView, Predicate<String>> recvChecker) {
        this.recvChecker = recvChecker;
    }

    public Predicate<String> recvChecker(MyTableView targetTable) {
        return recvChecker.apply(targetTable);
    }
}
