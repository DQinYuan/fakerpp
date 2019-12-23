package org.testany.fakerpp.core.engine.generator.joins;

import lombok.RequiredArgsConstructor;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.domain.ColExec;
import org.testany.fakerpp.core.engine.generator.Generator;

import java.util.List;

@RequiredArgsConstructor
public class LeftJoinGen implements Generator {

    private final List<ColExec> dependColExecs;

    @Override
    public void init() throws ERMLException {
    }

    @Override
    public String nextData() {
        return null;
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
