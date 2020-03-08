package org.testd.ui.view;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.fakerpp.core.parser.ast.Meta;
import org.testd.ui.DefaultsConfig;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.TableMetaProperty;
import org.testd.ui.service.TableInfoService;
import org.testd.ui.util.FxDialogs;
import org.testd.ui.view.dynamic.FollowRightMouseMenu;
import org.testd.ui.view.dynamic.MyTableView;
import org.testd.ui.view.dynamic.TableMetaConfView;

import java.util.List;
import java.util.function.Predicate;


@Component
@FxmlView
@RequiredArgsConstructor
public class MainWindowView {

    //------------ di
    private final DefaultsConfig defaultsConfig;
    private final PrimaryStageHolder primaryStageHolder;
    private final FxWeaver fxWeaver;
    private final TableInfoService tableInfoService;

    //------------ JavaFx Component

    @FXML
    private ChoiceBox<String> langs;

    @FXML
    private TableView<DataSourceInfo> dataSourceTable;

    @FXML
    private TableColumn<DataSourceInfo, String> dsNameCol;
    @FXML
    private TableColumn<DataSourceInfo, String> dsTypeCol;
    @FXML
    private TableColumn<DataSourceInfo, String> dsUrlCol;
    @FXML
    private Pane drawBoard;

    @FXML
    private void initialize() {
        // init langs
        ObservableList items = FXCollections.observableArrayList();
        DefaultsConfig.SupportedLocales localesConfig = this.defaultsConfig.getLocalesInfo();
        List<String> locales = localesConfig.getSupportedLocales();

        for (int i = 0; i < locales.size(); i++) {
            if (i == localesConfig.getSeparateBelow()) {
                items.add(new Separator());
            }
            items.add(locales.get(i));
        }

        langs.setItems(items);
        langs.getSelectionModel()
                .select(localesConfig.getDefaultLocale());

        // init dataSourceTable
        dsNameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        dsTypeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getType()));
        dsUrlCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUrl()));

        // init draw board 'New Table' menu
        drawBoard.addEventHandler(MouseEvent.MOUSE_CLICKED,
                // getScene return null
                new FollowRightMouseMenu(false, drawBoard,
                        FollowRightMouseMenu.menuEntry("New Table",
                                mouseEvent -> event -> handleNewTable(mouseEvent)))
        );
    }

    private void handleNewTable(MouseEvent mouseEvent) {
        TableMetaProperty tableMetaProperty = new TableMetaProperty();
        primaryStageHolder.newSceneInChild(TableMetaConfView
                .getView(tableMetaProperty, name -> !tableInfoService.nameExists(name)));

        MyTableView table =
                fxWeaver.loadControl(MyTableView.class);
        table.initTableMetaProperty(tableMetaProperty);
        table.setTranslateX(mouseEvent.getX());
        table.setTranslateY(mouseEvent.getY());
        drawBoard.getChildren().add(table);
        tableInfoService.addTable(table);
    }

    public void deleteTableFromDrawBoard(MyTableView tableView) {
        drawBoard.getChildren().remove(tableView);
        tableInfoService.deleteTable(tableView);
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleNewDataSource() {
        primaryStageHolder.newSceneInChild(NewDataSourceView.class);
    }

    public void initFromErml(ERML erml) {
        Meta meta = erml.getMeta();
        if (meta != null) {
            langs.getSelectionModel().select(meta.getLang());
            dataSourceTable.getItems().addAll(meta.getDataSourceInfos().values());
        }
    }


}
