package org.testd.fakerpp.core.store.storers.csv

import com.google.common.collect.ImmutableSet
import org.testd.fakerpp.core.ERMLException
import org.testd.fakerpp.core.parser.ast.DataSourceInfo
import org.testd.fakerpp.core.parser.ast.Table
import org.testd.fakerpp.core.store.storers.Storer
import org.testd.fakerpp.core.store.storers.TableStorer

class NotExistStorer implements Storer {
    @Override
    void init(DataSourceInfo dsi) throws ERMLException {

    }

    @Override
    TableStorer getTableStorer(String tableName, List<String> colNames) throws ERMLException {
        return null
    }

    @Override
    Set<Table> reverse() throws ERMLException {
        return ImmutableSet.of()
    }
}
