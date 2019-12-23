package org.testany.fakerpp.core.engine.domain;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.Generator;

import java.util.List;

public class ColFamilyExec {

    private final List<ColExec> cols;

    // generator
    private final Generator generator;

    public ColFamilyExec(List<ColExec> cols, Generator generator) throws ERMLException {
        this.cols = cols;
        this.generator = generator;
        generator.init();
    }

}
