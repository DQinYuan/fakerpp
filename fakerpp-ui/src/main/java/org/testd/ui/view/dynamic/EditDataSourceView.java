package org.testd.ui.view.dynamic;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.store.storers.Storer;
import org.testd.fakerpp.core.store.storers.Storers;
import org.testd.ui.DefaultsConfig;
import org.testd.ui.controller.MetaController;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.DataSourceInfoProperty;
import org.testd.ui.util.Stages;

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
        // init batch size input
        batchSizeInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (StringUtils.isNumeric(newValue)) {
                batchSizeErrorLabel.setText("");
                okButton.setDisable(false);
            } else {
                batchSizeErrorLabel.setText("must be number");
                okButton.setDisable(true);
            }
        });

        // init name input
        nameInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (StringUtils.isEmpty(newValue)) {
                nameErrorLabel.setText("required");
                okButton.setDisable(true);
            } else if (metaController.dsNameExists(newValue, relatedProperty)) {
                nameErrorLabel.setText("duplicated");
                okButton.setDisable(true);
            } else {
                nameErrorLabel.setText("");
                okButton.setDisable(false);
            }
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

    @FXML
    private void handleComplete() {
        // check name unique
        relatedProperty.getName().set(nameInput.getText());
        relatedProperty.getType().set(typeInput.getValue());
        relatedProperty.getStorer().set(storerInput.getValue());
        relatedProperty.getBatchSize().set(Integer.parseInt(batchSizeInput.getText()));
        relatedProperty.getUrl().set(urlInput.getText());
        relatedProperty.getUser().set(userInput.getText());
        relatedProperty.getPasswd().set(passwdInput.getText());
        okAction.run();
        Stages.closeWindow(getScene().getWindow());
    }

}
