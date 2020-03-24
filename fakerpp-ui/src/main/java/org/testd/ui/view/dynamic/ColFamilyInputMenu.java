package org.testd.ui.view.dynamic;

import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

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


}
