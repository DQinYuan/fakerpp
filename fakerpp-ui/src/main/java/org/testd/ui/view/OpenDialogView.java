package org.testd.ui.view;

import com.google.common.collect.ImmutableMap;
import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;
import lombok.RequiredArgsConstructor;
import org.javatuples.Pair;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.parser.ERMLParser;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.fakerpp.core.parser.ast.Meta;
import org.testd.ui.DefaultsConfig;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.UiPreferences;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.ERMLProperty;
import org.testd.ui.util.FxDialogs;
import org.testd.ui.util.XmlUtil;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.joox.JOOX.$;

@Component
@FxmlView
@RequiredArgsConstructor
public class OpenDialogView {

    private final ERMLParser ermlParser;

    private final PrimaryStageHolder primaryStageHolder;

    private final MainWindowView mainWindowView;

    private final DefaultsConfig defaultsConfig;

    private final UiPreferences uiPreferences;

    @FXML
    private void handleOpenEmpty() {
        ERML emptyErml = new ERML(
                new Meta(defaultsConfig.getLocalesInfo().getDefaultLocale(),
                        uiPreferences.getDataSources()),
                ImmutableMap.of()
        );
        primaryStageHolder.changeSceneFullScreenWithParam(MainWindowView.class,
                v -> v.initFromErml(ERMLProperty.map(emptyErml), null)
        );
    }

    @FXML
    private void handleOpenDirectory() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Open Model From Directory");
        File selectedFile = dirChooser.showDialog(primaryStageHolder.child());
        if (selectedFile == null || !selectedFile.isDirectory()) {
            FxDialogs.showError("Select Error", "Choose invalid ",
                    "You should choose a directory");
            return;
        }

        try {
            Path selectedPath = selectedFile.toPath();
            ERML erml = ermlParser.parseDir(selectedPath);
            ERMLProperty ermlProperty = ERMLProperty.map(erml);

            // update pos
            Path posesPath = selectedPath.resolve(ERMLProperty.posesXmlPath);
            if (Files.exists(posesPath)) {
                Map<String, Pair<Double, Double>> poses = parsePoses(posesPath);
                ermlProperty.updatePoses(poses);
            }

            primaryStageHolder.changeSceneFullScreenWithParam(MainWindowView.class,
                    v -> v.initFromErml(ermlProperty,
                            selectedFile.getName())
            );
        } catch (Exception e) {
            FxDialogs.showException("Parse Error", "invalid directory",
                    "Please choose a valid directory", e);
        }
    }

    private Map<String, Pair<Double, Double>> parsePoses(Path posesPath) throws IOException, SAXException {
        Map<String, Pair<Double, Double>> res = new HashMap<>();
        $(XmlUtil.rootElement(new String(Files.readAllBytes(posesPath))))
                .children("pos")
                .each(ctx -> res.put(
                        $(ctx).attr("table"),
                        new Pair<>(
                                Double.parseDouble($(ctx).attr("x")),
                                Double.parseDouble($(ctx).attr("y"))
                        )
                ));

        return res;
    }

    @FXML
    private void handleOpenDatabase() {
        primaryStageHolder.changeScene(SelectReverseDataSourceView.class);
    }

}
