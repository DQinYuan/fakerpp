package org.testd.ui.view.dynamic;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import lombok.RequiredArgsConstructor;
import org.javatuples.Pair;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.ui.DefaultsConfig;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.model.ColFamilyProperty;
import org.testd.ui.model.ColProperty;
import org.testd.ui.model.ConnectionProperty;
import org.testd.ui.view.MainWindowView;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConnectionView {

    private final FxWeaver fxWeaver;
    private final DefaultsConfig defaultsConfig;
    private final MainWindowView mainWindowView;
    private final PrimaryStageHolder primaryStageHolder;

    private JoinView joinSendView;
    private JoinView joinRecvView;
    // non null after visible
    private ConnectPolyLine connectLine;
    private Runnable listenerDeleter = () ->{};

    public void register(ConnectionProperty connectionProperty) {
        joinSendView = fxWeaver.loadControl(JoinView.class);
        joinSendView.init(connectionProperty.sendSet());
        joinRecvView = fxWeaver.loadControl(JoinView.class);
        joinRecvView.init(connectionProperty.recvSet());

        connectionProperty.sendSet().addListener((SetChangeListener<ColProperty>) change -> {
            if (change.getSet().isEmpty()) {
                connectionProperty.visibleProperty().set(false);
            }
        });

        connectionProperty.visibleProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        show(connectionProperty);
                    } else {
                        assert connectLine != null;
                        assert connectionProperty.sourceProperty().get() != null;
                        assert connectionProperty.targetProperty().get() != null;
                        listenerDeleter.run();
                        mainWindowView.removeFromDrawBoard(connectLine);
                        connectionProperty.sourceProperty().get()
                                .deleteTableColFamily(joinSendView);
                        connectionProperty.targetProperty().get()
                                .deleteTableColFamily(joinRecvView);
                    }
                });

        connectionProperty.setRecvChecker(
                tableView -> {
                    Set<String> checkCols =
                            tableView.getColFamiliesExcept(joinRecvView).stream()
                            .map(ColFamilyProperty::colsStr).flatMap(Set::stream)
                            .collect(Collectors.toSet());
                    return col -> !checkCols.contains(col);
                }
        );

        EventHandler<ActionEvent> editHandler = event -> {
            EditConnectionView newConnView =
                    fxWeaver.loadControl(EditConnectionView.class);
            newConnView.initFromConnectionProperty(connectionProperty);
            primaryStageHolder.newSceneInChild(newConnView);
        };
        EventHandler<ActionEvent> deleteHandler = event ->
            connectionProperty.visibleProperty().set(false);
        joinSendView.addMenuHandler(editHandler, deleteHandler);
        joinRecvView.addMenuHandler(editHandler, deleteHandler);

    }

    private void show(ConnectionProperty connectionProperty) {
        MyTableView sourceTableView =
                connectionProperty.sourceProperty().get();
        sourceTableView.addTableColFamily(joinSendView);

        MyTableView targetTableView =
                connectionProperty.targetProperty().get();
        targetTableView.addTableColFamily(joinRecvView);

        joinSendView.init1(true, connectionProperty.joinTypeProperty().get(),
                targetTableView.getName());
        joinRecvView.init1(false, connectionProperty.joinTypeProperty().get(),
                sourceTableView.getName());

        // line connect
        PosAndListenerDeleter sendPos = joinPosProperty(sourceTableView, joinSendView);
        PosAndListenerDeleter recvPos = joinPosProperty(targetTableView, joinRecvView);
        connectLine = new ConnectPolyLine(joinSendView, joinRecvView,
                sendPos.pos.getValue0(),
                sendPos.pos.getValue1(),
                recvPos.pos.getValue0(),
                recvPos.pos.getValue1(),
                defaultsConfig.getLineEvadeInterval()
        );

        mouseHoverHighlight(joinSendView);
        mouseHoverHighlight(joinRecvView);

        mainWindowView.appendInDrawBoard(connectLine);
        listenerDeleter = () -> {
            sendPos.deleter.run();
            recvPos.deleter.run();
        };
    }

    private void mouseHoverHighlight(Node node) {
        EventHandler<MouseEvent> handler = event -> {
            joinSendView.toggleEffect();
            joinRecvView.toggleEffect();
            connectLine.toggleEffect();
        };
        node.addEventHandler(MouseEvent.MOUSE_ENTERED, handler);
        node.addEventHandler(MouseEvent.MOUSE_EXITED, handler);
    }

    @RequiredArgsConstructor
    private static class PosAndListenerDeleter {
        private final Pair<DoubleProperty, DoubleProperty> pos;
        private final Runnable deleter;
    }

    private PosAndListenerDeleter joinPosProperty(MyTableView tableView,
                                                                 JoinView joinView) {
        tableView.flushColFamilyView(); // to compute accurately
        Pair<Double, Double> curPos = computeJoinViewPos(tableView, joinView);
        Pair<DoubleProperty, DoubleProperty> pos
                = new Pair<>(new SimpleDoubleProperty(curPos.getValue0()),
                new SimpleDoubleProperty(curPos.getValue1()));
        ChangeListener<Number> listener = (observable, oldValue, newValue) -> {
            Pair<Double, Double> newPos = computeJoinViewPos(tableView, joinView);
            pos.getValue0().set(newPos.getValue0());
            pos.getValue1().set(newPos.getValue1());
        };

        tableView.translateXProperty().addListener(listener);
        tableView.translateYProperty().addListener(listener);
        joinView.layoutYProperty().addListener(listener);
        Runnable listenerDeleter = () -> {
            tableView.translateXProperty().removeListener(listener);
            tableView.translateYProperty().removeListener(listener);
            tableView.layoutYProperty().removeListener(listener);
        };

        return new PosAndListenerDeleter(pos,
                listenerDeleter);
    }

    private Pair<Double, Double> computeJoinViewPos(MyTableView tableView, JoinView joinView) {
        if (joinView.getParent() == null) {
            return new Pair<>(0.0, 0.0);
        }

        return new Pair<>(
                tableView.getLayoutX() + tableView.getTranslateX(),
                tableView.getLayoutY()
                        + tableView.getTranslateY()
                        + joinView.getParent().getLayoutY()
                        + joinView.getLayoutY()
        );
    }

}
