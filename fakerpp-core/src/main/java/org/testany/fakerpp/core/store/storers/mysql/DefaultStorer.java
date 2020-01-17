package org.testany.fakerpp.core.store.storers.mysql;

import lombok.extern.slf4j.Slf4j;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.parser.ast.DataSourceInfo;
import org.testany.fakerpp.core.store.storers.DataSources;
import org.testany.fakerpp.core.store.storers.Storer;
import org.testany.fakerpp.core.store.storers.TableStorer;
import org.testany.fakerpp.core.util.MyStringUtil;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.testany.fakerpp.core.util.ExceptionConsumer.sneakyConsumer;

@Slf4j
public class DefaultStorer implements Storer {

    private DataSource dataSource;
    private int batchSize;

    @Override
    public void init(DataSourceInfo dsi, int batchSize) throws ERMLException {
        this.dataSource = DataSources.getDataSource(dsi.getUrl(),
                dsi.getUser(), dsi.getPasswd());
        this.batchSize = batchSize;

    }

    @Override
    public TableStorer getTableStorer(String tableName, List<String> colNames) {
        return new InternalTableStorer(tableName, colNames);
    }

    private class InternalTableStorer implements TableStorer {

        private final String name;
        private final List<List<String>> batch;
        private final List<String> cols;
        private final String prepareSQL;
        private final String selectTemplate;

        public InternalTableStorer(String name, List<String> cols) {
            this.name = name;
            this.batch = new ArrayList<>(batchSize);
            this.cols = cols;
            this.prepareSQL = MyStringUtil.prepareInsertSQL(name, cols, batchSize);
            this.selectTemplate = "SELECT %s FROM " + name;
        }

        @Override
        public void store(List<String> records) throws ERMLException {
            batch.add(records);
            if (batch.size() == batchSize) {
                commit();
            }
        }

        private void commit() throws ERMLException {
            int colNum = cols.size();
            checkBatch(colNum);
            executePrepareSql(prepareSQL);
            batch.clear();
        }

        private void executePrepareSql(String pSql) throws ERMLException {
            int colNum = cols.size();
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pStmt = conn.prepareStatement(pSql)) {
                for (int i = 0; i < batch.size(); i++) {
                    int base = colNum * i;
                    List<String> row = batch.get(i);
                    for (int j = 0; j < colNum; j++) {
                        pStmt.setString(base + j + 1, row.get(j));
                    }
                }
                int updateNum = pStmt.executeUpdate();
                if (updateNum != batchSize) {
                    log.warn("batch update warning, batch size: {}, real update:{}",
                            batchSize, updateNum);
                }
            } catch (SQLException e) {
                throw new ERMLException(e);
            }
        }

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

        @Override
        public void flush() throws ERMLException {
            if (batch.size() == batchSize) {
                commit();
            } else {
                String flushInsert = MyStringUtil.prepareInsertSQL(name, cols, batch.size());
                executePrepareSql(flushInsert);
                batch.clear();
            }
        }

        @Override
        public Map<String, List<String>> feedBackData(List<String> excludes)
                throws ERMLException {
            String selectCmd = String.format(selectTemplate,
                    excludes.stream().collect(Collectors.joining(","))
            );
            Map<String, List<String>> dataMap = excludes.stream()
                    .collect(Collectors.toMap(Function.identity(),
                            ignore -> new ArrayList<>()));
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(selectCmd);
                while (rs.next()) {
                    excludes.forEach(
                            sneakyConsumer(
                                    excColName -> dataMap.get(excColName).add(rs.getString(excColName))
                            )
                    );
                }
                return dataMap;
            } catch (SQLException e) {
                throw new ERMLException(e);
            }
        }
    }
}
