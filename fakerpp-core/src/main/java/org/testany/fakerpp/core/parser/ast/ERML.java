package org.testany.fakerpp.core.parser.ast;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ERML {

    private Meta meta;
    private Map<String, Table> tables;

    public ERML() {
        tables = new HashMap<>();
    }

    /**
     *
     * @param table
     * @return if table duplicate
     */
    public boolean appendTable(Table table) {
        Table oldValue = tables.put(table.getName(), table);
        return oldValue == null;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
