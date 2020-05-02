package org.testd.fakerpp.core.store.storers;

import org.testd.fakerpp.core.ERMLException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BatchableTableStorer implements TableStorer {

    private final List<List<String>> batch;
    protected final List<String> cols;
    private final int batchSize;

    public BatchableTableStorer(int batchSize, List<String> cols) {
        this.batchSize = batchSize;
        this.cols = cols;
        this.batch = new ArrayList<>(batchSize);
    }

    @Override
    public void store(List<String> records) throws ERMLException {
        batch.add(records);
        if (batch.size() == batchSize) {
            commit();
        }
    }

    @Override
    public void flush() throws ERMLException {
        if (batch.size() != 0) {
            commit();
        }
    }

    @Override
    public abstract Map<String, List<String>> feedBackData(List<String> excludes) throws ERMLException;

    private void checkBatch(int colNum) throws ERMLException {
        for (List<String> row : batch) {
            if (row.size() != colNum) {
                throw new ERMLException(
                        String.format(
                                "batch rows with different col number, %s",
                                batch.stream()
                                        .map(List::size)
                                        .map(Object::toString)
                                        .collect(Collectors.joining(",", "[", "]"))
                        )
                );
            }
        }
    }

    private void commit() throws ERMLException {
        int colNum = cols.size();
        checkBatch(colNum);
        executeCommit(batch);
        batch.clear();
    }

    protected abstract void executeCommit(List<List<String>> batch) throws ERMLException;
}
