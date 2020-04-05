package org.testd.ui.view.dynamic;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;


public class FollowRightMouseMenu implements EventHandler<MouseEvent> {

    private final boolean isAcceptBubblingEvent;
    private final Node windowNode; // null
    private final EntryNameAndAction[] entryNameAndActions;

    @RequiredArgsConstructor
    public static class EntryNameAndAction {
        private final String entryName;
        private final Function<MouseEvent,
                EventHandler<ActionEvent>> action;
    }

    public static EntryNameAndAction menuEntry(String entryName,
                                               Function<MouseEvent,
                                                       EventHandler<ActionEvent>> action) {
        return new EntryNameAndAction(entryName, action);
    }

    public FollowRightMouseMenu(boolean acceptBubblingEvent, Node node,
                                EntryNameAndAction... entryNameAndActions) {
        this.isAcceptBubblingEvent = acceptBubblingEvent;
        this.windowNode = node;
        this.entryNameAndActions = entryNameAndActions;
    }

    @Override
    public void handle(MouseEvent event) {
        // right mouse click
        if (event.getButton() == MouseButton.SECONDARY
                // avoid event bubbling
                && (isAcceptBubblingEvent || event.getTarget() == event.getSource())) {
            ContextMenu contextMenu = new ContextMenu();
            for (EntryNameAndAction entryNameAndAction : entryNameAndActions) {
                MenuItem mi = new MenuItem(entryNameAndAction.entryName);
                mi.setId(entryNameAndAction.entryName.replace(" ", "_") + "_mi");
                mi.setOnAction(entryNameAndAction.action.apply(event));
                contextMenu.getItems().add(mi);
            }
            contextMenu.show(windowNode.getScene().getWindow(),
                    event.getScreenX(),
                    event.getScreenY());
        }
    }
}
