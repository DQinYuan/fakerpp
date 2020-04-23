package org.testd.fakerpp.core;

import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.engine.ERMLEngine;
import org.testd.fakerpp.core.parser.ERMLParser;
import org.testd.fakerpp.core.parser.ast.ERML;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;
import org.testd.fakerpp.core.parser.ERMLParser;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ERMLExecutor {

    private final ERMLParser ermlParser;

    private final ERMLEngine ermlEngine;

    public void diskExec(Path path) throws ERMLException {
        ermlParser.execDisk(path);
    }

    public void memoryExec(InputStream metaStream, List<InputStream> tableStreams) throws ERMLException {
        ermlParser.execMemory(metaStream, tableStreams);
    }

    public void memoryExec(ERML erml) throws ERMLException {
        ermlEngine.exec(erml);
    }

    @VisibleForTesting
    @Cacheable("testCache")
    public int testCache(int i, String s, Class c) {
        return SeedableThreadLocalRandom.nextInt(10);
    }

}
