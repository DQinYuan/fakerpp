package org.testd.ui.view.dynamic;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.util.Stages;
import org.testd.ui.vo.ColFamilyVO;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExcludesView extends BorderPane implements ColFamilyViewInterface  {

    private final FxWeaver fxWeaver;

    @FXML
    private ColFamilyInputMenu excludedColLabels;

    private ColFamilyVO colFamilyVO;
    private MyTableView ownerTable;

    @FXML
    private void initialize() {
        // init edit cols menu
        excludedColLabels.setFollowRightMenu(
                FollowRightMouseMenu.menuEntry("edit cols",
                        ignore -> event -> {
                            EditColFamilyView editColFamilyView = fxWeaver.loadControl(EditColFamilyView.class);
                            editColFamilyView.initFromMyTableView(ownerTable, colFamilyVO);
                            Stages.newSceneInChild(editColFamilyView, getScene().getWindow());
                        }),
                FollowRightMouseMenu.menuEntry("clear exclude cols",
                        ignore -> event -> colFamilyVO.clear())
        );
    }


    public void initFromColFamilyVO(MyTableView ownerTable,
                                    ColFamilyVO colFamilyVO) {
        this.colFamilyVO = colFamilyVO;
        this.ownerTable = ownerTable;

        excludedColLabels.bindToCols(colFamilyVO.colsProperty());
    }

    @Override
    public ColFamilyVO getColFamilyVO() {
        return colFamilyVO;
    }
}
