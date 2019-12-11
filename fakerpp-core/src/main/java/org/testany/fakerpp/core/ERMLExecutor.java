package org.testany.fakerpp.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@RequiredArgsConstructor
public class ERMLExecutor {

    public void diskExec(Path path) throws ERMLException {
        System.out.println(path);
    }

}
