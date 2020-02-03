package org.testany.fakerpp.core.engine.generator.builtin;

import org.testany.fakerpp.core.engine.generator.Generator;
import org.testany.fakerpp.core.util.SeedableThreadLocalRandom;

import java.util.Arrays;
import java.util.List;

public class StrGen implements Generator {

    public String prefix = "";
    public String suffix = "";
    public int    len;

    @Override
    public void init(int colNum){
    }

    @Override
    public List<String> nextData() {
        String rStr = SeedableThreadLocalRandom.randomAlphanumeric(len);
        return Arrays.asList(prefix
                + rStr
                + suffix);
    }

    @Override
    public long dataNum() {
        return 0;
    }

}
