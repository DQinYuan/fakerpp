package org.testd.fakerpp.core.store.storers.mysql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.parser.ast.*;
import org.testd.fakerpp.core.store.storers.BatchableTableStorer;
import org.testd.fakerpp.core.store.storers.DataSources;
import org.testd.fakerpp.core.store.storers.Storer;
import org.testd.fakerpp.core.store.storers.TableStorer;
import org.testd.fakerpp.core.util.MyStringUtil;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.store.storers.BatchableTableStorer;
import org.testd.fakerpp.core.store.storers.DataSources;
import org.testd.fakerpp.core.store.storers.TableStorer;
import org.testd.fakerpp.core.util.ExceptionConsumer;
import org.testd.fakerpp.core.util.MyStringUtil;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.testd.fakerpp.core.util.ExceptionConsumer.sneakyConsumer;

@Slf4j
public class DefaultStorer implements Storer {

    private DataSourceInfo dataSourceInfo;
    private DataSource dataSource;
    private int batchSize;

    @Override
    public void init(DataSourceInfo dsi) throws ERMLException {
        this.dataSourceInfo = dsi;
        this.dataSource = DataSources.getDataSource(dsi.getUrl(),
                dsi.getUser(), dsi.getPasswd());
        this.batchSize = dsi.getBatchSize();

    }

    @Override
    public TableStorer getTableStorer(String tableName, List<String> colNames) {
        return new InternalTableStorer(tableName, colNames);
    }

    @Override
    public Set<Table> reverse() throws ERMLException {
        ImmutableSet.Builder<Table> tableSetBuilder = ImmutableSet.builder();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData dbMeta = conn.getMetaData();
            List<String> tableNames = getTableNames(conn.getCatalog(), dbMeta);
            for (String tableName : tableNames) {
                Pair<List<Table.ColFamily>, List<String>> cfAndExcludes =
                        getColFamilies(dbMeta, conn.getCatalog(), tableName);
                tableSetBuilder.add(
                        new Table(tableName,
                                dataSourceInfo.getName(),
                                100,
                                Table.Joins.emptyJoins,
                                cfAndExcludes.getValue0(),
                                cfAndExcludes.getValue1()
                        )
                );
            }

            return tableSetBuilder.build();
        } catch (SQLException e) {
            throw new ERMLException(e);
        }
    }

    /**
     *
     * @param dbMeta
     * @param dbName
     * @param tableName
     * @return {@code Pair<colFamilies, excludes>}
     * @throws ERMLException
     */
    private Pair<List<Table.ColFamily>, List<String>> getColFamilies(DatabaseMetaData dbMeta,
                                                      String dbName,
                                                      String tableName) throws ERMLException {
        Set<String> primaryKeys;
        try (ResultSet primaryKeyRes = dbMeta.getPrimaryKeys(dbName, null, tableName)) {
            ImmutableSet.Builder<String> setBuilder = ImmutableSet.builder();
            while (primaryKeyRes.next()) {
                setBuilder.add(primaryKeyRes.getString("COLUMN_NAME"));
            }
            primaryKeys = setBuilder.build();
        } catch (SQLException e) {
            throw new ERMLException(e);
        }

        try (ResultSet columns = dbMeta.getColumns(dbName, null,
                tableName, null)) {
            ImmutableList.Builder<Table.ColFamily> colFamiliesBuilder =
                    ImmutableList.builder();
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                if (!primaryKeys.contains(columnName)) {
                    colFamiliesBuilder.add(
                            new Table.ColFamily(
                                    Collections.singletonList(columnName),
                                    Collections.singletonList(
                                            TypeDefaultGenerator.defaultGenByType(columnType))
                            )
                    );
                }
            }

            return new Pair<>(colFamiliesBuilder.build(),
                    ImmutableList.copyOf(primaryKeys));
        } catch (SQLException e) {
            throw new ERMLException(e);
        }
    }

    private List<String> getTableNames(String dbName, DatabaseMetaData dbMeta) throws ERMLException {
        try (ResultSet tables = dbMeta.getTables(dbName, null,
                null, new String[]{"TABLE"})) {
            ImmutableList.Builder<String> tableNames = ImmutableList.builder();
            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME"));
            }

            return tableNames.build();
        } catch (SQLException e) {
            throw new ERMLException(e);
        }
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
                    String.join(",", excludes)
            );
            Map<String, List<String>> dataMap = excludes.stream()
                    .collect(Collectors.toMap(Function.identity(),
                            ignore -> new ArrayList<>()));
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(selectCmd);
                while (rs.next()) {
                    excludes.forEach(
                            ExceptionConsumer.sneakyConsumer(
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
                        String cellVal = row.get(j);
                        if ("true".equals(cellVal) || "false".equals(cellVal)) {
                            pStmt.setBoolean(base + j + 1,
                                    Boolean.parseBoolean(cellVal));
                            continue;
                        }
                        pStmt.setString(base + j + 1, cellVal);
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
