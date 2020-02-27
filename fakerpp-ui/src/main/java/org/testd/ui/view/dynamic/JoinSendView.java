package org.testd.ui.view.dynamic;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JoinSendView extends VBox {

    private final FxWeaver fxWeaver;

    @FXML
    private void initialize() {
        getChildren().add(fxWeaver.loadControl(ColFamilyView.class));
        getChildren().add(fxWeaver.loadControl(ColFamilyView.class));
        getChildren().add(fxWeaver.loadControl(ColFamilyView.class));
    }

}
