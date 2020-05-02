package org.testd.ui.util;

import com.dlsc.formsfx.model.structure.Element;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.structure.NodeElement;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Forms {

    public static Region renderForm(String title, List<Element> elements) {
        return renderForm(title, elements, ()->{});
    }

    public static Region renderForm(String title, List<Element> elements, Runnable okAction) {
        List<Element> extendedElements = new ArrayList<>(elements);

        // ok button
        Button okBu = new Button("OK");
        okBu.setId("formOkButton");
        okBu.setAlignment(Pos.CENTER);

        // build form
        extendedElements.add(NodeElement.of(new StackPane(okBu)));
        Form form = Form.of(Group.of(extendedElements.toArray(new Element[0]))).title(title);
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
                okAction.run();
            }
        });

        FormRenderer formRenderer = new FormRenderer(form);
        formRenderer.setMinWidth(600);

        return formRenderer;
    }

}
