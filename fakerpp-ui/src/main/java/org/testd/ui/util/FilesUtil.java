package org.testd.ui.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesUtil {

    public static void createDirectoriesIfNotExist(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
