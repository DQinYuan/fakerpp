package org.testany.fakerpp.core.engine.domain;

import lombok.Getter;
import org.testany.fakerpp.core.engine.DataFeeder;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ColExec implements DataFeeder {

    private final String name;
    private final List<String> data;

    public ColExec(String name) {
        this.name = name;
        data = new ArrayList<>();
    }

    public int size() {
        return data.size();
    }

    public void add(String newData) {
        data.add(newData);
    }

    @Override
    public void addAll(List<String> datas) {
        data.addAll(datas);
    }

    @Override
    public String toString() {
        return "ColExec{" +
                "name='" + name + '\'' +
                ",data size:" + data.size() +
                '}';
    }
}
