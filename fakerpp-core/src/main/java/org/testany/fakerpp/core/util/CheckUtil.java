package org.testany.fakerpp.core.util;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.domain.ColExec;

import java.util.List;

public class CheckUtil {

    public static void checkColsSizeIdentity(List<ColExec> cols) throws ERMLException {
        if (cols == null || cols.size() == 0) {
            throw new ERMLException("Rightjoin Dimension can not be empty");
        }

        int dataNum = cols.get(0).size();
        for (int i = 1; i < cols.size(); i++) {
            if (cols.get(i).size() != dataNum) {
                throw new ERMLException(
                        String.format("Right join Dimension %s data size not identity, perhaps storer " +
                                "not feed back exludes cols correctly", cols)
                );
            }
        }
    }

}
