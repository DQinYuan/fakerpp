package org.testany.fakerpp.core.store

import javax.sql.RowSet
import javax.sql.rowset.RowSetProvider
import java.sql.DriverManager

class H2DbTool {

    static printRowSet(RowSet rs) {
        def count = rs.getMetaData().getColumnCount()
        count.times {
            i ->
                printf("%5s", rs.getMetaData().getColumnName(i + 1))
        }
        println()
        while (rs.next()) {
            count.times {
                i ->
                    printf("%5s", rs.getString(i + 1))
            }
            println()
        }
        rs.beforeFirst()
    }

    static RowSet execSqlInH2(String db, String sql) {
        def h2Conn = DriverManager.getConnection("jdbc:h2:mem:${db};DB_CLOSE_DELAY=-1")
        def stmt = h2Conn.createStatement()
        def cachedRowSet = RowSetProvider.newFactory().createCachedRowSet()
        stmt.execute(sql)
        if (stmt.getResultSet() != null) {
            cachedRowSet.populate(stmt.getResultSet())
        }
        h2Conn.close()
        stmt.close()
        return cachedRowSet
    }

    static expectDbData(String dbName, String tableName, List head, List<List> expectData) {
        def rowSet = execSqlInH2(dbName, "select ${head.join(",")} from ${tableName}")
        int rowNum = 0
        while (rowSet.next()) {
            head.eachWithIndex { entry, i ->
                assert rowSet.getString(entry) == expectData[rowNum][i]
            }
            rowNum++
        }
        return true
    }
}
