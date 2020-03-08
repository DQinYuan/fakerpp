package org.testd.ui.view.dynamic;

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
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.model.ColFamilyProperty;
import org.testd.ui.util.FxDialogs;
import org.testd.ui.util.Stages;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EditColFamilyView extends BorderPane {

    @FXML
    private TextArea newCols;

    @FXML
    private CheckListView<String> catchOtherCols;

    private ColFamilyProperty colFamilyProperty;
    private List<ColFamilyProperty> otherColFamilies;

    public void initFromMyTableView(MyTableView tableView, ColFamilyProperty colFamilyProperty) {
        this.otherColFamilies = tableView.getColFamilyProperty().stream()
                .filter(cf -> !cf.equals(colFamilyProperty))
                .collect(Collectors.toList());
        otherColFamilies.forEach(
                cp -> catchOtherCols.getItems().addAll(cp.colsProperty())
        );

        this.colFamilyProperty = colFamilyProperty;

        newCols.setText(
                String.join("\n", colFamilyProperty.colsProperty())
        );
    }

    @FXML
    private void handleOk() {
        // add new cols and delete empty strings
        List<String> extraCols = Pattern.compile("\n")
                .splitAsStream(newCols.getText())
                .filter(s -> !s.trim().equals(""))
                .collect(Collectors.toList());

        // catch other cols and delete from origin col families
        ObservableList<String> catchCols = catchOtherCols.getCheckModel().getCheckedItems();
        otherColFamilies.forEach(cf -> cf.deleteCols(catchCols));

        if (CollectionUtils.isEmpty(extraCols) &&
                CollectionUtils.isEmpty(catchCols)) {
            FxDialogs.showError("Empty Col Family Error",
                    "Col family empty", "Col family can not be empty!!");
            return;
        }

        colFamilyProperty.clearCols();
        colFamilyProperty.addCols(extraCols);
        colFamilyProperty.addCols(catchCols);
        Stages.closeWindow(getScene().getWindow());
    }

}
