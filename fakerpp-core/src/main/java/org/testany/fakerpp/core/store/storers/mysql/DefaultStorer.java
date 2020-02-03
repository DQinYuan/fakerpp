package org.testany.fakerpp.core.store.storers.mysql;

import lombok.extern.slf4j.Slf4j;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.parser.ast.DataSourceInfo;
import org.testany.fakerpp.core.store.storers.BatchableTableStorer;
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

    private class InternalTableStorer extends BatchableTableStorer {

        private final String name;
        private final String prepareSQL;
        private int colNum;
        private final String selectTemplate;
        private final Function<Integer, String> prepareBySize;

        public InternalTableStorer(String name, List<String> cols) {
            super(batchSize, cols);
            this.name = name;
            this.prepareSQL = MyStringUtil.prepareInsertSQL(name, cols, batchSize);
            this.prepareBySize = size -> MyStringUtil.prepareInsertSQL(name, cols, size);
            this.selectTemplate = "SELECT %s FROM " + name;
            this.colNum = cols.size();
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

        @Override
        protected void executeCommit(List<List<String>> batch) throws ERMLException {
            String pSql = batch.size() == batchSize ? prepareSQL : prepareBySize.apply(batch.size());
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
    }
}
