package org.testd.ui.view.form;

import com.dlsc.formsfx.model.structure.*;
import com.dlsc.formsfx.model.validators.CustomValidator;
import com.dlsc.formsfx.model.validators.IntegerRangeValidator;
import com.dlsc.formsfx.view.controls.SimpleComboBoxControl;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.ui.controller.MetaController;
import org.testd.ui.model.DataSourceInfoProperty;
import org.testd.ui.util.Forms;
import org.testd.ui.vo.TableMetaVO;

import java.util.Arrays;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class TableMetaConfView {

    private final MetaController metaController;

    public Parent getView(TableMetaVO metaProperty,
                                 Predicate<String> nameValidator,
                          Runnable okAction) {

        BooleanField virtualField =
                Field.ofBooleanType(metaProperty.dataSourceProperty().get() == null)
                        .id("virtual")
                        .label("Virtual:");

        ObjectProperty<DataSourceInfoProperty> dsProperty = metaProperty.dataSourceProperty();

        // SingleSelectionType can not lazy persist, so we use a temp property
        ObjectProperty<DataSourceInfoProperty> temp =
                new SimpleObjectProperty<>(metaProperty.dataSourceProperty().get());
        SingleSelectionField<DataSourceInfoProperty> dataSourceField =
                Field.ofSingleSelectionType(new SimpleListProperty<>(metaController.getDsInfos()), temp)
                        .id("dataSource")
                        .label("DataSource:")
                        .editable(virtualField.valueProperty().not().get())
                        .render(new SimpleComboBoxControl<DataSourceInfoProperty>() {
                            @Override
                            public void initializeParts() {
                                super.initializeParts();
                                Callback<ListView<DataSourceInfoProperty>, ListCell<DataSourceInfoProperty>>
                                        cellFactory = listView ->
                                        new ListCell<DataSourceInfoProperty>() {
                                            @Override
                                            protected void updateItem(DataSourceInfoProperty item,
                                                                      boolean empty) {
                                                super.updateItem(item, empty);

                                                if (empty || item == null
                                                        || item.getName().isEmpty().get()) {
                                                    setText(null);
                                                    setGraphic(null);
                                                } else {
                                                    setText(item.getName().get());
                                                }
                                            }
                                        };
                                comboBox.setButtonCell(cellFactory.call(null));
                                comboBox.setCellFactory(cellFactory);
                            }

                            @Override
                            public void setupBindings() {
                                super.setupBindings();
                                readOnlyLabel.textProperty().unbind();
                                readOnlyLabel.setText("Virtual table has none data source.");
                            }
                        });

        virtualField.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                dataSourceField.deselect();
                dataSourceField.editable(false);
            } else {
                dataSourceField.editable(true);
                dataSourceField.required("table must have a data source");
            }
        });

        return Forms.renderForm("Meta Conf", Arrays.asList(
                Field.ofStringType(metaProperty.nameProperty())
                        .id("tableName")
                        .required("Table Name must not be empty")
                        .label("Table Name:")
                        .validate(CustomValidator.forPredicate(nameValidator,
                                "table name should be unique")),
                virtualField,
                dataSourceField,
                Field.ofIntegerType(metaProperty.numberProperty())
                        .id("number")
                        .validate(IntegerRangeValidator.atLeast(0, ">= 0"))
                        .label("Number:")
        ), ()->{
            dsProperty.set(temp.get());
            okAction.run();
        });
    }

}
