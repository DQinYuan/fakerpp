package org.testd.ui.view;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.controller.DrawBoardController;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.model.TableProperty;
import org.testd.ui.util.BindingUtil;
import org.testd.ui.view.dynamic.FollowRightMouseMenu;
import org.testd.ui.view.dynamic.MyTableView;
import org.testd.ui.view.form.TableMetaConfView;
import org.testd.ui.vo.TableMetaVO;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DrawBoardView extends Pane {

    private final PrimaryStageHolder primaryStageHolder;
    private final FxWeaver fxWeaver;
    private final TableMetaConfView tableMetaConfView;
    private final DrawBoardController drawBoardController;

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
        Map<String, TableProperty> propMap = new HashMap<>();
        BindingUtil.mapContentWithFilter(
                propMap,
                getChildren(),
                node -> ((MyTableView)node).nameProperty(),
                node -> ((MyTableView)node).tableProperty(),
                node -> node instanceof MyTableView
        );

        BindingUtil.mapContentWithFilter(
                viewMap,
                getChildren(),
                node -> ((MyTableView)node).nameProperty(),
                node -> ((MyTableView)node),
                node -> node instanceof MyTableView
        );

        // init controller
        drawBoardController.init(propMap, getChildren());
    }

    private void handleNewTable(MouseEvent mouseEvent) {
        TableProperty tableProperty = new TableProperty("Test");
        TableMetaVO tableMetaVO = new TableMetaVO(tableProperty);
        primaryStageHolder.newSceneInChild(tableMetaConfView
                .getView(tableMetaVO,
                        name -> !drawBoardController.tableNameExists(name))
        );

        MyTableView table =
                fxWeaver.loadControl(MyTableView.class);
        table.initTableProperty(tableProperty);
        table.setTranslateX(mouseEvent.getX());
        table.setTranslateY(mouseEvent.getY());
        drawBoardController.append(table);
    }

    public MyTableView getTableByName(String name) {
        return viewMap.get(name);
    }

    public void init(Map<String, TableProperty> ermlTables) {
        // init exists tables in ermlTables
        // ...

        // binding
        BindingUtil.mapContentWithFilter(ermlTables,
                getChildren(),
                node -> ((MyTableView)node).nameProperty(),
                node -> ((MyTableView)node).tableProperty(),
                node -> node instanceof MyTableView);
    }
}
