package org.testd.ui.view.dynamic;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.ui.fxweaver.core.FxWeaver;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.util.BindingUtil;
import org.testd.ui.util.Stages;
import org.testd.ui.vo.ColFamilyVO;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ColFamilyView extends BorderPane implements ColFamilyViewInterface {

    private final FxWeaver fxWeaver;
    private final BeanFactory beanFactory;

    private ColFamilyVO colFamilyVO;
    private MyTableView ownerTable;
    private BooleanProperty weightVisible = new SimpleBooleanProperty(false);

    @FXML
    private VBox generatorsInput;

    @FXML
    private ColFamilyInputMenu colsInput;

    @FXML
    private void initialize() {
        // init edit cols menu
        colsInput.setFollowRightMenu(
                FollowRightMouseMenu.menuEntry("edit cols",
                        ignore -> event -> {
                            EditColFamilyView editColFamilyView = fxWeaver.loadControl(EditColFamilyView.class);
                            editColFamilyView.initFromMyTableView(ownerTable, colFamilyVO);
                            Stages.newSceneInChild(editColFamilyView, getScene().getWindow());
                        }),
                FollowRightMouseMenu.menuEntry("delete cols",
                        ignore -> event -> {
                            ownerTable.deleteTableColFamily(this);
                            colFamilyVO.clear();
                        }
                )
        );
    }


    public void initFromTableAndColFamilyProperty(MyTableView ownerTable,
                                                  ColFamilyVO colFamilyVO) {
        this.ownerTable = ownerTable;
        this.colFamilyVO = colFamilyVO;

        BindingUtil.mapContent(colsInput.getChildren(),
                colFamilyVO.colsProperty(),
                colProperty -> new Label(colProperty.getColName()),
                (l1, l2) -> ((Label) l1).getText().equals(((Label) l2).getText()));

        assert colFamilyVO.getGeneratorInfos().size() >= 1;

        // weight spinner only visible when generator num >= 2
        generatorsInput.getChildren().addListener((ListChangeListener<Node>) c -> {
            if (c.getList().size() >= 2) {
                weightVisible.set(true);
            } else {
                weightVisible.set(false);
            }
        });

        BindingUtil.mapContent(generatorsInput.getChildren(),
                colFamilyVO.getGeneratorInfos(),
                gInfo -> {
                    GeneratorSelector gs = beanFactory.getBean(GeneratorSelector.class);
                    gs.init(colFamilyVO, gInfo, weightVisible);
                    return gs;
                });
    }

    public ColFamilyVO getColFamilyVO() {
        return colFamilyVO;
    }

}
