package org.testany.fakerpp.core.engine;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.parser.ast.DataSourceInfo;
import org.testany.fakerpp.core.util.ExceptionConsumer;

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
