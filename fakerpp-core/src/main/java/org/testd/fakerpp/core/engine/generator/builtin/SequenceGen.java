package org.testd.fakerpp.core.engine.generator.builtin;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SequenceGen implements Generator {

    private int sequence = 0;

    @Override
    public void init(int colNum) throws ERMLException {
        sequence = 0;
    }

    @Override
    public List<String> nextData() throws ERMLException {
        return Collections.singletonList(String.valueOf(sequence++));
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
