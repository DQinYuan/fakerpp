package org.testd.ui.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Service;
import org.testd.ui.view.dynamic.MyTableView;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TableInfoService {

    private ObservableList<MyTableView> displayTables = FXCollections.observableArrayList();

    public void addTable(MyTableView tableView) {
        displayTables.add(tableView);
    }

    public void deleteTable(MyTableView tableView) {
        displayTables.remove(tableView);
    }

    public List<MyTableView> tablesExcept(MyTableView tableView) {
        return displayTables.stream()
                .filter(t -> t != tableView)
                .collect(Collectors.toList());
    }

    public boolean nameExistsExcept(String tableName, MyTableView except) {
        return displayTables.stream()
                .filter(tableView -> tableView!=except)
                .anyMatch(tableView ->
                        Objects.equals(tableName, tableView.getName()));
    }

    /**
     * throw Exception if not exist
     * @param name
     * @return
     */
    public MyTableView getTableByName(String name) {
        return displayTables.stream()
                .filter(t -> Objects.equals(t.getName(), name))
                .findFirst().get();
    }
}
