package org.testd.ui.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.fakerpp.core.parser.ast.Meta;
import org.testd.fakerpp.core.parser.ast.Table;
import org.testd.fakerpp.core.store.storers.Storer;
import org.testd.fakerpp.core.store.storers.Storers;
import org.testd.fakerpp.core.util.MyMapUtil;
import org.testd.ui.DefaultsConfig;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.UiPreferences;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.DataSourceInfoProperty;
import org.testd.ui.model.ERMLProperty;
import org.testd.ui.model.MetaProperty;
import org.testd.ui.model.TableProperty;
import org.testd.ui.util.BindingUtil;
import org.testd.ui.util.FxDialogs;
import org.testd.ui.view.dynamic.EditDataSourceView;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@FxmlView
@RequiredArgsConstructor
public class SelectReverseDataSourceView {

    private final UiPreferences uiPreferences;
    private final DefaultsConfig defaultsConfig;
    private final FxWeaver fxWeaver;
    private final PrimaryStageHolder primaryStageHolder;
    private final Storers storers;

    @FXML
    private TableView<DataSourceInfoProperty> dataSourceTable;
    @FXML
    private TableColumn<DataSourceInfoProperty, String> dsNameCol;
    @FXML
    private TableColumn<DataSourceInfoProperty, String> dsTypeCol;
    @FXML
    private TableColumn<DataSourceInfoProperty, String> dsUrlCol;

    private ObservableMap<String, DataSourceInfoProperty> defaultDataSources;

    @FXML
    private void initialize() {
        dsNameCol.setCellValueFactory(cellData -> cellData.getValue().getName());
        dsTypeCol.setCellValueFactory(cellData -> cellData.getValue().getType());
        dsUrlCol.setCellValueFactory(cellData -> cellData.getValue().getUrl());

        defaultDataSources =
                FXCollections.observableMap(
                        MyMapUtil.mutableValueMap(uiPreferences.getDataSources(),
                                DataSourceInfoProperty::new)
                );
        BindingUtil.bindValue(
                dataSourceTable.getItems(),
                defaultDataSources
        );
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
                defaultDataSources.put(dataSourceInfoProperty.getName().get(),
                        dataSourceInfoProperty));
        primaryStageHolder
                .newSceneInChild(editDataSourceView);
    }

    @FXML
    private void handleOk() {
        DataSourceInfoProperty selectedItem =
                dataSourceTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            FxDialogs.showError("Reverse from datasource fail",
                    "Not Select Datasource",
                    "Please select a datasource firest");
            return;
        }
        Supplier<Storer> storerSupplier = storers.storers().get(selectedItem.getType().get())
                .get(selectedItem.getStorer().get());
        if (storerSupplier == null) {
            FxDialogs.showError("Reverse from datasource fail",
                    "Can not find storer in datasource",
                    String.format("datasource name: '%s', type: '%s', storer: '%s'",
                            selectedItem.getName().get(),
                            selectedItem.getType().get(),
                            selectedItem.getStorer().get()));
            return;
        }

        try {
            Storer storer = storerSupplier.get();
            storer.init(selectedItem.unmap());
            Set<Table> reverseTables = storer.reverse();
            ERML erml = new ERML(
                    new Meta(
                            defaultsConfig.getLocalesInfo().getDefaultLocale(),
                            MyMapUtil.valueMap(defaultDataSources, DataSourceInfoProperty::unmap)
                    ),
                    reverseTables.stream().collect(
                            Collectors.toMap(
                                    Table::getName,
                                    Function.identity()
                            )
                    )

            );
            primaryStageHolder.changeSceneFullScreenWithParam(MainWindowView.class,
                    v -> v.initFromErml(ERMLProperty.map(erml), null));
        } catch (ERMLException e) {
            FxDialogs.showException(
                    "Reverse from datasource fail",
                    "Exception when reverse from datasource",
                    e.getMessage(),
                    e
            );
        }
    }


}
