package org.testd.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Component;
import org.testd.ui.model.DataSourceInfoProperty;

import java.util.HashMap;
import java.util.Map;

@Component
public class MetaController {

    private Map<String, DataSourceInfoProperty> propMap = new HashMap<>();
    private ObservableList<DataSourceInfoProperty> dataSourceInfos =
            FXCollections.observableArrayList();

    public void init(Map<String, DataSourceInfoProperty> propMap,
                     ObservableList<DataSourceInfoProperty> dataSourceInfos) {
        this.propMap = propMap;
        this.dataSourceInfos = dataSourceInfos;
    }

    public boolean dsNameExists(String name, DataSourceInfoProperty except) {
        if (!propMap.containsKey(name)) {
            return false;
        }
        return propMap.get(name) != except;
    }

    public ObservableList<DataSourceInfoProperty> getDsInfos() {
        return dataSourceInfos;
    }

}
