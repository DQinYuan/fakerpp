package org.testany.fakerpp.core.engine.generator.builtin;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.Generator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnumGen implements Generator {

    public List<String> options = new ArrayList<>();

    @Override
    public void init() throws ERMLException {
        if (options == null || options.isEmpty()) {
            throw new ERMLException("<enum> sub tag <options> can not be empty");
        }
    }

    @Override
    public String nextData() {
        return options.get(ThreadLocalRandom.current().nextInt(options.size()));
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
