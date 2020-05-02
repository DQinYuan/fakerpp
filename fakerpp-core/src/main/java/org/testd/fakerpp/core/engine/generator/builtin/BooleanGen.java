package org.testd.fakerpp.core.engine.generator.builtin;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;

import java.util.Collections;
import java.util.List;

public class BooleanGen implements Generator {
    @Override
    public void init(int colNum) throws ERMLException {
    }

    @Override
    public List<String> nextData() throws ERMLException {
        return Collections.singletonList(
                SeedableThreadLocalRandom.nextBoolean().toString()
        );
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
