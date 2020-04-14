package org.testd.ui.view.dynamic;

import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.testd.ui.fxweaver.core.FxmlView;
import org.testd.ui.vo.ColFamilyVO;
import org.testd.ui.model.ColProperty;
import org.testd.ui.model.JoinType;
import org.testd.ui.util.BindingUtil;
import org.testd.ui.util.Equaler;
import org.testd.ui.util.TypeUtil;

@Component
@FxmlView
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JoinView extends BorderPane implements ColFamilyViewInterface {

    private static final String normalStyle = "-fx-border-color: black;-fx-border-width: 0.3;";
    private static final String sendStyle = "-fx-border-color: red; -fx-border-width: 2;";
    private static final String receiveStype = "-fx-border-color: blue; -fx-border-width: 2;";

    @FXML
    private Label joinText;
    @FXML
    private ColFamilyInputMenu partInCols;

    private String effectStyle;
    private ColFamilyVO colFamilyVO;
    private boolean send;

    @FXML
    private void initialize() {
    }

    public boolean isSend() {
        return send;
    }

    public void init(ObservableSet<ColProperty> colsSet) {
        this.colFamilyVO = new ColFamilyVO(colsSet);

        // bind ui and property
        BindingUtil.mapContent(
                TypeUtil.saftCast(partInCols.getChildren(), Label.class),
                colFamilyVO.colsProperty(),
                col -> new Label(col.getColName()),
                Equaler.withExtrator(Label::getText));
    }

    public void init1(boolean send,
                      JoinType joinType,
                      String counterTableName) {
        this.send = send;
        String generatorText;
        if (send) {
            this.effectStyle = sendStyle;
            generatorText = String.format(joinType + " Join to %s", counterTableName);
        } else {
            this.effectStyle = receiveStype;
            generatorText = String.format(joinType + " Join from %s", counterTableName);
        }

        this.joinText.setText(generatorText);
        setStyle(normalStyle);
    }

    public void toggleEffect() {
        if (getStyle().equals(normalStyle)) {
            setStyle(this.effectStyle);
        } else {
            setStyle(normalStyle);
        }
    }

    @Override
    public ColFamilyVO getColFamilyVO() {
        return colFamilyVO;
    }

    public void addMenuHandler(EventHandler<ActionEvent> editHandler,
                               EventHandler<ActionEvent> deleteHandler) {
        partInCols.setFollowRightMenu(
                new FollowRightMouseMenu.EntryNameAndAction(
                        "edit connection",
                        mouseEvent -> editHandler
                ),
                new FollowRightMouseMenu.EntryNameAndAction(
                        "delete connection",
                        mouseEvent -> deleteHandler
                )
        );
    }
}
