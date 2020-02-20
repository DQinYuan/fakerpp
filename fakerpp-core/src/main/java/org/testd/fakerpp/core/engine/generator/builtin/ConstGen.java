package org.testd.fakerpp.core.engine.generator.builtin;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.Generator;

import java.util.List;

public class ConstGen implements Generator {

    public List<List<String>> options;
    private int cursor;


    @Override
    public void init(int colNum) throws ERMLException {
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
