package org.testd.ui.view.dynamic;

import com.google.common.annotations.VisibleForTesting;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.engine.generator.GeneratorSupplier;
import org.testd.fakerpp.core.engine.generator.Generators;
import org.testd.ui.model.TableProperty;
import org.testd.ui.util.Stages;
import org.testd.ui.view.form.EditColFamilyParamView;
import org.testd.ui.vo.ColFamilyVO;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GeneratorSelector extends HBox {

    private final Generators generators;

    private final EditColFamilyParamView editColFamilyParamView;

    private ColFamilyVO owner;
    private TableProperty.GeneratorInfoProperty generatorInfoProperty;

    private ChoiceBox<String> fieldInput;

    private ChoiceBox<String> generatorInput;

    private Spinner<Integer> weightInput;

    private MenuItem editParam;
    private MenuItem composeMore;
    private MenuItem deleteGen;

    @VisibleForTesting
    @PostConstruct
    protected void postConstruct() {
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10.0);

        setPrefWidth(200);

        fieldInput = new ChoiceBox<>();
        fieldInput.setPrefHeight(26);
        fieldInput.setPrefWidth(80);
        fieldInput.setId("fieldInput");
        getChildren().add(fieldInput);

        generatorInput = new ChoiceBox<>();
        generatorInput.setPrefHeight(26);
        generatorInput.setPrefWidth(80);
        generatorInput.setId("generatorInput");
        getChildren().add(generatorInput);

        // init cascade between fieldInput and generatorInput
        Map<String, Map<String, GeneratorSupplier>> gens = generators.generators();
        fieldInput.setItems(FXCollections.observableArrayList(gens.keySet()));
        fieldInput.valueProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<String> generators = FXCollections
                    .observableArrayList(gens.get(newValue).keySet());
            generatorInput.setItems(generators);
            if (generators.size() > 0) {
                generatorInput.getSelectionModel().select(0);
            }
        });
        fieldInput.getSelectionModel().select("built-in");

        // init weight input
        weightInput = new Spinner<>(0, Integer.MAX_VALUE, 1);
        weightInput.setPrefHeight(26);
        weightInput.setPrefWidth(65);
    }

    private GeneratorSupplier currentGenerator() {
        GeneratorSupplier current = generators.generators()
                .get(fieldInput.getValue()).get(generatorInput.getValue());
        assert current != null;
        return current;
    }

    public void init(ColFamilyVO owner,
                     TableProperty.GeneratorInfoProperty generatorInfoProperty) {
        this.owner = owner;
        this.generatorInfoProperty = generatorInfoProperty;

        fieldInput.valueProperty()
                .bindBidirectional(generatorInfoProperty.getField());
        generatorInput.valueProperty()
                .bindBidirectional(generatorInfoProperty.getGenerator());

        generatorInput.valueProperty().addListener((observable, oldValue, newValue) -> {
            generatorInfoProperty.clearParam();
            if (newValue != null) {
                // pop up config dialog when new generator has required param
                GeneratorSupplier curGenerator = currentGenerator();
                boolean hasRequiredParam = curGenerator.paramInfos().values().stream().anyMatch(pi ->
                        pi.getDefaultValue() == null) || curGenerator.optionSetter().isPresent();
                if (hasRequiredParam) {
                    showParamEditDialog();
                }
            }
        });

        weightInput.getValueFactory().setValue(generatorInfoProperty.getWeight().get());
        generatorInfoProperty.getWeight()
                .bind(weightInput.valueProperty());

        // init context menu
        ContextMenu contextMenu = new ContextMenu();

        editParam = new MenuItem("Edit Params");
        contextMenu.getItems().add(editParam);
        editParam.setId("Edit_Params_mi");
        editParam.setOnAction(ignore -> showParamEditDialog());

        composeMore = new MenuItem("Compose More Generator");
        contextMenu.getItems().add(composeMore);
        composeMore.setId("Compose_More_Generator_mi");
        composeMore.setOnAction(event -> owner.getGeneratorInfos()
                .add(TableProperty.GeneratorInfoProperty.defaultProperty()));

        deleteGen = new MenuItem("Delete");
        deleteGen.setOnAction(event ->
                owner.getGeneratorInfos().remove(generatorInfoProperty));

        generatorInput.setContextMenu(contextMenu);

        // some interface only show when there is more than two generators
        BooleanBinding moreThanOneGen = Bindings
                .greaterThan(Bindings.size(owner.getGeneratorInfos()), 1);
        moreThanOneGen
                .addListener((observable, oldValue, newValue) ->
                        moreThanOneGenAction(newValue));
        moreThanOneGenAction(moreThanOneGen.get());
    }

    private void showParamEditDialog() {
        Stage child = Stages.child(getScene().getWindow());
        Region editView = editColFamilyParamView
                .getView(generatorInfoProperty, currentGenerator(), child);
        child.setScene(new Scene(editView));
        child.showAndWait();
    }

    private void moreThanOneGenAction(boolean isMoreThanOne) {
        if (isMoreThanOne) {
            getChildren().add(weightInput);
            generatorInput.getContextMenu().getItems().add(deleteGen);
        } else {
            getChildren().remove(weightInput);
            generatorInput.getContextMenu().getItems().remove(deleteGen);
        }
    }


}
