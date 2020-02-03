package org.testany.fakerpp.core.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.ERMLEngine;
import org.testany.fakerpp.core.parser.ast.ERML;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.testany.fakerpp.core.util.ExceptionConsumer.sneakyConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class ERMLParser {

    @Resource
    private final ERMLEngine ermlEngine;

    public ERML parseDir(Path path) throws ERMLException {
        FileParser processor = new FileParser();
        try {
            Files.list(path)
                    .forEach(sneakyConsumer(processor::processByDir));
        } catch (IOException e) {
            throw new ERMLException("can not access path" + path, e);
        }

        return processor.getERML();
    }

    public void execDisk(Path path) throws ERMLException {
        ERML erml = parseDir(path);
        ermlEngine.exec(erml);
    }

    public void execMemory(InputStream metaStream, List<InputStream> tables) throws ERMLException {
        FileParser fileParser = new FileParser();
        fileParser.processByStream(metaStream, tables);
        ermlEngine.exec(fileParser.getERML());
    }

}
