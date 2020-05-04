package org.testd.ui.view.dynamic;

import com.google.common.collect.ImmutableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.RequiredArgsConstructor;
import org.javatuples.Pair;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.testd.ui.controller.DrawBoardController;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.TableProperty;
import org.testd.ui.vo.ConnectionVO;
import org.testd.ui.vo.ColFamilyVO;
import org.testd.ui.model.ColProperty;
import org.testd.ui.model.JoinType;
import org.testd.ui.util.FxDialogs;
import org.testd.ui.util.FxProperties;
import org.testd.ui.util.Stages;
import org.testd.ui.view.DrawBoardView;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EditConnectionView extends VBox {

    private final FxWeaver fxWeaver;
    private final DrawBoardView drawBoardView;
    private final ColPropertyFactory colPropertyFactory;
    private final DrawBoardController drawBoardController;

    @FXML
    private VBox colFamiliesSelectBoxes;
    @FXML
    private Label title;
    @FXML
    private ToggleGroup joinTypeGroup;
    @FXML
    private RadioButton leftRadio;
    @FXML
    private RadioButton rightRadio;
    @FXML
    private CheckBox randomInput;
    @FXML
    private HBox randomInputHbox;
    @FXML
    private ComboBox<TableProperty> targetInput;

    private ConnectionVO connectionVO;

    @FXML
    private void initialize() {
        randomInputHbox.visibleProperty().bind(joinTypeGroup
                .selectedToggleProperty().isEqualTo(rightRadio));
    }

    public void initFromConnectionProperty(ConnectionVO connectionVO) {
        assert connectionVO.sourceProperty().get() != null;
        this.connectionVO = connectionVO;

        // init title
        MyTableView sourceTable = connectionVO.sourceProperty().getValue();
        title.setText(String.format("Connection From %s",
                sourceTable.getName()));

        // Target Table
        List<TableProperty> otherTables =
                drawBoardController.tablesExcept(sourceTable.tableProperty());
        Callback<ListView<TableProperty>, ListCell<TableProperty>> cellFactory =
                listView -> new ListCell<TableProperty>(){
                    @Override
                    protected void updateItem(TableProperty item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName().get());
                        }
                    }
                };
        targetInput.setButtonCell(cellFactory.call(null));
        targetInput.setCellFactory(cellFactory);
        targetInput.getItems().addAll(otherTables);

        FxProperties.runIfExists(connectionVO.targetProperty(),
                targetTable -> {
                    targetInput.getSelectionModel().select(targetTable.tableProperty());
                    targetInput.setDisable(true);
                });

        // Join type
        FxProperties.runIfExists(connectionVO.joinTypeProperty(),
                this::selectByJoinType);

        // Random checkbox (for right join)
        randomInput.setSelected(connectionVO.randomProperty().get());

        // Selectable Col families from Source Table
        List<ColFamilyVO> selectableColFamilies = sourceTable
                .getColFamiliesExcept(JoinView.class, joinView -> !joinView.isSend());

        connectionVO.sendRecvMap().forEach((s, r) -> {
            ColFamilyMappingView colFamilyMappingView =
                    fxWeaver.loadControl(ColFamilyMappingView.class);
            colFamilyMappingViewInit(colFamilyMappingView,
                    s.getColName(),r.getColName(), connectionVO.sendRecvMap());
            colFamilyMappingView.select();
            colFamiliesSelectBoxes.getChildren().add(colFamilyMappingView);
        });

        selectableColFamilies.stream()
                .map(ColFamilyVO::colsProperty)
                .flatMap(Set::stream)
                .filter(col -> !connectionVO
                        .sendRecvMap().containsKey(col))
                .map(col -> {
                    ColFamilyMappingView colFamilyMappingView =
                            fxWeaver.loadControl(ColFamilyMappingView.class);
                    colFamilyMappingViewInit(colFamilyMappingView,
                            col.getColName(), col.getColName(), connectionVO.sendRecvMap());
                    return colFamilyMappingView;
                }).forEach(colFamiliesSelectBoxes.getChildren()::add);
    }

    /**
     * init targetInput before call this method
     * @param colFamilyMappingView
     * @param originCol
     * @param targetCol
     * @param sendRecv
     */
    private void colFamilyMappingViewInit(ColFamilyMappingView colFamilyMappingView,
                                          String originCol,
                                          String targetCol,
                                          Map<ColProperty, ColProperty> sendRecv) {
        if (targetInput.isDisable()) {
            colFamilyMappingView.initTargetImmutable(
                    originCol, targetCol, targetInput.getValue(),
                    sendRecv.values().stream().map(ColProperty::getColName)
                            .collect(Collectors.toSet())
            );
        } else {
            colFamilyMappingView.initTargetMutable(
                    originCol, targetCol, targetInput.valueProperty()
            );
        }
    }

    private void selectByJoinType(JoinType joinType) {
        switch (joinType) {
            case RIGHT:
                joinTypeGroup.selectToggle(rightRadio);
                break;
            case LEFT:
                joinTypeGroup.selectToggle(leftRadio);
                break;
        }
    }

    private JoinType getJoinType() {
        if (joinTypeGroup.getSelectedToggle() == rightRadio) {
            return JoinType.RIGHT;
        } else {
            return JoinType.LEFT;
        }
    }

    @FXML
    private void handleOk() {
        // check
        TableProperty targetTable = targetInput.getSelectionModel()
                .getSelectedItem();
        if (targetTable == null) {
            FxDialogs.showError("New Connection Error",
                    "Target Table Invalid",
                    "Target table can not be empty");
            return;
        }
        MyTableView targetTableView = drawBoardView.getTableByName(targetTable.getName().get());

        List<ColFamilyMappingView> selectedMapping = colFamiliesSelectBoxes
                .getChildren()
                .stream()
                .map(node -> ColFamilyMappingView.class.cast(node))
                .filter(ColFamilyMappingView::selected)
                .collect(ImmutableList.toImmutableList());
        if (CollectionUtils.isEmpty(selectedMapping)) {
            FxDialogs.showError("New Connection Error",
                    "Join Cols Invalid",
                    "Part in cols can not be empty");
            return;
        }

        // isUserDefined -> List<Pair<origin col, target col>>
        Map<Boolean, List<Pair<String, String>>> extralAndCatchCols = selectedMapping.stream()
                .collect(Collectors.partitioningBy(
                        ColFamilyMappingView::isUserDefined,
                        Collectors.mapping(
                                cmv -> new Pair<>(cmv.getOrigin(), cmv.getTarget()),
                                Collectors.toList()
                        )
                ));

        List<Pair<String, String>> extraCols = extralAndCatchCols.get(true);
        List<Pair<String, String>> catchCols = extralAndCatchCols.get(false);

        // check target col duplicate
        if (Stream.of(extraCols.stream(), catchCols.stream())
                .flatMap(Function.identity()).distinct().count()
                != extraCols.size() + catchCols.size()) {
            FxDialogs.showError("New Connection Error",
                    "Join Cols Invalid",
                    "Target col duplicate!!");
            return;
        }

        List<String> extraReceiveCols = extraCols.stream().map(Pair::getValue1)
                .collect(ImmutableList.toImmutableList());
        Predicate<String> recvChecker = connectionVO.recvChecker(targetTableView);
        for (String extraReceiveCol : extraReceiveCols) {
            if (!recvChecker.test(extraReceiveCol)) {
                FxDialogs.showError("New Connection Error",
                        "Join Cols Invalid",
                        "Target col duplicate with existing col in target table!!");
                return;
            }
        }

        ////// check pass, modify connectionVO

        connectionVO.randomProperty().set(randomInput.isSelected());
        connectionVO.targetProperty().set(targetTableView);
        connectionVO.joinTypeProperty().set(getJoinType());
        targetTableView.relateConnection(connectionVO);
        connectionVO.sourceProperty().get().relateConnection(connectionVO);

        // move catch cols

        Map<ColProperty, ColProperty> newSendRecv = new LinkedHashMap<>();
        Map<String, ColProperty> movingTargetCols = targetTableView.moveCols(
                catchCols.stream().map(Pair::getValue1).collect(Collectors.toSet())
        ).stream().collect(Collectors.toMap(
                ColProperty::getColName,
                Function.identity()
        ));
        for (Pair<String, String> catchCol : catchCols) {
            ColProperty sendProperty = new ColProperty(catchCol.getValue0());
            ColProperty recvProperty = movingTargetCols.get(catchCol.getValue1());
            // when send col deleted, corresponding recv col must be deleted at same time
            sendProperty.addDeleteListener(colName ->
                    connectionVO.sendRecvMap().remove(sendProperty));
            newSendRecv.put(sendProperty, recvProperty);
        }


        // add extra cols

        for (Pair<String, String> extraCol : extraCols) {
            ColProperty sendProperty = new ColProperty(extraCol.getValue0());
            ColProperty recvProperty = colPropertyFactory.colPropertyWithListener(extraCol.getValue1(),
                    targetTableView);
            // when send col deleted, corresponding recv col must be deleted at same time
            sendProperty.addDeleteListener(colName ->
                    connectionVO.sendRecvMap().remove(sendProperty));
            newSendRecv.put(sendProperty, recvProperty);
        }
        connectionVO.replaceSendRecv(newSendRecv);

        if (!connectionVO.visibleProperty().get()) {
            connectionVO.visibleProperty().set(true);
        }

        Stages.closeWindow(getScene().getWindow());
    }

}
