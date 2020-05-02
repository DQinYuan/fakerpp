package org.testd.fakerpp.core.engine.generator.builtin;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultNumber;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;

import java.util.Collections;
import java.util.List;

public class IntGen implements Generator {

    @DefaultNumber(0)
    public int min = 0;   // inclusive
    @DefaultNumber(100)
    public int max = 100;  // exclusive

    @Override
    public void init(int colNum) throws ERMLException {
        if (max <= min) {
            throw new ERMLException("<int min='...' max='...'> max must be larger than min");
        }
    }

    @Override
    public List<String> nextData() {
        return Collections.singletonList(String.valueOf(SeedableThreadLocalRandom.nextInt(min, max)));
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
