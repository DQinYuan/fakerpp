package org.testd.ui.controller;

import javafx.scene.Node;
import org.springframework.stereotype.Component;
import org.testd.ui.model.DataSourceInfoProperty;
import org.testd.ui.model.TableProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DrawBoardController {

    private Map<String, TableProperty> propMap;
    private List<Node> elements;

    public void init(Map<String, TableProperty> propMap, List<Node> elements) {
        this.propMap = propMap;
        this.elements = elements;
    }

    public boolean tableNameExists(String name) {
        return propMap.containsKey(name);
    }

    public boolean tableNameExists(String name, TableProperty except) {
        if (!propMap.containsKey(name)) {
            return false;
        }
        return propMap.get(name) != except;
    }

    public List<TableProperty> tablesExcept(TableProperty except) {
        return propMap.values().stream()
                .filter(t -> t != except)
                .collect(Collectors.toList());
    }

    public Optional<TableProperty> dsInUse(DataSourceInfoProperty dataSource) {
        return propMap.values().stream()
                .filter(tp -> tp.getDs().get() == dataSource)
                .findFirst();
    }

    public void append(Node node) {
        elements.add(node);
    }

    public void remove(Node node) {
        elements.remove(node);
    }



}

