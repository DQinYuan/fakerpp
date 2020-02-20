package org.testd.fakerpp.core.engine;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.util.ExceptionConsumer;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.util.ExceptionConsumer;

import java.util.List;
import java.util.Optional;

public interface TableIter {
    String name();

    /**
     *
     * @return columns without excludes
     */
    List<String> columns();
    /**
     * all exclude fields
     * @return
     */
    List<String> exludes();

    void forEach(ExceptionConsumer<List<String>,
                ERMLException> consumer) throws ERMLException;
    Optional<DataSourceInfo> dataSourceInfo();
    void feedEachExclude(ExceptionConsumer<DataFeeder, ERMLException> feedConsumer)
            throws ERMLException;



    /**
     * record number of this table
     * @return
     */
    long recordNum();
}
