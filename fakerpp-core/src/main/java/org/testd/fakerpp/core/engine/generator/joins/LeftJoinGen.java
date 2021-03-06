package org.testd.fakerpp.core.engine.generator.joins;

import lombok.RequiredArgsConstructor;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.domain.ColExec;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.util.CheckUtil;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;
import org.testd.fakerpp.core.util.CheckUtil;

import java.util.List;

@RequiredArgsConstructor
public class LeftJoinGen implements Generator {

    // NonEmpty
    private final List<ColExec> dependColExecs;
    private RowList rowList;

    @Override
    public void init(int colNum) throws ERMLException {
        rowList = new RowList(dependColExecs);
    }

    private boolean checked = false;

    private void checkData() {
        if (!checked) {
            try {
                CheckUtil.checkColsSizeIdentity(dependColExecs);
            } catch (ERMLException e) {
                throw new RuntimeException(e);
            }
            checked = true;
        }
    }

    @Override
    public List<String> nextData() {
        checkData();
        return rowList.get(
                SeedableThreadLocalRandom.nextInt(rowList.size())
        );
    }

    @Override
    public long dataNum() {
        checkData();
        return rowList.size();
    }
}
