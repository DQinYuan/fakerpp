package org.testany.fakerpp.core.engine.generator.joins

import org.testany.fakerpp.core.engine.domain.ColExec

class Tools {

    static ColExec getColExec(String name, List<String> data) {
        def colExec = new ColExec(name)
        colExec.data.addAll(data)
        return colExec
    }
}
