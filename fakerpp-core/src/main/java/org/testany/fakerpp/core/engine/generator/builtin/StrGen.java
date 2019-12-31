package org.testany.fakerpp.core.engine.generator.builtin;

import org.apache.commons.lang3.RandomStringUtils;
import org.testany.fakerpp.core.engine.generator.Generator;

import java.util.Arrays;
import java.util.List;

public class StrGen implements Generator {

    public String prefix;
    public String suffix;
    public int    len;

    @Override
    public void init(){
    }

    @Override
    public List<String> nextData() {
        String rStr = RandomStringUtils.randomAlphanumeric(len);
        return Arrays.asList(prefix + rStr + suffix);
    }

    @Override
    public long dataNum() {
        return 0;
    }

}
