package org.testany.fakerpp.core.engine.generator.builtin;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.Generator;
import org.testany.fakerpp.core.util.SeedableThreadLocalRandom;

import java.util.ArrayList;
import java.util.List;

public class EnumGen implements Generator {

    public List<List<String>> options = new ArrayList<>();

    @Override
    public void init(int colNum) throws ERMLException {
    }

    @Override
    public List<String> nextData() {
        return options.get(SeedableThreadLocalRandom.nextInt(options.size()));
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
