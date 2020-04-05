package org.testd.ui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.parser.ast.Meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class MetaProperty {

    private final StringProperty lang;
    private final ObservableList<DataSourceInfoProperty> dataSourceInfos;

    private MetaProperty(String lang, Map<String, DataSourceInfo> dataSourceInfos) {
        this.lang = new SimpleStringProperty(lang);
        this.dataSourceInfos = dataSourceInfos.values().stream()
                .map(DataSourceInfoProperty::new)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public static MetaProperty map(Meta meta) {
        return new MetaProperty(meta.getLang(),
                meta.getDataSourceInfos());
    }
}
