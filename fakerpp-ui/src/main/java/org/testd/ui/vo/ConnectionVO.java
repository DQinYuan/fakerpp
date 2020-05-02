package org.testd.ui.vo;

import com.google.common.collect.Sets;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.testd.ui.model.ColProperty;
import org.testd.ui.model.JoinType;
import org.testd.ui.model.TableProperty;
import org.testd.ui.util.BindingUtil;
import org.testd.ui.view.dynamic.MyTableView;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ConnectionVO {

    private final TableProperty.JoinProperty relatedJoinProperty;
    private final ObjectProperty<MyTableView> source;
    private Function<MyTableView, Predicate<String>> recvChecker = t -> s -> true;

    private final ObjectProperty<MyTableView> target;

    private final ObjectProperty<JoinType> joinType;

    private final BooleanProperty random;

    private final BooleanProperty visible = new SimpleBooleanProperty(false);

    // keySet bind with join send view
    // values bind with join receive view
    private final ObservableMap<ColProperty, ColProperty> sendRecv = FXCollections
            .observableMap(new LinkedHashMap<>());

    public ConnectionVO(MyTableView source,
                        MyTableView defaultTarget,
                        TableProperty.JoinProperty joinProperty,
                        JoinType joinType) {
        this.joinType = new SimpleObjectProperty<>(joinType);
        this.relatedJoinProperty = joinProperty;
        this.source = new SimpleObjectProperty<>(source);
        this.target = new SimpleObjectProperty<>(defaultTarget);
        this.random = joinProperty.getRandom();
        sendRecv.putAll(
                joinProperty.getMap().entrySet().stream()
                        .collect(Collectors.toMap(
                                en -> new ColProperty(en.getKey()),
                                en -> new ColProperty(en.getValue())
                        ))
        );
        BindingUtil.mapContentWithoutInit(joinProperty.getMap(),
                sendRecv,
                ColProperty::getColName,
                ColProperty::getColName);

        this.joinType.addListener((observable, oldValue, newValue) -> {
            if (visible.get()) {
                joinDelete();
                joinAdd();
            }
        });
    }

    public void joinAdd() {
        TableProperty.JoinsProperty targetTableJoinsProp =
                targetProperty().get().tableProperty().getJoins();
        this.joinType.get().handle(
                () -> targetTableJoinsProp.getLeftJoins().add(relatedJoinProperty),
                () -> targetTableJoinsProp.getRightJoins().add(relatedJoinProperty)
        );
    }

    public void joinDelete() {
        TableProperty.JoinsProperty targetTableJoinsProp =
                targetProperty().get().tableProperty().getJoins();
        targetTableJoinsProp.getLeftJoins().remove(relatedJoinProperty);
        targetTableJoinsProp.getRightJoins().remove(relatedJoinProperty);
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

    public BooleanProperty randomProperty() {
        return random;
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
