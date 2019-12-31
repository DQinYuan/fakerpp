package org.testany.fakerpp.core.engine.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ColExec {

    private final String name;
    private final List<String> data;

    public ColExec(String name) {
        this.name = name;
        data = new ArrayList<>();
    }

    public int size() {
        return data.size();
    }
}
