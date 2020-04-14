package org.testd.ui.model;

import com.google.common.collect.Sets;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import org.testd.ui.view.dynamic.ColFamilyView;
import org.testd.ui.view.dynamic.MyTableView;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConnectionProperty {

    private final ObjectProperty<MyTableView> source;
    private Function<MyTableView, Predicate<String>> recvChecker = t -> s -> true;

    private final ObjectProperty<MyTableView> target = new SimpleObjectProperty<>();

    private final ObjectProperty<JoinType> joinType = new SimpleObjectProperty<>(JoinType.LEFT);

    private final BooleanProperty visible = new SimpleBooleanProperty(false);

    // keySet bind with join send view
    // values bind with join receive view
    private final ObservableMap<ColProperty, ColProperty> sendRecv = FXCollections
            .observableMap(new LinkedHashMap<>());

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

    public ObservableMap<ColProperty, ColProperty> sendRecvMap() {
        return sendRecv;
    }

    public void replaceSendRecv(Map<ColProperty, ColProperty> newSendRecv) {
        Sets.SetView<ColProperty> deleted =
                Sets.difference(sendRecv.keySet(), newSendRecv.keySet());
        sendRecv.putAll(newSendRecv);
        deleted.forEach(sendRecv::remove);
    }

    public void setRecvChecker(Function<MyTableView, Predicate<String>> recvChecker) {
        this.recvChecker = recvChecker;
    }

    public Predicate<String> recvChecker(MyTableView targetTable) {
        return recvChecker.apply(targetTable);
    }
}
