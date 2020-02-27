package org.testd.ui.view.dynamic;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.concurrent.ThreadLocalRandom;

// alignment="CENTER_LEFT" prefHeight="50.0" prefWidth="111.0" BorderPane.alignment="CENTER_LEFT"
public class ColFamilyInputMenu extends VBox {

    public ColFamilyInputMenu() {
        addEventHandler(MouseEvent.MOUSE_CLICKED, new FollowRightMouseMenu(true,this,
                ignore -> {
                    MenuItem addColItem = new MenuItem("edit cols");
                    addColItem.setOnAction(itemEvent -> {
                        if (ThreadLocalRandom.current().nextInt() % 2 == 0) {
                            getChildren().add(new Label("colName"));
                        } else {
                            getChildren().add(new Label("colNamecolNamecolNamecolNamecolName"));
                        }
                    });
                    return addColItem;
                }));
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(50.0);
        setPrefWidth(111.0);
    }
}
