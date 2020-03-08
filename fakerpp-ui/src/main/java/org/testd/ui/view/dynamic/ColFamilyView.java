package org.testd.ui.view.dynamic;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.engine.generator.GeneratorSupplier;
import org.testd.fakerpp.core.engine.generator.Generators;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.ColFamilyProperty;
import org.testd.ui.util.Stages;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ColFamilyView extends BorderPane implements ColFamilyViewInterface {

    private final Generators generators;
    private final FxWeaver fxWeaver;

    private ColFamilyProperty colFamilyProperty;
    private MyTableView ownerTable;

    @FXML
    private ChoiceBox<String> fieldInput;

    @FXML
    private ChoiceBox<String> generatorInput;

    @FXML
    private ColFamilyInputMenu colsInput;

    @FXML
    private void initialize() {
        // init generatorInput fieldInput cascade
        Map<String, Map<String, GeneratorSupplier>> gens = generators.generators();
        fieldInput.setItems(FXCollections.observableArrayList(gens.keySet()));
        fieldInput.valueProperty().addListener((observable, oldValue, newValue) -> {
            generatorInput.setItems(FXCollections
                    .observableArrayList(gens.get(newValue).keySet()));
        });

        // init edit cols menu
        colsInput.setFollowRightMenu(
                FollowRightMouseMenu.menuEntry("edit cols",
                        ignore -> event -> {
                            EditColFamilyView editColFamilyView = fxWeaver.loadControl(EditColFamilyView.class);
                            editColFamilyView.initFromMyTableView(ownerTable, colFamilyProperty);
                            Stages.newSceneInChild(editColFamilyView, getScene().getWindow());
                        }),
                FollowRightMouseMenu.menuEntry("delete cols",
                        ignore -> event -> ownerTable.deleteTableColFamily(this))
        );
    }

    public void initFromTableAndColFamilyProperty(MyTableView ownerTable,
            ColFamilyProperty colFamilyProperty) {
        this.ownerTable = ownerTable;
        this.colFamilyProperty = colFamilyProperty;
        colsInputSync();
        colFamilyProperty.colsProperty()
                .addListener((ListChangeListener<String>) c -> colsInputSync());

        colFamilyProperty.fieldProperty()
                .bindBidirectional(fieldInput.valueProperty());
        colFamilyProperty.generatorProperty()
                .bindBidirectional(generatorInput.valueProperty());
    }

    private void colsInputSync() {
        colsInput.getChildren().clear();
        colsInput.getChildren().addAll(
                colFamilyProperty.colsProperty().stream()
                        .map(Label::new).collect(Collectors.toList())
        );
    }

    public ColFamilyProperty getColFamilyProperty() {
        return colFamilyProperty;
    }
}
