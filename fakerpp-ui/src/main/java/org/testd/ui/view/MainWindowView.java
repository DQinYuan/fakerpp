package org.testd.ui.view;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.DataSourceInfoProperty;
import org.testd.ui.model.ERMLProperty;
import org.testd.ui.model.MetaProperty;
import org.testd.ui.model.TableProperty;
import org.testd.ui.util.BindingUtil;

import java.util.Map;


@Component
@FxmlView
@RequiredArgsConstructor
public class MainWindowView {

    //------------ di
    private final DrawBoardView drawBoardView;
    private final MetaView metaView;
    private final FxWeaver fxWeaver;

    //------------ JavaFx Component

    @FXML
    private ScrollPane boardScroll;
    @FXML
    private SplitPane mainSplit;

    @FXML
    private void initialize() {
        // init meta
        mainSplit.getItems().set(0, fxWeaver.loadView(MetaView.class));

        // init drawBoard
        boardScroll.setContent(drawBoardView);
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    public void initFromErml(ERML erml) {
        ERMLProperty ermlProperty = ERMLProperty.map(erml);

        // bind meta info
        MetaProperty metaProperty = ermlProperty.getMeta();
        metaView.initFromMetaProperty(metaProperty);

        Map<String, TableProperty> tables = ermlProperty.getTables();
        drawBoardView.init(tables);
    }

}
