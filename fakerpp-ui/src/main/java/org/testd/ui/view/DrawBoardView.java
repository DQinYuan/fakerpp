package org.testd.ui.view;

import com.google.common.collect.ImmutableList;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;
import org.testd.ui.controller.DrawBoardController;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.model.JoinType;
import org.testd.ui.model.TableProperty;
import org.testd.ui.util.BindingUtil;
import org.testd.ui.util.ResourceUtil;
import org.testd.ui.util.Stages;
import org.testd.ui.view.dynamic.ConnectionView;
import org.testd.ui.view.dynamic.FollowRightMouseMenu;
import org.testd.ui.view.dynamic.MyTableView;
import org.testd.ui.view.dynamic.TaskStateView;
import org.testd.ui.view.form.TableMetaConfView;
import org.testd.ui.vo.ConnectionVO;
import org.testd.ui.vo.TableMetaVO;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class DrawBoardView extends Pane {

    private final FxWeaver fxWeaver;
    private final TableMetaConfView tableMetaConfView;
    private final DrawBoardController drawBoardController;
    private final BeanFactory beanFactory;

    private Map<String, MyTableView> viewMap = new HashMap<>();

    @PostConstruct
    private void postConstruct() {
        // init draw board 'New Table' menu
        addEventHandler(MouseEvent.MOUSE_CLICKED,
                // getScene return null
                new FollowRightMouseMenu(false, this,
                        FollowRightMouseMenu.menuEntry("New Table",
                                mouseEvent -> event -> handleNewTable(mouseEvent)))
        );

        // init name maps to check duplicate
        // propMap and viewMap are all bound with children
        Map<String, TableProperty> propMap = new HashMap<>();
        BindingUtil.mapContentWithFilter(
                propMap,
                getChildren(),
                node -> ((MyTableView) node).nameProperty(),
                node -> ((MyTableView) node).tableProperty(),
                node -> node instanceof MyTableView
        );

        BindingUtil.mapContentWithFilter(
                viewMap,
                getChildren(),
                node -> ((MyTableView) node).nameProperty(),
                node -> ((MyTableView) node),
                node -> node instanceof MyTableView
        );

        // init controller
        drawBoardController.init(propMap, getChildren());
    }

    private void handleNewTable(MouseEvent mouseEvent) {
        TableProperty tableProperty = new TableProperty("Test");
        TableMetaVO tableMetaVO = new TableMetaVO(tableProperty);
        Stages.newSceneInChild(tableMetaConfView.getView(tableMetaVO,
                name -> !drawBoardController.tableNameExists(name),
                () -> {
                    MyTableView table =
                            fxWeaver.loadControl(MyTableView.class);
                    table.initTableProperty(tableProperty);
                    table.setTranslateX(mouseEvent.getX());
                    table.setTranslateY(mouseEvent.getY());
                    drawBoardController.append(table);
                }),
                getScene().getWindow());
    }

    public MyTableView getTableByName(String name) {
        return viewMap.get(name);
    }

    public void init(Set<TableProperty> ermlTables) {
        // init exists tables in ermlTables
        Map<String, MyTableView> tableViewMap = new HashMap<>();
        ermlTables.forEach(tableProperty -> {
            MyTableView myTableView = fxWeaver.loadControl(MyTableView.class);
            myTableView.initTableProperty(tableProperty);
            tableViewMap.put(tableProperty.getName().get(), myTableView);
            drawBoardController.append(myTableView);
        });

        // init exists join
        ermlTables.forEach(tableProperty -> {
            BiConsumer<TableProperty.JoinProperty, JoinType> showConnection = (jp, jt) -> {
                ConnectionView connectionView = beanFactory.getBean(ConnectionView.class);
                ConnectionVO connectionVO = new ConnectionVO(
                        tableViewMap.get(jp.getDepend().get()),
                        tableViewMap.get(tableProperty.getName().get()),
                        jp,
                        jt
                );
                connectionView.init(connectionVO);
                // show connection view
                connectionVO.visibleProperty().set(true);
            };

            List<TableProperty.JoinProperty> leftJoinCopy =
                    ImmutableList.copyOf(tableProperty.getJoins().getLeftJoins());
            List<TableProperty.JoinProperty> rightJoinCopy =
                    ImmutableList.copyOf(tableProperty.getJoins().getRightJoins());

            leftJoinCopy.forEach(joinProperty ->
                    showConnection.accept(joinProperty, JoinType.LEFT));
            rightJoinCopy.forEach(joinProperty ->
                    showConnection.accept(joinProperty, JoinType.RIGHT));
        });

        // binding
        BindingUtil.mapContentWithFilter(ermlTables,
                getChildren(),
                node -> ((MyTableView) node).tableProperty(),
                node -> node instanceof MyTableView);
    }
}
