package org.testd.ui.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.store.storers.Storer;
import org.testd.fakerpp.core.store.storers.Storers;
import org.testd.ui.DefaultsConfig;

import java.util.Map;
import java.util.function.Supplier;

@Component
@FxmlView
@RequiredArgsConstructor
public class NewDataSourceView {

    //------------ di
    private final DefaultsConfig defaultsConfig;
    private final Storers storers;

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
    private void initialize() {
        // init batch size input
        batchSizeInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (StringUtils.isNumeric(newValue)) {
                batchSizeErrorLabel.setText("");
            } else {
                batchSizeErrorLabel.setText("must be number");
            }
        });
        batchSizeInput.setText(String.valueOf(defaultsConfig.getBatchSize()));

        // init name input
        nameInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (StringUtils.isEmpty(newValue)) {
                nameErrorLabel.setText("required");
            } else {
                nameErrorLabel.setText("");
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

}
