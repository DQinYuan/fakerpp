package org.testd.ui.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.testd.ui.view.dynamic.FollowRightMouseMenu;

import java.util.List;

public class EditableTableBuilder extends BorderPane {

    private final Runnable changeScene;

    private void change(Node newNode) {
        setCenter(newNode);
        changeScene.run();
    }

    public EditableTableBuilder(List<List<StringProperty>> values,
                                Runnable changeScene) {
        assert values != null;
        this.changeScene = changeScene;

        Label optionsLabel = new Label("options:   ");
        setLeft(optionsLabel);
        optionsLabel.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new FollowRightMouseMenu(true, optionsLabel,
                        new FollowRightMouseMenu.EntryNameAndAction(
                                "Reset",
                                ignore -> event -> {
                                    values.clear();
                                    change(builderHBox(values));
                                }
                        )));

        if (values.size() == 0) {
            change(builderHBox(values));
        } else {
            EditableTable editableTable = new EditableTable(values);
            change(editableTable);
        }
    }

    private Node builderHBox(List<List<StringProperty>> values) {
        HBox builder = new HBox();
        builder.setSpacing(10);

        Spinner<Integer> rowNumInput = new Spinner<>(1, Integer.MAX_VALUE, 1);
        Spinner<Integer> colNumInput = new Spinner<>(1, Integer.MAX_VALUE, 1);
        Button build = new Button("Confirm");
        build.setOnAction(event -> {
            int rowNum = rowNumInput.getValue();
            int colNum = colNumInput.getValue();
            for (int i = 0; i < rowNum; i++) {
                values.add(ListUtil.repeat(() -> new SimpleStringProperty(""),
                        colNum));
            }

            EditableTable editableTable = new EditableTable(values);
            change(editableTable);
        });

        builder.getChildren().add(rowNumInput);
        builder.getChildren().add(new Label("x"));
        builder.getChildren().add(colNumInput);
        builder.getChildren().add(build);

        return builder;
    }


}
