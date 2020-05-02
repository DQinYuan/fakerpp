package org.testd.fakerpp.core.store.storers.mocks;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.fakerpp.core.parser.ast.Table;
import org.testd.fakerpp.core.store.storers.Storer;
import org.testd.fakerpp.core.store.storers.TableStorer;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
public class DiscardStorer implements Storer {

    private boolean inited = false;

    @Override
    public void init(DataSourceInfo dsi) throws ERMLException {
        inited = true;
    }

    @Override
    public TableStorer getTableStorer(String tableName, List<String> colNames) {
        return null;
    }

    @Override
    public Set<Table> reverse() throws ERMLException {
        return ImmutableSet.of();
    }
}
