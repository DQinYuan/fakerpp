package org.testany.fakerpp.core.engine.generator.builtin;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.Generator;

import java.util.List;

public class ConstGen implements Generator {

    public List<List<String>> options;
    private int cursor;


    @Override
    public void init() throws ERMLException {
        cursor = 0;
    }

    @Override
    public List<String> nextData() throws ERMLException {
        return options.get(cursor++);
    }

    @Override
    public long dataNum() {
        return options.size();
    }
}
