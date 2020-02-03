package org.testany.fakerpp.core.engine.generator.builtin;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.Generator;
import org.testany.fakerpp.core.util.SeedableThreadLocalRandom;

import java.util.Arrays;
import java.util.List;

public class IntGen implements Generator {

    public int min = 0;   // inclusive
    public int max = 100;  // exclusive

    @Override
    public void init(int colNum) throws ERMLException {
        if (max <= min) {
            throw new ERMLException("<int min='...' max='...'> max must be larger than min");
        }
    }

    @Override
    public List<String> nextData() {
        return Arrays.asList(String.valueOf(SeedableThreadLocalRandom.nextInt(min, max)));
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
