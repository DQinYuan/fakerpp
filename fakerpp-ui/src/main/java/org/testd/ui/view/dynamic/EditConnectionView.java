package org.testd.ui.view.dynamic;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.testd.ui.controller.DrawBoardController;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.vo.ColFamilyVO;
import org.testd.ui.model.ColProperty;
import org.testd.ui.model.ConnectionProperty;
import org.testd.ui.model.JoinType;
import org.testd.ui.util.FxDialogs;
import org.testd.ui.util.FxProperties;
import org.testd.ui.util.Stages;
import org.testd.ui.view.DrawBoardView;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    private ComboBox<String> targetInput;

    private ConnectionProperty connectionProperty;

    @FXML
    private void initialize() {
    }

    public void initFromConnectionProperty(ConnectionProperty connectionProperty) {
        assert connectionProperty.sourceProperty().get() != null;
        this.connectionProperty = connectionProperty;

        // init title
        MyTableView sourceTable = connectionProperty.sourceProperty().getValue();
        title.setText(String.format("Connection From %s",
                sourceTable.getName()));

        // Target Table
        List<String> otherTables = drawBoardController.tablesExcept(sourceTable.tableProperty())
                .stream().map(tp -> tp.getName().get()).collect(Collectors.toList());
        targetInput.getItems().addAll(otherTables);

        FxProperties.runIfExists(connectionProperty.targetProperty(),
                targetTable -> {
                    targetInput.getSelectionModel().select(targetTable.getName());
                    targetInput.setDisable(true);
                });

        // Join type
        FxProperties.runIfExists(connectionProperty.joinTypeProperty(),
                this::selectByJoinType);

        // Col families
        List<ColFamilyVO> selectableColFamilies = sourceTable
                .getColFamiliesExcept(JoinView.class, joinView -> !joinView.isSend());

        connectionProperty.sendRecvMap().forEach((s, r) -> {
            ColFamilyMappingView colFamilyMappingView =
                    fxWeaver.loadControl(ColFamilyMappingView.class);
            colFamilyMappingView.initFromOriginAndMappingCol(s.getColName(),
                    r.getColName());
            colFamilyMappingView.select();
            colFamiliesSelectBoxes.getChildren().add(colFamilyMappingView);
        });

        selectableColFamilies.stream()
                .map(ColFamilyVO::colsProperty)
                .flatMap(Set::stream)
                .filter(col -> !connectionProperty
                        .sendRecvMap().containsKey(col))
                .map(col -> {
                    ColFamilyMappingView colFamilyMappingView =
                            fxWeaver.loadControl(ColFamilyMappingView.class);
                    colFamilyMappingView.initFromOriginAndMappingCol(col.getColName(),
                            col.getColName());
                    return colFamilyMappingView;
                }).forEach(colFamiliesSelectBoxes.getChildren()::add);
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
        String targetTable = targetInput.getSelectionModel()
                .getSelectedItem();
        if (targetTable == null) {
            FxDialogs.showError("New Connection Error",
                    "Target Table Invalid",
                    "Target table can not be empty");
            return;
        }
        MyTableView targetTableView = drawBoardView.getTableByName(targetTable);

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

        Map<String, String> resColMap = selectedMapping.stream()
                .collect(ImmutableMap.toImmutableMap(
                        ColFamilyMappingView::getOrigin,
                        ColFamilyMappingView::getTarget
                ));
        if (resColMap.values().stream().distinct().count() != resColMap.size()) {
            FxDialogs.showError("New Connection Error",
                    "Join Cols Invalid",
                    "Target col duplicate!!");
            return;
        }

        Set<String> receiveCols = new HashSet<>(resColMap.values());
        Predicate<String> recvChecker = connectionProperty.recvChecker(targetTableView);
        for (String receiveCol : receiveCols) {
            if (!recvChecker.test(receiveCol)) {
                FxDialogs.showError("New Connection Error",
                        "Join Cols Invalid",
                        "Target col duplicate with existing col in target table!!");
                return;
            }
        }

        // check pass, modify connectionProperty
        connectionProperty.targetProperty().set(targetTableView);
        connectionProperty.joinTypeProperty().set(getJoinType());

        Map<ColProperty, ColProperty> newSendRecv = new LinkedHashMap<>();
        for (Map.Entry<String, String> sendRecvEntry : resColMap.entrySet()) {
            ColProperty sendProperty = new ColProperty(sendRecvEntry.getKey());
            ColProperty recvProperty = colPropertyFactory.colPropertyWithListener(sendRecvEntry.getValue(),
                    targetTableView);
            // when send col deleted, corresponding recv col must be deleted at same time
            sendProperty.addDeleteListener(colName ->
                connectionProperty.sendRecvMap().remove(sendProperty));
            newSendRecv.put(sendProperty, recvProperty);
        }

        connectionProperty.replaceSendRecv(newSendRecv);

        if (!connectionProperty.visibleProperty().get()) {
            connectionProperty.visibleProperty().set(true);
        }

        Stages.closeWindow(getScene().getWindow());

    }

}
