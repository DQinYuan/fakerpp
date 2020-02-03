package org.testany.fakerpp.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testany.fakerpp.core.parser.ERMLParser;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ERMLExecutor {

    private final ERMLParser ermlParser;

    public void diskExec(Path path) throws ERMLException {
        ermlParser.execDisk(path);
    }

    public void memoryExec(InputStream metaStream, List<InputStream> tableStreams) throws ERMLException {
        ermlParser.execMemory(metaStream, tableStreams);
    }

}
