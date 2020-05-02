package org.testd.ui.view.dynamic;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
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
import org.testd.ui.controller.DrawBoardController;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.model.TableProperty;
import org.testd.ui.util.Stages;
import org.testd.ui.vo.ColFamilyVO;
import org.testd.ui.model.ColProperty;
import org.testd.ui.vo.ConnectionVO;
import org.testd.ui.util.BindingUtil;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * represent a compose of join send view, join receive view and a connection line
 */
@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConnectionView {

    private final FxWeaver fxWeaver;
    private final DefaultsConfig defaultsConfig;
    private final DrawBoardController drawBoardController;

    private JoinView joinSendView;
    private JoinView joinRecvView;
    // non null after visible
    private ConnectPolyLine connectLine;
    private Runnable listenerDeleter = () ->{};

    /**
     * relate this connection view with a connection property
     * @param connectionVO
     */
    public void init(ConnectionVO connectionVO) {

        // bind send and receive set
        ObservableSet<ColProperty> sendSet = FXCollections
                .observableSet(new LinkedHashSet<>());
        ObservableSet<ColProperty> recvSet = FXCollections
                .observableSet(new LinkedHashSet<>());
        BindingUtil.bindKeyValue(sendSet, recvSet, connectionVO.sendRecvMap());

        joinSendView = fxWeaver.loadControl(JoinView.class);
        joinSendView.init(sendSet);
        joinRecvView = fxWeaver.loadControl(JoinView.class);
        joinRecvView.init(recvSet);

        sendSet.addListener((SetChangeListener<ColProperty>) change -> {
            if (change.getSet().isEmpty()) {
                connectionVO.visibleProperty().set(false);
            }
        });

        connectionVO.visibleProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        show(connectionVO);
                        connectionVO.joinAdd();
                    } else {
                        assert connectLine != null;
                        assert connectionVO.sourceProperty().get() != null;
                        assert connectionVO.targetProperty().get() != null;
                        listenerDeleter.run();
                        drawBoardController.remove(connectLine);
                        connectionVO.sourceProperty().get()
                                .deleteTableColFamily(joinSendView);
                        connectionVO.targetProperty().get()
                                .deleteTableColFamily(joinRecvView);
                        connectionVO.joinDelete();
                    }
                });

        connectionVO.setRecvChecker(
                tableView -> {
                    Set<String> checkCols =
                            tableView.getColFamiliesExcept(joinRecvView).stream()
                            .map(ColFamilyVO::colsStr).flatMap(Set::stream)
                            .collect(Collectors.toSet());
                    return col -> !checkCols.contains(col);
                }
        );

        EventHandler<ActionEvent> editHandler = event -> {
            EditConnectionView newConnView =
                    fxWeaver.loadControl(EditConnectionView.class);
            newConnView.initFromConnectionProperty(connectionVO);
            Stages.newSceneInChild(newConnView, connectLine.getScene().getWindow());
        };
        EventHandler<ActionEvent> deleteHandler = event ->
            connectionVO.visibleProperty().set(false);
        joinSendView.addMenuHandler(editHandler, deleteHandler);
        joinRecvView.addMenuHandler(editHandler, deleteHandler);

    }

    private void show(ConnectionVO connectionVO) {
        MyTableView sourceTableView =
                connectionVO.sourceProperty().get();
        sourceTableView.addTableColFamily(joinSendView);

        MyTableView targetTableView =
                connectionVO.targetProperty().get();
        targetTableView.addTableColFamily(joinRecvView);

        joinSendView.init1(true, connectionVO.joinTypeProperty().get(),
                targetTableView.getName());
        joinRecvView.init1(false, connectionVO.joinTypeProperty().get(),
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

        drawBoardController.append(connectLine);
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
