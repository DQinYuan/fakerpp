package org.testany.fakerpp.core.engine.generator.builtin;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.Generator;

import java.util.concurrent.ThreadLocalRandom;

public class IntGen implements Generator {

    public int min = 0;   // inclusive
    public int max = 100;  // exclusive

    @Override
    public void init() throws ERMLException {
        if (max <= min) {
            throw new ERMLException("<int min='...' max='...'> max must be larger than min");
        }
    }

    @Override
    public String nextData() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(min, max));
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
