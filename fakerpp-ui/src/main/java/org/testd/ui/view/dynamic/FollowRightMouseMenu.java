package org.testd.ui.view.dynamic;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.function.Function;


public class FollowRightMouseMenu implements EventHandler<MouseEvent> {

    private final boolean isAcceptBubblingEvent;
    private final Node windowNode; // null
    private final Function<MouseEvent, MenuItem>[] itemGetters;

    public FollowRightMouseMenu(boolean acceptBubblingEvent, Node node, Function<MouseEvent, MenuItem>... items) {
        isAcceptBubblingEvent = acceptBubblingEvent;
        windowNode = node;
        itemGetters = items;
    }

    @Override
    public void handle(MouseEvent event) {
        // right mouse click
        if (event.getButton() == MouseButton.SECONDARY
                // avoid event bubbling
                && (isAcceptBubblingEvent || event.getTarget() == event.getSource())) {
            ContextMenu contextMenu = new ContextMenu();
            for (Function<MouseEvent, MenuItem> itemGetter : itemGetters) {
                contextMenu.getItems().add(itemGetter.apply(event));
            }
            contextMenu.show(windowNode.getScene().getWindow(),
                    event.getScreenX(),
                    event.getScreenY());
        }
    }
}
