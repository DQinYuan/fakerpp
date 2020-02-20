package org.testd.fakerpp.core.store.storers.mocks;

import lombok.Getter;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.store.storers.Storer;
import org.testd.fakerpp.core.store.storers.TableStorer;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;

import java.util.List;

@Getter
public class DiscardStorer implements Storer {

    private boolean inited = false;

    @Override
    public void init(DataSourceInfo dsi, int batchSize) throws ERMLException {
        inited = true;
    }

    @Override
    public TableStorer getTableStorer(String tableName, List<String> colNames) {
        return null;
    }
}
