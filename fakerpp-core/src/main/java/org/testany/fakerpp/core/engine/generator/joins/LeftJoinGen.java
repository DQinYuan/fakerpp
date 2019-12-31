package org.testany.fakerpp.core.engine.generator.joins;

import lombok.RequiredArgsConstructor;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.domain.ColExec;
import org.testany.fakerpp.core.engine.generator.Generator;
import org.testany.fakerpp.core.util.SeedableThreadLocalRandom;

import java.util.List;

@RequiredArgsConstructor
public class LeftJoinGen implements Generator {

    // NonEmpty
    private final List<ColExec> dependColExecs;
    private RowList rowList;

    @Override
    public void init() throws ERMLException {
        rowList = new RowList(dependColExecs);
    }

    @Override
    public List<String> nextData() {
        return rowList.get(
                SeedableThreadLocalRandom.nextInt(rowList.size())
        );
    }

    @Override
    public long dataNum() {
        return rowList.size();
    }
}
