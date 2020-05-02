package org.testd.fakerpp.core.engine.generator.builtin;

import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultNumber;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultString;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultString;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StrGen implements Generator {

    @DefaultString("")
    public String prefix = "";
    @DefaultString("")
    public String suffix = "";
    @DefaultNumber(10)
    public int    len = 10;

    @Override
    public void init(int colNum){
    }

    @Override
    public List<String> nextData() {
        String rStr = SeedableThreadLocalRandom.randomAlphanumeric(len);
        return Collections.singletonList(prefix
                + rStr
                + suffix);
    }

    @Override
    public long dataNum() {
        return 0;
    }

}
