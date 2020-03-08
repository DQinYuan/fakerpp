package org.testd.ui.view.dynamic;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.ColFamilyProperty;
import org.testd.ui.model.JoinType;

import java.util.Collection;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JoinSendView extends VBox implements ColFamilyViewInterface {

    @FXML
    private Label joinLabel;

    @FXML
    private void initialize() {
    }

    public void setJoinTypeAndTarget(JoinType joinType, String targetTableName) {
        String base = " Join To " + targetTableName;
        switch (joinType) {
            case LEFT:
                joinLabel.setText("Left" + base);
                break;
            case RIGHT:
                joinLabel.setText("Right " + base);
                break;
        }
    }

    public void addColFamilies(Collection<ColFamilyView> colFamilyViews) {
        getChildren().addAll(colFamilyViews);
    }

    @Override
    public ColFamilyProperty getColFamilyProperty() {
        return null;
    }
}
