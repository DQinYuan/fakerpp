package org.testd.fakerpp.core.store.storers;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.fakerpp.core.parser.ast.Table;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * it's implementation must be thread-safe
 */
public interface Storer {

    void init(DataSourceInfo dsi) throws ERMLException;

    TableStorer getTableStorer(String tableName, List<String> colNames) throws ERMLException;

    /**
     * reverse default ERML from datasource
     * @return
     * @throws ERMLException
     */
    Set<Table> reverse() throws ERMLException;

}
