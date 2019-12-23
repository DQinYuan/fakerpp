package org.testany.fakerpp.core.engine.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.parser.ast.DataSourceInfo;
import org.testany.fakerpp.core.util.ExceptionConsumer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class TableExec {

    private final String name;
    private final int num;

    // null in virtual table
    private final DataSourceInfo dataSourceInfo;
    // column families can define record number
    private final List<ColFamilyExec> criticalColFamilies;
    // normal column failies
    private final List<ColFamilyExec> colFamilies;
    // columns without excludes
    private final LinkedHashMap<String, ColExec> columns;
    // exclude columns
    private final Map<String, ColExec> excludes;


    public List<String> columns() {
        return null;
    }

    public void forEach(ExceptionConsumer<List<String>, ERMLException> consumer) throws ERMLException {
    }

    public void addCriticalColFamilies(ColFamilyExec colFamilyExec) {
        criticalColFamilies.add(colFamilyExec);
    }

    public void addNormalColFamilies(ColFamilyExec colFamilyExec) {
        colFamilies.add(colFamilyExec);
    }

    public boolean containsCol(String colName) {
        return columns.containsKey(colName) || excludes.containsKey(colName);
    }

    public ColExec column(String colName) {
        return columns.getOrDefault(colName, excludes.get(colName));
    }

    public void addColumn(ColExec colExec) {
        columns.put(colExec.getName(), colExec);
    }

}
