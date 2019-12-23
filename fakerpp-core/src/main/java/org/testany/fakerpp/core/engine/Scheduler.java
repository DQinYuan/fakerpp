package org.testany.fakerpp.core.engine;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.domain.TableExec;
import org.testany.fakerpp.core.util.ExceptionConsumer;

import java.util.Map;
import java.util.function.Function;

public class Scheduler {

    public Scheduler(Map<String, TableExec> tableExecMap) {

    }


    public void forEach(ExceptionConsumer<TableExec, ERMLException> consumer) throws ERMLException {
    }

}
