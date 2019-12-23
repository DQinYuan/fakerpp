package org.testany.fakerpp.core.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.ERMLEngine;
import org.testany.fakerpp.core.parser.ast.ERML;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testany.fakerpp.core.util.ExceptionConsumer.sneaky;

@Slf4j
@Component
@RequiredArgsConstructor
public class ERMLParser {

    @Resource
    private final ERMLEngine ermlEngine;

    public ERML parseDir(Path path) throws ERMLException {
        FileProcessor processor = new FileProcessor();
        try {
            Files.list(path)
                    .forEach(sneaky(processor::process));
        } catch (IOException e) {
            throw new ERMLException("can not access path" + path, e);
        }
        return processor.getErml();
    }

    public void exec(Path path) throws ERMLException {
        ERML erml = parseDir(path);
        ermlEngine.exec(erml);
    }

}
