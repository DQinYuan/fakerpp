package org.testany.fakerpp.core.engine.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ColExec {

    @Getter
    private final String name;
    private final List<String> data;

    public ColExec(String name) {
        this.name = name;
        data = new ArrayList<>();
    }
}
