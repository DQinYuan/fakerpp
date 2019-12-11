package org.testany.fakerpp.core;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;


public class ERMLExecutorTest {

    private ERMLExecutor executor;

    @Before
    public void before() {
        executor = new ERMLExecutor();
    }

    @Test
    @Ignore
    public void testDiskExec() throws Exception {
        executor.diskExec(Paths.get("aaaa"));
    }

}