package org.testd.ui.view.dynamic;

import com.google.common.annotations.VisibleForTesting;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.ColFamilyProperty;
import org.testd.ui.model.JoinType;
import org.testd.ui.model.TableMetaProperty;
import org.testd.ui.service.TableInfoService;
import org.testd.ui.util.FxDialogs;
import org.testd.ui.util.MyVBox;
import org.testd.ui.util.Stages;
import org.testd.ui.view.MainWindowView;
import org.testd.ui.view.NewConnectionView;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MyTableView extends BorderPane {

    //----------- di
    private final FxWeaver fxWeaver;
    private final MainWindowView mainWindowView;
    private final PrimaryStageHolder primaryStageHolder;
    private final TableInfoService tableInfoService;

    //----------- property
    private TableMetaProperty tableMetaProperty;

    //----------- JavaFx Component
    @FXML
    private MyVBox<ColFamilyViewInterface> colFamiliesInput;

    @FXML
    private ToolBar toolBar;

    @FXML
    private MenuItem deleteTableMenu;

    @FXML
    private Label tableNameLabel;

    @FXML
    private void initialize() {
        dragable();

        deleteTableMenu.setOnAction(event -> mainWindowView.deleteTableFromDrawBoard(this));
    }

    public void initTableMetaProperty(TableMetaProperty metaProperty) {
        this.tableMetaProperty = metaProperty;
        tableNameLabel.textProperty().bind(metaProperty.nameProperty());
    }

    public String getName() {
        return tableMetaProperty.nameProperty().get();
    }

    private static class Delta {
        double x, y;
    }

    private void dragable() {
        final Delta dragDelta = new Delta();
        this.setOnMousePressed(mouseEvent -> {
            dragDelta.x = this.getTranslateX() - mouseEvent.getSceneX();
            dragDelta.y = this.getTranslateY() - mouseEvent.getSceneY();

            this.setCursor(Cursor.NONE);
            this.toFront();
        });
        this.setOnMouseReleased(mouseEvent -> this.setCursor(Cursor.HAND));
        this.setOnMouseDragged(mouseEvent -> {
            double translateX = mouseEvent.getSceneX() + dragDelta.x;
            double translateY = mouseEvent.getSceneY() + dragDelta.y;

            this.setTranslateX(translateX);

            this.setTranslateY(translateY);
        });
    }

    @FXML
    private void handleMetaConf() {
        primaryStageHolder.newSceneInChild(
                TableMetaConfView.getView(tableMetaProperty,
                        name -> !tableInfoService.nameExists(name))
        );
    }

    @FXML
    private void handleNewConnection() {
        List<MyTableView> otherTables = tableInfoService.tablesExcept(this);
        if (CollectionUtils.isEmpty(otherTables)) {
            FxDialogs.showError("new connection error", "no other tables",
                    "there has not other tables");
            return;
        }

        primaryStageHolder.newSceneInChild(NewConnectionView.class);
    }

    public List<ColFamilyProperty> getColFamilyProperty() {
        return colFamiliesInput.getMyChildren()
                .stream().map(ColFamilyViewInterface::getColFamilyProperty)
                .collect(Collectors.toList());
    }

    @FXML
    private void handleNewColFamily() {
        ColFamilyProperty colFamilyProperty = new ColFamilyProperty();

        EditColFamilyView editColFamilyView = fxWeaver.loadControl(EditColFamilyView.class);
        editColFamilyView.initFromMyTableView(this, colFamilyProperty);

        Stages.newSceneInChild(editColFamilyView, this.getScene().getWindow());

        ColFamilyView colFamilyView = fxWeaver.loadControl(ColFamilyView.class);
        colFamilyView.initFromTableAndColFamilyProperty(this, colFamilyProperty);
        colFamiliesInput.getMyChildren().add(colFamilyView);
        colFamilyProperty.colsProperty().addListener((ListChangeListener<String>) c -> {
            if (c.getList().isEmpty()) {
                colFamiliesInput.getMyChildren().remove(colFamilyView);
            }
        });
    }

    public void addTableColFamilies(Collection<ColFamilyViewInterface> tcv) {
        colFamiliesInput.getMyChildren().addAll(tcv);
    }

    public void addTableColFamily(ColFamilyViewInterface colFamilyView) {
        colFamiliesInput.getMyChildren().add(colFamilyView);
    }

    public void deleteTableColFamily(ColFamilyViewInterface colFamilyView) {
        colFamiliesInput.getMyChildren().remove(colFamilyView);
    }

    @VisibleForTesting
    protected void wrapInJoinSendView(String targetTableName, JoinType joinType,
                                      List<ColFamilyView> colFamilyViews) {
        JoinSendView joinSendView = fxWeaver.loadControl(JoinSendView.class);
        joinSendView.setJoinTypeAndTarget(joinType, targetTableName);
        joinSendView.addColFamilies(colFamilyViews);

        colFamiliesInput.getMyChildren().add(joinSendView);
    }
}
