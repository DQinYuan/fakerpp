package org.testd.ui.view;

import com.google.common.collect.ImmutableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.view.dynamic.ColFamilyMappingView;

@Component
@FxmlView
@RequiredArgsConstructor
public class NewConnectionView {

    private final FxWeaver fxWeaver;

    @FXML
    private VBox colFamiliesSelectHboxes;

    @FXML
    private void initialize() {
        ColFamilyMappingView colFamilyMappingView = fxWeaver.loadControl(ColFamilyMappingView.class);
        colFamilyMappingView.initFromOriginAndMappingColFamily(
                ImmutableList.of("col1", "col2", "col3"),
                ImmutableList.of(new SimpleStringProperty("col1"),
                        new SimpleStringProperty("col2"), new SimpleStringProperty("col3"))
        );
        colFamiliesSelectHboxes.getChildren().add(colFamilyMappingView);
    }

}
