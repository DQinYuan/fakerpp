package org.testd.ui.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class MyVBox<T> extends VBox {

    private ObservableList<T> paramChildren = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public MyVBox() {
        BindingUtil.bindContentTypeUnsafe(super.getChildren(), paramChildren);
    }

    public ObservableList<T> getMyChildren() {
        return paramChildren;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return (ObservableList<Node>) getMyChildren();
    }
}
