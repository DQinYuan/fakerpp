package org.testd.ui.view.form;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.ui.DefaultsConfig;
import org.testd.ui.model.TableProperty;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EditColFamilyParamView {

    private final DefaultsConfig defaultsConfig;

    private static String FOLLOW_GLOBAL_LANG = "(follow global lang)";

    public Parent getView(TableProperty.GeneratorInfoProperty generatorInfoProperty) {
        VBox parent = new VBox();
        parent.setSpacing(10);

        // init langs
        ChoiceBox<String> langs = new ChoiceBox<>();
        langs.getItems().add(FOLLOW_GLOBAL_LANG);
        langs.getItems().addAll(defaultsConfig.getLocalesInfo().getLocaleItems());
        langs.getSelectionModel().select(FOLLOW_GLOBAL_LANG);
        langs.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.equals(FOLLOW_GLOBAL_LANG, newValue)) {
                generatorInfoProperty.getLang().set("");
            } else {
                generatorInfoProperty.getLang().set(newValue);
            }
        });
        parent.getChildren().add(langs);

        // init others


        return parent;
    }

}
