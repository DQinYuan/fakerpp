package org.testd.ui.view.dynamic;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.ColFamilyProperty;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JoinReceiveView extends BorderPane implements ColFamilyViewInterface {

    private final FxWeaver fxWeaver;

    @FXML
    private void initialize() {
    }

    @Override
    public ColFamilyProperty getColFamilyProperty() {
        return null;
    }
}