package org.testd.ui.util;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.util.Collection;

public class MyVBox<T> extends VBox {

    private ObservableList<T> paramChildren = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public MyVBox() {
        paramChildren.addListener((ListChangeListener<T>) c -> {
            super.getChildren().clear();
            super.getChildren().addAll((Collection<? extends Node>) c.getList());
        });
    }

    public ObservableList<T> getMyChildren() {
        return paramChildren;
    }

    @Override
    public ObservableList<Node> getChildren() {
        return (ObservableList<Node>) getMyChildren();
    }
}
