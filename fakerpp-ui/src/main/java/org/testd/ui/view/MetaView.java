package org.testd.ui.view;

import com.google.common.annotations.VisibleForTesting;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.ui.DefaultsConfig;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.controller.DrawBoardController;
import org.testd.ui.controller.MetaController;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.DataSourceInfoProperty;
import org.testd.ui.model.MetaProperty;
import org.testd.ui.model.TableProperty;
import org.testd.ui.util.BindingUtil;
import org.testd.ui.util.FxDialogs;
import org.testd.ui.view.dynamic.EditDataSourceView;
import org.testd.ui.view.dynamic.FollowRightMouseMenu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
@FxmlView
@RequiredArgsConstructor
public class MetaView {

    private final DefaultsConfig defaultsConfig;
    private final PrimaryStageHolder primaryStageHolder;
    private final FxWeaver fxWeaver;
    private final DrawBoardController drawBoardController;
    private final MetaController metaController;

    @FXML
    private ChoiceBox<String> langs;

    @FXML
    private TableView<DataSourceInfoProperty> dataSourceTable;

    @FXML
    private TableColumn<DataSourceInfoProperty, String> dsNameCol;
    @FXML
    private TableColumn<DataSourceInfoProperty, String> dsTypeCol;
    @FXML
    private TableColumn<DataSourceInfoProperty, String> dsUrlCol;

    @FXML
    private void initialize() {
        // init langs
        langs.setItems(defaultsConfig.getLocalesInfo().getLocaleItems());
        langs.getSelectionModel()
                .select(defaultsConfig.getLocalesInfo().getDefaultLocale());

        // init dataSourceTable
        dsNameCol.setCellValueFactory(cellData -> cellData.getValue().getName());
        dsTypeCol.setCellValueFactory(cellData -> cellData.getValue().getType());
        dsUrlCol.setCellValueFactory(cellData -> cellData.getValue().getUrl());

        dataSourceTable.setRowFactory(tableView -> {
            TableRow<DataSourceInfoProperty> tableRow = new TableRow<>();
            tableRow.addEventHandler(MouseEvent.MOUSE_CLICKED, new FollowRightMouseMenu(
                    true,
                    dataSourceTable,
                    new FollowRightMouseMenu.EntryNameAndAction(
                            "Edit",
                            mouseEvent -> aEvent -> {
                                EditDataSourceView editDataSourceView =
                                        fxWeaver.loadControl(EditDataSourceView.class);
                                editDataSourceView.init(tableRow.getItem(), () -> {
                                });
                                primaryStageHolder.newSceneInChild(editDataSourceView);
                            }
                    ),
                    new FollowRightMouseMenu.EntryNameAndAction(
                            "Delete",
                            mouseEvent -> aEvent -> handleDataSourceDelete(tableRow.getItem())
                    )
            ));
            return tableRow;
        });


        // bind propMap
        Map<String, DataSourceInfoProperty> propMap = new HashMap<>();
        BindingUtil.mapContent(propMap, dataSourceTable.getItems(),
                DataSourceInfoProperty::getName,
                Function.identity());
        metaController.init(propMap, dataSourceTable.getItems());

    }

    @VisibleForTesting
    protected void handleDataSourceDelete(DataSourceInfoProperty dataSourceInfoProperty) {
        Optional<TableProperty> useTableView = drawBoardController.dsInUse(dataSourceInfoProperty);
        if (useTableView.isPresent()) {
            FxDialogs.showError("Data source delete error",
                    "This data source still in use",
                    String.format("data source %s is still used in table %s",
                            dataSourceInfoProperty.getName().get(),
                            useTableView.get().getName().get()));
            return;
        }
        dataSourceTable.getItems().remove(dataSourceInfoProperty);
    }

    @VisibleForTesting
    protected void appendDataSource(DataSourceInfoProperty dataSourceInfoProperty) {
        dataSourceTable.getItems().add(dataSourceInfoProperty);
    }

    @FXML
    private void handleNewDataSource() {
        DataSourceInfoProperty dataSourceInfoProperty = new DataSourceInfoProperty(
                "",
                defaultsConfig.getStoreType(),
                "default",
                defaultsConfig.getBatchSize(),
                "", "", ""
        );
        EditDataSourceView editDataSourceView = fxWeaver.loadControl(EditDataSourceView.class);
        editDataSourceView.init(dataSourceInfoProperty, () ->
                appendDataSource(dataSourceInfoProperty));
        primaryStageHolder.newSceneInChild(editDataSourceView);
    }

    public void initFromMetaProperty(MetaProperty metaProperty) {
        Bindings.bindBidirectional(metaProperty.getLang(),
                langs.valueProperty());
        Bindings.bindContentBidirectional(metaProperty.getDataSourceInfos(),
                dataSourceTable.getItems());
    }

}
