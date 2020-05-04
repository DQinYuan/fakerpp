package org.testd.ui.view.form;

import com.dlsc.formsfx.model.structure.*;
import com.dlsc.formsfx.model.validators.CustomValidator;
import com.dlsc.formsfx.model.validators.StringLengthValidator;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.engine.generator.GeneratorSupplier;
import org.testd.fakerpp.core.parser.ast.Table;
import org.testd.ui.DefaultsConfig;
import org.testd.ui.model.TableProperty;
import org.testd.ui.view.component.EditableTableBuilder;
import org.testd.ui.util.Forms;
import org.testd.ui.util.RunnableUtil;

import java.util.*;

@Component
@RequiredArgsConstructor
public class EditColFamilyParamView {

    private final DefaultsConfig defaultsConfig;

    private static String FOLLOW_GLOBAL_LANG = "(follow global lang)";

    public Region getView(TableProperty.GeneratorInfoProperty generatorInfoProperty,
                          GeneratorSupplier generatorSupplier,
                          Stage showStage) {
        List<Element> formElements = new ArrayList<>();

        // init langs
        ChoiceBox<String> langs = new ChoiceBox<>();
        langs.getItems().add(FOLLOW_GLOBAL_LANG);
        langs.getItems().addAll(defaultsConfig.getLocalesInfo().getLocaleItems());
        langs.getSelectionModel().select(generatorInfoProperty.getLang()
                .isEqualTo(Table.GeneratorInfo.FOLLOW_DEFAULT_LANG).get() ||
                generatorInfoProperty.getLang().isEmpty().get() ?
                FOLLOW_GLOBAL_LANG : generatorInfoProperty.getLang().get());
        langs.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (Objects.equals(FOLLOW_GLOBAL_LANG, newValue)) {
                generatorInfoProperty.getLang().set(Table.GeneratorInfo.FOLLOW_DEFAULT_LANG);
            } else {
                generatorInfoProperty.getLang().set(newValue);
            }
        });
        formElements.add(NodeElement.of(langs));

        // init params
        Collection<GeneratorSupplier.ParamInfo> generatorParamInfos = generatorSupplier
                .paramInfos().values();
        Map<String, StringProperty> attrsProperty = generatorInfoProperty.getAttributes();

        Runnable extraOkAction = () -> {
        };
        for (GeneratorSupplier.ParamInfo paramInfo : generatorParamInfos) {
            Element formField;
            StringProperty paramStr = attrsProperty.computeIfAbsent(paramInfo.getName(),
                    name -> new SimpleStringProperty(defaultValue(paramInfo)));

            if (paramInfo.getDefaultValue() instanceof String[]) {
                String[] supportedValues = (String[])
                        paramInfo.getDefaultValue();
                ObjectProperty<String> temp =
                        new SimpleObjectProperty<>(paramStr.get());
                formField =
                        Field.ofSingleSelectionType(new SimpleListProperty<>(
                                FXCollections.observableArrayList(supportedValues)), temp)
                                .label(paramInfo.getName())
                                .id(paramInfo.getName());
                extraOkAction = RunnableUtil.andThen(extraOkAction, () -> paramStr.set(temp.get()));
            } else {
                Class<?> type = paramInfo.getRelType();
                StringField stringField = Field.ofStringType(paramStr)
                        .label(paramInfo.getName())
                        .id(paramInfo.getName());

                if (Number.class.isAssignableFrom(ClassUtils.primitiveToWrapper(type))) {
                    // number input
                    stringField.validate(CustomValidator.forPredicate(
                            StringUtils::isNumeric
                            , "must be number"));
                } else if (paramInfo.getDefaultValue() == null) {
                    stringField.validate(
                            StringLengthValidator.atLeast(1, "required")
                    );
                }
                stringField.multiline(paramInfo.isMultiLine());
                formField = stringField;
            }

            formElements.add(formField);
            attrsProperty.put(paramInfo.getName(), paramStr);
        }

        // init options
        if (generatorSupplier.optionSetter().isPresent()) {
            EditableTableBuilder etb = new EditableTableBuilder(generatorInfoProperty.getOptions(),
                    () -> showStage.setScene(new Scene(Forms.renderForm("Edit Param", formElements)))
            );
            formElements.add(NodeElement.of(etb));
        }

        return Forms.renderForm("Edit Param", formElements, extraOkAction);
    }

    private String defaultValue(GeneratorSupplier.ParamInfo paramInfo) {
        Object defaultValue = paramInfo.getDefaultValue();
        if (defaultValue == null) {
            return "";
        }
        if (defaultValue instanceof String[]) {
            return ((String[]) defaultValue)[0];
        }

        return defaultValue.toString();
    }

}
