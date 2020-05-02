package org.testd.ui.view.dynamic;

import javafx.collections.ObservableSet;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.testd.ui.model.ColProperty;
import org.testd.ui.util.BindingUtil;

public class ColFamilyInputMenu extends VBox {

    public ColFamilyInputMenu() {
        setAlignment(Pos.CENTER_LEFT);
        setPrefHeight(50.0);
        setPrefWidth(111.0);
    }

    public void setFollowRightMenu(FollowRightMouseMenu.EntryNameAndAction... entryNameAndActions) {
        addEventHandler(MouseEvent.MOUSE_CLICKED,
                new FollowRightMouseMenu(true,this,
                entryNameAndActions));
    }

    public void bindToCols(ObservableSet<ColProperty> cols) {
        BindingUtil.mapContent(getChildren(),
                cols,
                colProperty -> new Label(colProperty.getColName()),
                (l1, l2) -> ((Label) l1).getText().equals(((Label) l2).getText()));
    }


}
