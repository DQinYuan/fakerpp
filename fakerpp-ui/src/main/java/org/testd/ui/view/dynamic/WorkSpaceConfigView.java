package org.testd.ui.view.dynamic;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.ui.UiPreferences;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.util.Stages;

import java.io.File;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class WorkSpaceConfigView extends VBox {

    private final UiPreferences uiPreferences;

    @FXML
    private Label workspaceDisplay;

    @FXML
    private void initialize() {
        workspaceDisplay.setText(
                uiPreferences.get(UiPreferences.workSpaceKey) == null ?
                        "None" :
                        uiPreferences.get(UiPreferences.workSpaceKey)
        );
    }

    private File selectDir() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("please select a workspace");
        return dirChooser.showDialog(Stages.child(getScene().getWindow()));
    }

    @FXML
    private void handleNewWorkspace() {
        File selectedFile = selectDir();
        while (selectedFile == null || !selectedFile.isDirectory()) {
            selectedFile = selectDir();
        }

        String workSpacePath = selectedFile.getAbsolutePath();
        uiPreferences.put(UiPreferences.workSpaceKey, workSpacePath);
        workspaceDisplay.setText(workSpacePath);
    }

}
