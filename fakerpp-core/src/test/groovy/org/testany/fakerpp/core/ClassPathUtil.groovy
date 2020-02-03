package org.testany.fakerpp.core

import java.nio.file.Path
import java.nio.file.Paths

class ClassPathUtil {

    static Path relative(Class klass, String path, String... paths){
        Path classPath = null
        try {
            classPath = Paths.get(klass
                    .getClassLoader().getResource("").toURI())
        } catch (URISyntaxException e) {
            throw new RuntimeException(e)
        }
        return classPath.resolve(Paths.get(path, paths))
    }

}
