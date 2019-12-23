package org.testany.fakerpp.core.engine.generator;

import org.testany.fakerpp.core.ERMLException;

public interface Generator {

    void init() throws ERMLException;

    /**
     *
     * @return generated data. null means it can not generate data any more
     */
    String nextData();

    /**
     * number of data it can generate
     * @return if return -1, it can generate unlimited number
     */
    long dataNum();

}
