package org.testd.ui.view.dynamic;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.store.storers.Storer;
import org.testd.fakerpp.core.store.storers.Storers;
import org.testd.ui.DefaultsConfig;
import org.testd.ui.controller.MetaController;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.DataSourceInfoProperty;
import org.testd.ui.util.BindingUtil;
import org.testd.ui.util.ButtonAbler;
import org.testd.ui.util.FxDialogs;
import org.testd.ui.util.Stages;

import java.sql.SQLException;
import java.util.Map;
import java.util.function.Supplier;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EditDataSourceView extends AnchorPane {

    //------------ di
    private final Storers storers;
    private final MetaController metaController;
    private final DefaultsConfig defaultsConfig;

    //------------ JavaFx Component

    @FXML
    private Label batchSizeErrorLabel;
    @FXML
    private TextField batchSizeInput;

    @FXML
    private Label nameErrorLabel;
    @FXML
    private TextField nameInput;

    @FXML
    private ChoiceBox<String> typeInput;

    @FXML
    private ChoiceBox<String> storerInput;

    @FXML
    private TextField urlInput;

    @FXML
    private TextField userInput;

    @FXML
    private PasswordField passwdInput;

    @FXML
    private Button okButton;

    private DataSourceInfoProperty relatedProperty;
    private Runnable okAction;

    @FXML
    private void initialize() {

        ButtonAbler buttonAbler = new ButtonAbler(okButton);

        // init batch size input
        BooleanBinding batchSizePredicate = BindingUtil.isNumberic(batchSizeInput.textProperty());
        buttonAbler.addPredicate(batchSizePredicate);
        batchSizePredicate.addListener((observable, oldValue, newValue) ->
                batchSizeErrorLabel.setText(newValue ? "" : "must be number"));

        // init name input
        BooleanBinding namePredicate = BindingUtil.predicate(nameInput.textProperty(),
                newName ->
                        !StringUtils.isEmpty(newName) &&
                                !metaController.dsNameExists(newName, relatedProperty));
        buttonAbler.addPredicate(namePredicate);
        namePredicate.addListener((observable, oldValue, newValue) -> {
            if (StringUtils.isEmpty(nameInput.getText())) {
                nameErrorLabel.setText("required");
                return;
            }

            nameErrorLabel.setText(newValue ? "" : "duplicated");
        });

        // init type and storer input
        Map<String, Map<String, Supplier<Storer>>> storers = this.storers.storers();
        typeInput.setItems(FXCollections.observableArrayList(storers.keySet()));
        typeInput.getSelectionModel().select(defaultsConfig.getStoreType());

        storerInput.setItems(FXCollections.observableArrayList(
                storers.get(defaultsConfig.getStoreType()).keySet()));
        storerDefaultSelect();

        typeInput.valueProperty().addListener((observable, oldValue, newValue) -> {
            storerInput.setItems(FXCollections.observableArrayList(storers.get(newValue).keySet()));
            storerDefaultSelect();
        });

        // build button abler
        buttonAbler.build();
    }

    public void init(DataSourceInfoProperty dataSourceInfoProperty, Runnable okAction) {
        this.relatedProperty = dataSourceInfoProperty;
        this.okAction = okAction;

        nameInput.setText(dataSourceInfoProperty.getName().get());

        typeInput.getSelectionModel().select(dataSourceInfoProperty.getType().get());
        storerInput.getSelectionModel().select(dataSourceInfoProperty.getStorer().get());

        batchSizeInput.setText(String.valueOf(dataSourceInfoProperty.getBatchSize().get()));

        urlInput.setText(dataSourceInfoProperty.getUrl().get());
        userInput.setText(dataSourceInfoProperty.getUser().get());
        passwdInput.setText(dataSourceInfoProperty.getPasswd().get());
    }

    private String DEFAULT_STORER = "default";

    private void storerDefaultSelect() {
        if (storerInput.getItems().contains(DEFAULT_STORER)) {
            storerInput.getSelectionModel().select(DEFAULT_STORER);
            return;
        }
        if (storerInput.getItems().size() > 0) {
            storerInput.getSelectionModel().select(storerInput.getItems().get(0));
            return;
        }
    }

    private DataSourceInfo currentDataSourceInfo() {
        return new DataSourceInfo(
                nameInput.getText(),
                typeInput.getValue(),
                storerInput.getValue(),
                Integer.parseInt(batchSizeInput.getText()),
                urlInput.getText(),
                userInput.getText(),
                passwdInput.getText()
        );
    }

    @FXML
    private void handleComplete() {
        // check name unique
        relatedProperty.set(currentDataSourceInfo());
        okAction.run();
        Stages.closeWindow(getScene().getWindow());
    }

    @FXML
    private void handleTestConnection() {
        String selectedType = typeInput.getValue();
        String selectedStorer = storerInput.getValue();
        if (StringUtils.isEmpty(selectedType) || StringUtils.isEmpty(selectedStorer)) {
            FxDialogs.showError("Test Connection error",
                    "Type or Storer is empty",
                    "Type or Storer can not be empty");
            return;
        }

        Supplier<Storer> storerSupplier = storers.storers().get(selectedType)
                .get(selectedStorer);
        if (selectedStorer == null) {
            FxDialogs.showError("Test Connection error",
                    "Storer not exist",
                    String.format("type: %s, storer: %s, not exist",
                            selectedType, selectedStorer));
            return;
        }

        Storer storer = storerSupplier.get();
        DataSourceInfo dataSourceInfo = currentDataSourceInfo();
        try {
            storer.init(dataSourceInfo);
        } catch (Exception e) {
            FxDialogs.showWarning("Test connection warning",
                    "Connect error",
                    String.format("can not connect to data source %s",
                            dataSourceInfo.getUrl()));
            return;
        }

        FxDialogs.showInformation("Test connection successfully",
                "Connection OK",
                "");
    }

}
