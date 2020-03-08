package org.testd.ui.model;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.testd.ui.view.dynamic.ColFamilyView;
import org.testd.ui.view.dynamic.MyTableView;

public class ConnectionProperty {

    private ObjectProperty<MyTableView> target = new SimpleObjectProperty<>();

    private ObjectProperty<JoinType> joinType = new SimpleObjectProperty<>();

    private ListProperty<ColFamilyView> partInColFamilies = new SimpleListProperty<>();


    public ObjectProperty<MyTableView> targetProperty() {
        return target;
    }

    public ObjectProperty<JoinType> joinTypeProperty() {
        return joinType;
    }

    public ListProperty<ColFamilyView> partInColFamiliesProperty() {
        return partInColFamilies;
    }
}
