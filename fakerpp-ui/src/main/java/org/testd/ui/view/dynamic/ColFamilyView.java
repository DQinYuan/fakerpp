package org.testd.ui.view.dynamic;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.engine.generator.GeneratorSupplier;
import org.testd.fakerpp.core.engine.generator.Generators;
import org.testd.ui.fxweaver.core.FxmlView;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ColFamilyView extends BorderPane {

    private final Generators generators;

    @FXML
    private ChoiceBox<String> fieldInput;

    @FXML
    private ChoiceBox<String> generatorInput;

    @FXML
    private void initialize() {
        Map<String, Map<String, GeneratorSupplier>> gens = generators.generators();
        fieldInput.setItems(FXCollections.observableArrayList(gens.keySet()));
        fieldInput.valueProperty().addListener((observable, oldValue, newValue) -> {
            generatorInput.setItems(FXCollections
                        .observableArrayList(gens.get(newValue).keySet()));
        });
        fieldInput.getSelectionModel().select("built-in");

        // init edit cols menu
/*        cols.addEventHandler(MouseEvent.MOUSE_CLICKED, new FollowRightMouseMenu(this,
                ignore -> {
                    MenuItem addColItem = new MenuItem("edit cols");
                    addColItem.setOnAction(itemEvent -> {
                        if (ThreadLocalRandom.current().nextInt() % 2 == 0) {
                            cols.getChildren().add(new Label("colName"));
                        } else {
                            cols.getChildren().add(new Label("colNamecolNamecolNamecolNamecolName"));
                        }
                    });
                    return addColItem;
                }));*/
    }

}
