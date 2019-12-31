package org.testany.fakerpp.core.engine.generator.joins;

import lombok.RequiredArgsConstructor;
import org.testany.fakerpp.core.engine.domain.ColExec;

import java.util.AbstractList;
import java.util.List;
import java.util.stream.Collectors;
@RequiredArgsConstructor
public class RowList extends AbstractList<List<String>> {
    private final List<ColExec> dependColExecs;
    private List<String>[] cache = null;

    public int size() {
        return dependColExecs.get(0).size();
    }

    public List<String> get(int index) {
        if (index >= size()) {
            throw new IndexOutOfBoundsException(
                    String.format("out of row list size, Index: %d, Size: %d", index, size())
            );
        }

        if (cache == null) {
            cache = new List[size()];
        }

        if (cache[index] != null) {
            return cache[index];
        }

        List<String> row = dependColExecs
                .stream().map(ColExec::getData)
                .map(l -> l.get(index))
                .collect(Collectors.toList());
        cache[index] = row;
        return row;
    }
}
