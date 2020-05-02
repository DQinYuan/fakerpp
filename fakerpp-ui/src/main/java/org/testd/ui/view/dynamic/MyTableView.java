package org.testd.ui.view.dynamic;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.testd.ui.controller.DrawBoardController;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.DataSourceInfoProperty;
import org.testd.ui.model.JoinType;
import org.testd.ui.util.BindingUtil;
import org.testd.ui.vo.ConnectionVO;
import org.testd.ui.vo.ColFamilyVO;
import org.testd.ui.model.ColProperty;
import org.testd.ui.model.TableProperty;
import org.testd.ui.util.FxDialogs;
import org.testd.ui.util.MyVBox;
import org.testd.ui.util.Stages;
import org.testd.ui.view.DrawBoardView;
import org.testd.ui.view.form.TableMetaConfView;
import org.testd.ui.vo.TableMetaVO;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MyTableView extends BorderPane {

    //----------- di
    private final FxWeaver fxWeaver;
    private final DrawBoardView drawBoardView;
    private final DrawBoardController drawBoardController;
    private final BeanFactory beanFactory;
    private final TableMetaConfView tableMetaConfView;
    private final ColPropertyFactory colPropertyFactory;

    //----------- property
    private TableProperty tableProperty;
    private ColFamilyVO excludesColFamily;

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

        deleteTableMenu.setOnAction(event -> drawBoardController.remove(this));
    }

    private void showExcluedCols() {
        ExcludesView excludesView = fxWeaver.loadControl(ExcludesView.class);
        excludesView.initFromColFamilyVO(this, excludesColFamily);
        addTableColFamily(excludesView);
    }

    public void initTableProperty(TableProperty tableProperty) {
        this.tableProperty = tableProperty;
        tableNameLabel.textProperty().bind(tableProperty.getName());

        // init excludes view when table is virtual
        ObservableSet<ColProperty> excludeColsProperty = FXCollections.observableSet(
                new LinkedHashSet<>(
                        colPropertyFactory.colPropertiesWithListener(tableProperty.getExcludes(),
                                this)
                )
        );
        BindingUtil.mapContent(tableProperty.getExcludes(), excludeColsProperty,
                ColProperty::getColName);
        this.excludesColFamily = new ColFamilyVO(excludeColsProperty);
        if (tableProperty.getDs().get() != null) {
            showExcluedCols();
        }
        tableProperty.getDs().isNull().addListener((observable, oldValue, isNull) -> {
            if (isNull) { // virtual
                excludeColsProperty.clear();
                deleteTableColFamily(ExcludesView.class);
            } else {
                showExcluedCols();
            }
        });

        // init exists col families
        tableProperty.getColFamilies().forEach(cf -> relatedColFamilyVo(cf).visibleProperty().set(true));

        // init position binding
        translateXProperty().bindBidirectional(tableProperty.getX());
        translateYProperty().bindBidirectional(tableProperty.getY());
    }

    public TableProperty tableProperty() {
        return tableProperty;
    }

    public StringProperty nameProperty() {
        return tableProperty.getName();
    }

    public String getName() {
        return tableProperty.getName().get();
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
            if (maxX > drawBoardView.getMinWidth()) {
                drawBoardView.setMinWidth(maxX);
            }
            double maxY = translateY + getHeight();
            if (maxY > drawBoardView.getMinHeight()) {
                drawBoardView.setMinHeight(maxY);
            }

            this.setTranslateX(Math.max(translateX, 0.0));

            this.setTranslateY(Math.max(translateY, 0.0));
        });
    }

    @FXML
    private void handleMetaConf() {
        Stages.newSceneInChild(tableMetaConfView.getView(new TableMetaVO(tableProperty),
                name -> !drawBoardController.tableNameExists(name, tableProperty), () -> {
                }),
                getScene().getWindow());
    }

    @FXML
    private void handleNewConnection() {
        List<TableProperty> otherTables = drawBoardController.tablesExcept(tableProperty);
        if (CollectionUtils.isEmpty(otherTables)) {
            FxDialogs.showError("new connection error", "no other tables",
                    "there has not other tables");
            return;
        }

        ConnectionView connectionView = beanFactory.getBean(ConnectionView.class);
        TableProperty.JoinProperty newJoinProp =
                TableProperty.JoinProperty.defaultProperty(this.getName());
        ConnectionVO connectionVO = new ConnectionVO(this,
                null, newJoinProp, JoinType.defaultType);
        connectionView.init(connectionVO);

        EditConnectionView editConnectionView = fxWeaver.loadControl(EditConnectionView.class);
        editConnectionView.initFromConnectionProperty(connectionVO);

        Stages.newSceneInChild(editConnectionView, this.getScene().getWindow());
    }

    /**
     * get except viewType class not meet predicate
     *
     * @param viewType
     * @param predicate
     * @param <T>
     * @return
     */
    public <T extends ColFamilyViewInterface> List<ColFamilyVO>
    getColFamiliesExcept(Class<T> viewType, Predicate<T> predicate) {
        return colFamiliesInput.getMyChildren().stream()
                .filter(c -> {
                    if (!c.getClass().equals(viewType)) {
                        return true;
                    }

                    return predicate.test(viewType.cast(c));
                })
                .map(ColFamilyViewInterface::getColFamilyVO)
                .collect(Collectors.toList());
    }

    public List<ColFamilyVO> getColFamilies() {
        return colFamiliesInput.getMyChildren()
                .stream().map(ColFamilyViewInterface::getColFamilyVO)
                .collect(Collectors.toList());
    }

    public <T extends ColFamilyViewInterface> List<ColFamilyVO>
    getColFamilies(Class<T> viewType, Predicate<T> predicate) {
        return colFamiliesInput.getMyChildren().stream()
                .filter(c -> c.getClass().equals(viewType))
                .filter(c -> predicate.test(viewType.cast(c)))
                .map(ColFamilyViewInterface::getColFamilyVO)
                .collect(Collectors.toList());
    }


    public List<ColFamilyVO> getColFamiliesExcept(ColFamilyViewInterface expect) {
        return colFamiliesInput.getMyChildren().stream()
                .filter(c -> c != expect)
                .map(ColFamilyViewInterface::getColFamilyVO)
                .collect(Collectors.toList());
    }

    /**
     * @return col families except col families related with join
     */
    public List<ColFamilyVO> getColFamiliesExceptJoin() {
        return colFamiliesInput.getMyChildren()
                .stream().filter(vi -> !(vi instanceof JoinView))
                .map(ColFamilyViewInterface::getColFamilyVO)
                .collect(Collectors.toList());
    }

    public ColFamilyVO relatedColFamilyVo(TableProperty.ColFamilyProperty colFamilyProperty) {
        ColFamilyVO colFamilyVO = new ColFamilyVO(colFamilyProperty,
                colName -> colPropertyFactory.colPropertyWithListener(colName, this));

        ColFamilyView colFamilyView = fxWeaver.loadControl(ColFamilyView.class);
        colFamilyVO.visibleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                colFamilyView.initFromTableAndColFamilyVO(this, colFamilyVO);
                colFamiliesInput.getMyChildren().add(colFamilyView);
                tableProperty.getColFamilies().add(colFamilyProperty);
            } else {
                colFamiliesInput.getMyChildren().remove(colFamilyView);
                tableProperty.getColFamilies().remove(colFamilyProperty);
            }
        });

        // auto invisible when col family is empty
        colFamilyVO.colsProperty().addListener((SetChangeListener<ColProperty>) c -> {
            if (c.getSet().isEmpty()) {
                colFamilyVO.visibleProperty().set(false);
            }
        });

        return colFamilyVO;
    }

    @FXML
    private void handleNewColFamily() {
        TableProperty.ColFamilyProperty colFamilyProperty =
                new TableProperty.ColFamilyProperty(new ArrayList<>(), new ArrayList<>());
        ColFamilyVO colFamilyVO = relatedColFamilyVo(colFamilyProperty);

        EditColFamilyView editColFamilyView = fxWeaver.loadControl(EditColFamilyView.class);
        editColFamilyView.initFromMyTableView(this, colFamilyVO);

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

    public void deleteTableColFamily(Class<?> cfClass) {
        colFamiliesInput.getMyChildren().removeIf(colFamilyView ->
                cfClass.isAssignableFrom(colFamilyView.getClass()));
    }

    public void flushColFamilyView() {
        colFamiliesInput.requestFocus();
        colFamiliesInput.layout();
    }
}
