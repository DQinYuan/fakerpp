package org.testd.ui.view.dynamic;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class TaskStateView extends HBox {

    private final ImageView img;
    private final String description;

    public TaskStateView(ImageView img, String description) {
        img.setFitWidth(20);
        img.setFitHeight(20);
        this.img = img;
        this.description = description;
        setSpacing(10.0);
        setLayoutX(0.0);
        setLayoutY(0.0);
        getChildren().addAll(img, new Label(description));
    }
}
