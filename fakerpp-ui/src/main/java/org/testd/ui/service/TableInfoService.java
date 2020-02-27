package org.testd.ui.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Service;
import org.testd.ui.view.dynamic.MyTableView;

@Service
public class TableInfoService {

    private ObservableList<MyTableView> displayTables = FXCollections.observableArrayList();

    public void addTable(MyTableView tableView) {
        displayTables.add(tableView);
    }

    public void deleteTable(MyTableView tableView) {
        displayTables.remove(tableView);
    }

}
