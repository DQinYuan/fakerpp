package org.testd.fakerpp.core.store.storers.csv;

import lombok.extern.slf4j.Slf4j;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.store.storers.BatchableTableStorer;
import org.testd.fakerpp.core.store.storers.Storer;
import org.testd.fakerpp.core.store.storers.TableStorer;
import org.testd.fakerpp.core.store.storers.BatchableTableStorer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class DefaultStorer implements Storer {

    private int batchSize;
    private Path dataDir;

    @Override
    public void init(DataSourceInfo dsi) throws ERMLException {
        this.batchSize = dsi.getBatchSize();
        try {
            dataDir = Paths.get(dsi.getName());
            if (!Files.exists(dataDir)) {
                Files.createDirectory(dataDir);
            }
        } catch (IOException e) {
            throw new ERMLException(
                    String.format("can not open csv file %s",
                            dataDir, e));
        }
    }

    @Override
    public TableStorer getTableStorer(String tableName, List<String> colNames) throws ERMLException {
        return new InternalStorer(dataDir.resolve(tableName + ".csv"),
                batchSize, colNames);
    }

    private class InternalStorer extends BatchableTableStorer {

        private FileWriter csvFileWriter;
        private Path path;

        public InternalStorer(Path path, int batchSize, List<String> cols) throws ERMLException {
            super(batchSize, cols);
            this.path = path;
            try {
                csvFileWriter = new FileWriter(path.toFile());
                csvFileWriter.write(cols.stream().collect(Collectors.joining(",")) + "\n");
            } catch (FileNotFoundException e) {
                throw new ERMLException(
                        String.format("csv file %s open fail", path.toAbsolutePath()),
                        e);
            } catch (IOException e) {
                throw new ERMLException(
                        String.format("csv file %s write fail", path.toAbsolutePath()),
                        e);
            }
        }

        @Override
        public Map<String, List<String>> feedBackData(List<String> excludes) throws ERMLException {
            return new HashMap<>();
        }

        @Override
        protected void executeCommit(List<List<String>> batch) throws ERMLException {
            String batchContent = batch.stream()
                    .map(l -> l.stream().collect(Collectors.joining(",")))
                    .collect(Collectors.joining("\n"));
            try {
                csvFileWriter.write(batchContent + "\n");
            } catch (IOException e) {
                log.warn("csv file {} write fail", path.toAbsolutePath());
            }
        }

        @Override
        public void flush() throws ERMLException {
            super.flush();
            try {
                csvFileWriter.close();
            } catch (IOException e) {
                log.warn("csv file {} close fail", path.toAbsolutePath());
            }
        }
    }
}
