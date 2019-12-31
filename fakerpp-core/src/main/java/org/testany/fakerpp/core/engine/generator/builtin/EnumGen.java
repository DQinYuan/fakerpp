package org.testany.fakerpp.core.engine.generator.builtin;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.Generator;
import org.testany.fakerpp.core.util.SeedableThreadLocalRandom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnumGen implements Generator {

    public List<String> options = new ArrayList<>();

    @Override
    public void init() throws ERMLException {
        if (options == null || options.isEmpty()) {
            throw new ERMLException("<enum> sub tag <options> can not be empty");
        }
    }

    @Override
    public List<String> nextData() {
        return Arrays.asList(
                options.get(SeedableThreadLocalRandom.nextInt(options.size()))
        );
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
