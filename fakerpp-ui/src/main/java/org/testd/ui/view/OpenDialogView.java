package org.testd.ui.view;

import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.parser.ERMLParser;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.util.FxDialogs;

import java.io.File;

@Component
@FxmlView
@RequiredArgsConstructor
public class OpenDialogView {

    private final ERMLParser ermlParser;

    private final PrimaryStageHolder primaryStageHolder;

    private final MainWindowView mainWindowView;

    @FXML
    private void handleOpenEmpty() {
        primaryStageHolder.changeSceneFullScene(MainWindowView.class);
    }

    @FXML
    private void handleOpenDirectory() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Open From Directory");
        File selectedFile = dirChooser.showDialog(primaryStageHolder.child());
        if (selectedFile == null || !selectedFile.isDirectory()) {
            FxDialogs.showError("Select Error", "Choose invalid ", "You should choose a directory");
            return;
        }

        try {
            ERML erml = ermlParser.parseDir(selectedFile.toPath());
            primaryStageHolder.changeSceneFullSceneWithParam(MainWindowView.class,
                    erml, mainWindowView::initFromErml);
        } catch (ERMLException e) {
            FxDialogs.showError("Parse Error", "invalid directory",
                    "Please choose a valid directory");
        }
    }

}
