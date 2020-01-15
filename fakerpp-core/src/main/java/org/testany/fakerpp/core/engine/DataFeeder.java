package org.testany.fakerpp.core.engine;

import java.util.List;

public interface DataFeeder {
    String getName();
    void add(String newData);
    void addAll(List<String> datas);
}
