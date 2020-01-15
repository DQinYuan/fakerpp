package org.testany.fakerpp.core.engine;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.util.ExceptionConsumer;

public interface Scheduler {

    void forEach(ExceptionConsumer<TableIter, ERMLException> consumer)
            throws ERMLException;

}
