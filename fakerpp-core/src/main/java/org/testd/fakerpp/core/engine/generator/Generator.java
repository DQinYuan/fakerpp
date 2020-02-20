package org.testd.fakerpp.core.engine.generator;

import org.testd.fakerpp.core.ERMLException;

import java.util.List;

public interface Generator {

    /**
     *
     * @throws ERMLException if params is illegal
     * @param colNum the col number of the col family it will try it's best to generate
     */
    void init(int colNum) throws ERMLException;

    /**
     *
     * @return generated data. null means it can not generate data any more
     */
    List<String> nextData() throws ERMLException;

    /**
     * number of data it can generate
     * @return if return 0, it can generate unlimited number
     */
    long dataNum();

}
