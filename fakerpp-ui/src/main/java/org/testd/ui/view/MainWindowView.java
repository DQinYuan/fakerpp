package org.testd.ui.view;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.fakerpp.core.parser.ast.Meta;
import org.testd.ui.DefaultsConfig;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;

import java.util.List;


@Component
@FxmlView
@RequiredArgsConstructor
public class MainWindowView {

    //------------ di
    private final DefaultsConfig defaultsConfig;
    private final PrimaryStageHolder primaryStageHolder;
    private final FxWeaver fxWeaver;

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
    private FlowPane drawBoard;

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
        dsNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        dsTypeCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        dsUrlCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUrl()));
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
