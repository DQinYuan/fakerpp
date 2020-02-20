package org.testd.fakerpp.core.engine;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.util.ExceptionConsumer;
import org.testd.fakerpp.core.util.ExceptionConsumer;

public interface Scheduler {

    void forEach(ExceptionConsumer<TableIter, ERMLException> consumer)
            throws ERMLException;

}
