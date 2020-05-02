package org.testd.ui.util;

import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

import java.util.List;

public class EditableTable extends TableView<List<StringProperty>> {

    /**
     * values[0] is row 0, values[1] is row 2....
     * @param values
     */
    public EditableTable(List<List<StringProperty>> values) {
        setEditable(true);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setFixedCellSize(30);
        skinProperty().addListener((observable, oldSkin, newSkin) -> {
            // set no head
            TableHeaderRow headerRow = ((TableViewSkinBase) newSkin).getTableHeaderRow();
            headerRow.setMinHeight(0);
            headerRow.setPrefHeight(0);
            headerRow.setMaxHeight(0);
            headerRow.setVisible(false);
        });
        int columnNum = values.get(0).size();

        for (int i = 0; i < columnNum; i++) {
            final int index = i;
            TableColumn<List<StringProperty>, String> column = new TableColumn<>();
            column.setCellValueFactory(param -> param.getValue().get(index));
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            getColumns().add(column);
        }

        values.forEach(getItems()::add);

        double height = getItems().size() * getFixedCellSize() + 3;
        setPrefHeight(height);
        setMinHeight(height);
    }

}
