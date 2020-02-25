package org.testd.ui.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.ui.fxweaver.core.FxmlView;

import java.util.concurrent.ThreadLocalRandom;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ColFamilyView extends BorderPane {

    @FXML
    private VBox cols;

    @FXML
    private void initialize() {
        cols.getChildren().add(
                new Label(String.valueOf(ThreadLocalRandom.current().nextInt(10000)))
        );
        cols.getChildren().add(
                new Label(String.valueOf(ThreadLocalRandom.current().nextInt(10000)))
        );
        cols.getChildren().add(
                new Label(String.valueOf(ThreadLocalRandom.current().nextInt(10000)))
        );
    }

}
