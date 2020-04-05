package org.testd.ui.view;

import com.google.common.collect.ImmutableMap;
import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.parser.ERMLParser;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.fakerpp.core.parser.ast.Meta;
import org.testd.ui.DefaultsConfig;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.util.FxDialogs;

import java.io.File;

@Component
@FxmlView
@RequiredArgsConstructor
public class OpenDialogView {

    private final ERMLParser ermlParser;

    private final PrimaryStageHolder primaryStageHolder;

    private final MainWindowView mainWindowView;

    private final DefaultsConfig defaultsConfig;

    @FXML
    private void handleOpenEmpty() {
        ERML emptyErml = new ERML(
                new Meta(defaultsConfig.getLocalesInfo().getDefaultLocale(), ImmutableMap.of()),
                ImmutableMap.of()
        );
        primaryStageHolder.changeSceneFullSceneWithParam(MainWindowView.class,
                emptyErml, mainWindowView::initFromErml);
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
