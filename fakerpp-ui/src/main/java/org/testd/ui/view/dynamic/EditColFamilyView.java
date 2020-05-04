package org.testd.ui.view.dynamic;

import com.google.common.collect.ImmutableSet;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import lombok.RequiredArgsConstructor;
import org.controlsfx.control.CheckListView;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.testd.fakerpp.core.parser.ast.Table;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.ColProperty;
import org.testd.ui.vo.ColFamilyVO;
import org.testd.ui.util.FxDialogs;
import org.testd.ui.util.Stages;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EditColFamilyView extends BorderPane {

    private final ColPropertyFactory colPropertyFactory;

    @FXML
    private TextArea newCols;

    @FXML
    private CheckListView<String> catchOtherCols;

    private ColFamilyVO colFamilyVO;
    private List<ColFamilyVO> otherColFamilies;
    private MyTableView ownerTable;

    public void initFromMyTableView(MyTableView tableView, ColFamilyVO colFamilyVO) {
        this.ownerTable = tableView;
        this.otherColFamilies = tableView.getColFamiliesExceptJoin().stream()
                .filter(cf -> !cf.equals(colFamilyVO))
                .collect(Collectors.toList());
        otherColFamilies.forEach(
                cp -> catchOtherCols.getItems().addAll(cp.colsStr())
        );

        this.colFamilyVO = colFamilyVO;

        newCols.setText(
                String.join("\n", colFamilyVO.colsStr())
        );
    }

    @FXML
    private void handleOk() {
        // add new cols and delete empty strings
        Set<String> extraCols = Pattern.compile("\n")
                .splitAsStream(newCols.getText())
                .filter(s -> !s.trim().equals(""))
                .map(String::trim)
                .collect(ImmutableSet.toImmutableSet());

        // catch other cols and delete from origin col families
        ObservableList<String> catchCols = catchOtherCols.getCheckModel().getCheckedItems();

        if (CollectionUtils.isEmpty(extraCols) &&
                CollectionUtils.isEmpty(catchCols)) {
            FxDialogs.showError("Edit Col Family Error",
                    "Col family empty", "Col family can not be empty!!");
            return;
        }
        if (otherColFamilies.stream()
                .map(ColFamilyVO::colsProperty)
                .flatMap(Set::stream)
                .anyMatch(cp -> extraCols.contains(cp.getColName()))) {
            FxDialogs.showError("Edit Col Family Error",
                    "Col Name duplicate", "Col Name can not duplicate!!");
            return;
        }

        Set<ColProperty> extraColProperties =
                colPropertyFactory.colPropertiesWithListener(extraCols, ownerTable);

        ImmutableSet.Builder<ColProperty> currentColsBuilder = ImmutableSet.builder();
        currentColsBuilder.addAll(extraColProperties);
        currentColsBuilder.addAll(otherColFamilies.stream()
                .flatMap(cfvo -> cfvo.moveCols(catchCols).stream())
                .collect(Collectors.toList())
        );

        colFamilyVO.replace(currentColsBuilder.build());

        colFamilyVO.visibleProperty().set(true);
        Stages.closeWindow(getScene().getWindow());
    }

}
