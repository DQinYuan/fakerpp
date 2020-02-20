package org.testd.fakerpp.core.store.storers;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;

import java.util.List;

/**
 * it's implementation must be thread-safe
 */
public interface Storer {

    void init(DataSourceInfo dsi, int batchSize) throws ERMLException;

    TableStorer getTableStorer(String tableName, List<String> colNames) throws ERMLException;

}
