package org.testd.ui.util;

import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Forms {

    public static Parent renderForm(Form form) {
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(new FormRenderer(form));

        // ok button
        Button okBu = new Button("OK");
        form.validProperty().addListener((ob, oldV, newV) -> {
            if (newV) {
                okBu.setDisable(false);
            } else {
                okBu.setDisable(true);
            }
        });
        okBu.setOnAction(e -> {
            // propagate bind
            if (form.isValid()) {
                form.persist();
                Button button = (Button) e.getSource();
                Stage stage = (Stage) button.getScene().getWindow();
                stage.close();
            }
        });
        BorderPane.setAlignment(okBu, Pos.CENTER);

        borderPane.setBottom(okBu);
        borderPane.setPrefWidth(600);
        borderPane.setPadding(new Insets(5));

        return borderPane;
    }

}
