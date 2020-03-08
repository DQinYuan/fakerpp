package org.testd.ui.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class TableMetaProperty {

    private final SimpleStringProperty nameProperty;
    private final SimpleBooleanProperty virtualProperty;
    private final SimpleIntegerProperty numberProperty;

    public TableMetaProperty() {
        this("Test", false, 0);
    }

    public TableMetaProperty(String name, boolean virtual, int number) {
        nameProperty = new SimpleStringProperty(name);
        virtualProperty = new SimpleBooleanProperty(virtual);
        numberProperty = new SimpleIntegerProperty(number);
    }

    public SimpleStringProperty nameProperty() {
        return nameProperty;
    }

    public SimpleBooleanProperty virtualProperty() {
        return virtualProperty;
    }

    public SimpleIntegerProperty numberProperty() {
        return numberProperty;
    }
}
