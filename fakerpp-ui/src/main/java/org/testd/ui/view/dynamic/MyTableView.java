package org.testd.ui.view.dynamic;

import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.testd.ui.PrimaryStageHolder;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.ColFamilyProperty;
import org.testd.ui.model.ColProperty;
import org.testd.ui.model.ConnectionProperty;
import org.testd.ui.model.TableMetaProperty;
import org.testd.ui.service.TableInfoService;
import org.testd.ui.util.FxDialogs;
import org.testd.ui.util.MyVBox;
import org.testd.ui.util.Stages;
import org.testd.ui.view.MainWindowView;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
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
    private final BeanFactory beanFactory;

    //----------- property
    private TableMetaProperty tableMetaProperty;
    private Pane drawBoard;

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

    public void initTableMetaProperty(TableMetaProperty metaProperty, Pane drawBoard) {
        this.drawBoard = drawBoard;
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

            double maxX = translateX + getWidth();
            if (maxX > drawBoard.getMinWidth()) {
                drawBoard.setMinWidth(maxX);
            }
            double maxY = translateY + getHeight();
            if (maxY > drawBoard.getMinHeight()) {
                drawBoard.setMinHeight(maxY);
            }

            this.setTranslateX(Math.max(translateX, 0.0));

            this.setTranslateY(Math.max(translateY, 0.0));
        });
    }

    @FXML
    private void handleMetaConf() {
        Stages.newSceneInChild(TableMetaConfView.getView(tableMetaProperty,
                name -> !tableInfoService.nameExistsExcept(name, this)),
                getScene().getWindow());
    }

    @FXML
    private void handleNewConnection() {
        List<MyTableView> otherTables = tableInfoService.tablesExcept(this);
        if (CollectionUtils.isEmpty(otherTables)) {
            FxDialogs.showError("new connection error", "no other tables",
                    "there has not other tables");
            return;
        }

        ConnectionView connectionView = beanFactory.getBean(ConnectionView.class);
        ConnectionProperty connectionProperty = new ConnectionProperty(this);
        connectionView.register(connectionProperty);

        EditConnectionView editConnectionView = fxWeaver.loadControl(EditConnectionView.class);
        editConnectionView.initFromConnectionProperty(connectionProperty);

        Stages.newSceneInChild(editConnectionView, this.getScene().getWindow());
    }

    /**
     *  get except viewType class not meet predicate
     * @param viewType
     * @param predicate
     * @param <T>
     * @return
     */
    public <T extends ColFamilyViewInterface> List<ColFamilyProperty>
        getColFamiliesExcept(Class<T> viewType, Predicate<T> predicate) {
        return colFamiliesInput.getMyChildren().stream()
                .filter(c -> {
                    if (!c.getClass().equals(viewType)) {
                        return true;
                    }

                    return predicate.test(viewType.cast(c));
                })
                .map(ColFamilyViewInterface::getColFamilyProperty)
                .collect(Collectors.toList());
    }

    public List<ColFamilyProperty> getColFamilies() {
        return colFamiliesInput.getMyChildren()
                .stream().map(ColFamilyViewInterface::getColFamilyProperty)
                .collect(Collectors.toList());
    }

    public <T extends ColFamilyViewInterface> List<ColFamilyProperty>
        getColFamilies(Class<T> viewType, Predicate<T> predicate) {
        return colFamiliesInput.getMyChildren().stream()
                .filter(c -> c.getClass().equals(viewType))
                .filter(c -> predicate.test(viewType.cast(c)))
                .map(ColFamilyViewInterface::getColFamilyProperty)
                .collect(Collectors.toList());
    }


    public List<ColFamilyProperty> getColFamiliesExcept(ColFamilyViewInterface expect) {
        return colFamiliesInput.getMyChildren().stream()
                .filter(c -> c != expect)
                .map(ColFamilyViewInterface::getColFamilyProperty)
                .collect(Collectors.toList());
    }

    /**
     * col families except join col families
     *
     * @return
     */
    public List<ColFamilyProperty> getNormalColFamilies() {
        return colFamiliesInput.getMyChildren()
                .stream().filter(vi -> vi instanceof ColFamilyView)
                .map(ColFamilyViewInterface::getColFamilyProperty)
                .collect(Collectors.toList());
    }

    public boolean containsAny(Collection<String> colNames) {
        return getColFamilies().stream()
                .map(ColFamilyProperty::colsProperty)
                .flatMap(Set::stream)
                .anyMatch(colNames::contains);
    }

    public boolean containsAnyExcept(Collection<String> colNames, ColFamilyViewInterface expect) {
        return colFamiliesInput.getMyChildren().stream()
                .filter(c -> c != expect)
                .map(ColFamilyViewInterface::getColFamilyProperty)
                .map(ColFamilyProperty::colsProperty)
                .flatMap(Set::stream)
                .anyMatch(colNames::contains);
    }

    @FXML
    private void handleNewColFamily() {
        ColFamilyProperty colFamilyProperty = new ColFamilyProperty();

        EditColFamilyView editColFamilyView = fxWeaver.loadControl(EditColFamilyView.class);
        editColFamilyView.initFromMyTableView(this, colFamilyProperty);

        ColFamilyView colFamilyView = fxWeaver.loadControl(ColFamilyView.class);
        colFamilyProperty.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                colFamilyView.initFromTableAndColFamilyProperty(this, colFamilyProperty);
                colFamiliesInput.getMyChildren().add(colFamilyView);
            } else {
                colFamiliesInput.getMyChildren().remove(colFamilyView);
            }
        });
        colFamilyProperty.colsProperty().addListener((SetChangeListener<ColProperty>) c -> {
            if (c.getSet().isEmpty()) {
                colFamilyProperty.visibleProperty().set(false);
            }
        });

        Stages.newSceneInChild(editColFamilyView, this.getScene().getWindow());
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

    public void flushColFamilyView() {
        colFamiliesInput.requestFocus();
        colFamiliesInput.layout();
    }
}
