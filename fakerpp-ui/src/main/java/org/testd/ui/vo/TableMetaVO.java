package org.testd.ui.vo;

import com.google.common.annotations.VisibleForTesting;
import javafx.beans.property.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.ui.model.DataSourceInfoProperty;
import org.testd.ui.model.TableProperty;

public class TableMetaVO {

    private final StringProperty nameProperty;
    private final ObjectProperty<DataSourceInfoProperty> dataSourceProperty;
    private final IntegerProperty numberProperty;

    @VisibleForTesting
    public TableMetaVO(String name, String ds, int number) {
        nameProperty = new SimpleStringProperty(name);
        dataSourceProperty = new SimpleObjectProperty<>(new DataSourceInfoProperty(new DataSourceInfo(ds, "", "",
                10,  "", "", "")));
        numberProperty = new SimpleIntegerProperty(number);
    }

    @VisibleForTesting
    public TableMetaVO(StringProperty nameProperty, ObjectProperty<DataSourceInfoProperty> dataSourceProperty, IntegerProperty numberProperty) {
        this.nameProperty = nameProperty;
        this.dataSourceProperty = dataSourceProperty;
        this.numberProperty = numberProperty;
    }

    public TableMetaVO(TableProperty tableProperty) {
        nameProperty = tableProperty.getName();
        dataSourceProperty = tableProperty.getDs();
        numberProperty = tableProperty.getNum();
    }

    public StringProperty nameProperty() {
        return nameProperty;
    }


    public IntegerProperty numberProperty() {
        return numberProperty;
    }

    public ObjectProperty<DataSourceInfoProperty> dataSourceProperty() {
        return dataSourceProperty;
    }
}
