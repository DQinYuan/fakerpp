package org.testd.ui.view.dynamic;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.concurrent.ThreadLocalRandom;

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
