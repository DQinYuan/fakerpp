package org.testd.fakerpp.core.engine.generator.joins;

import org.testd.fakerpp.core.engine.domain.ColExec;
import org.testd.fakerpp.core.engine.domain.ColExec;

import java.util.AbstractList;
import java.util.List;

/**
 * transpose a col list to a row list
 * size() and get(index) must be lazy(can not be determined in constructor)
 */
public class RowList extends AbstractList<List<String>> {
    private final List<ColExec> dependColExecs;

    public RowList(List<ColExec> dependColExecs)  {
        this.dependColExecs = dependColExecs;
    }

    public int size() {
        return dependColExecs.get(0).size();
    }

    public List<String> get(int index) {
        return new ViewList(dependColExecs, index);
    }

    private static class ViewList extends AbstractList<String>{
        // table before transpose
        private List<ColExec> table;

        private int rowNum;

        public ViewList(List<ColExec> table, int rowNum) {
            this.table = table;
            this.rowNum = rowNum;
        }

        @Override
        public String get(int index) {
            return table.get(index).getData().get(rowNum);
        }

        @Override
        public int size() {
            return table.size();
        }
    }
}
