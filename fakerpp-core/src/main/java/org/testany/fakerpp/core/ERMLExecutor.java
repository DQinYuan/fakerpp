package org.testany.fakerpp.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testany.fakerpp.core.parser.ERMLParser;
import org.testany.fakerpp.core.parser.ast.ERML;
import org.testany.fakerpp.core.parser.ast.Meta;
import org.testany.fakerpp.core.parser.ast.Table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ERMLExecutor {

    private final ERMLParser ermlParser;

    public void diskExec(Path path) throws ERMLException {
        ermlParser.exec(path);
    }

}
