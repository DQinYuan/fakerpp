package org.testany.fakerpp.core.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class MyListUtil {

    /**
     * transpose a list with out copy
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> transposeView(List<List<T>> table){
        List<List<T>> transposed = new ArrayList<>();
        for ( int i = 0; i < table.get(0).size(); i++ ){
            transposed.add(new ViewList<>(table, i));
        }

        return transposed;
    }

    private static class ViewList<T> extends AbstractList<T> {
        //转置前的表
        private List<List<T>> table;

        private int rowNum;

        public ViewList(List<List<T>> table, int rowNum) {
            this.table = table;
            this.rowNum = rowNum;
        }

        @Override
        public T get(int index) {
            return table.get(index).get(rowNum);
        }

        @Override
        public int size() {
            return table.size();
        }
    }

}
