package org.testd.ui.view;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.model.TableMetaProperty;
import org.testd.ui.util.BindingUtil;
import org.testd.ui.view.dynamic.FollowRightMouseMenu;
import org.testd.ui.view.dynamic.MyTableView;
import org.testd.ui.view.dynamic.TableMetaConfView;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DrawBoardView extends Pane {

    private final PrimaryStageHolder primaryStageHolder;
    private final FxWeaver fxWeaver;

    private Map<String, MyTableView> nameMap = new HashMap<>();

    @PostConstruct
    private void init() {
        // init draw board 'New Table' menu
        addEventHandler(MouseEvent.MOUSE_CLICKED,
                // getScene return null
                new FollowRightMouseMenu(false, this,
                        FollowRightMouseMenu.menuEntry("New Table",
                                mouseEvent -> event -> handleNewTable(mouseEvent)))
        );

        // init name set to check duplicate
        BindingUtil.mapContentWithFilter(
                nameMap,
                getChildren(),
                node -> ((MyTableView)node).getName(),
                node -> ((MyTableView)node),
                node -> node instanceof MyTableView
        );
    }

    private void handleNewTable(MouseEvent mouseEvent) {
        TableMetaProperty tableMetaProperty = new TableMetaProperty();
        primaryStageHolder.newSceneInChild(TableMetaConfView
                .getView(tableMetaProperty, name -> !nameMap.containsKey(name)));

        MyTableView table =
                fxWeaver.loadControl(MyTableView.class);
        table.initTableMetaProperty(tableMetaProperty, this);
        table.setTranslateX(mouseEvent.getX());
        table.setTranslateY(mouseEvent.getY());
        append(table);
    }

    public boolean nameExists(String name, MyTableView except) {
        if (!nameMap.containsKey(name)) {
            return false;
        }
        return nameMap.get(name) != except;
    }

    public MyTableView getTableByName(String name) {
        return nameMap.get(name);
    }

    public List<MyTableView> tablesExcept(MyTableView except) {
        return nameMap.values().stream()
                .filter(tableView -> tableView != except)
                .collect(Collectors.toList());
    }

    public void append(Node node) {
        getChildren().add(node);
    }

    public void remove(Node node) {
        getChildren().remove(node);
    }
}
