package org.testany.fakerpp.core.engine.generator;

import org.testany.fakerpp.core.ERMLException;

import java.util.List;

public interface Generator {

    /**
     *
     * @throws ERMLException if params is illegal
     */
    void init() throws ERMLException;

    /**
     *
     * @return generated data. null means it can not generate data any more
     */
    List<String> nextData();

    /**
     * number of data it can generate
     * @return if return -1, it can generate unlimited number
     */
    long dataNum();

}
