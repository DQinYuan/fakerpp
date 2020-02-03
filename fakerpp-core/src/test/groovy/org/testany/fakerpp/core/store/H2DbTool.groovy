package org.testany.fakerpp.core.store

import javax.sql.RowSet
import javax.sql.rowset.RowSetProvider
import java.sql.DriverManager

class H2DbTool {

    static void printRowSet(RowSet rs) {
        println(rowSetToString(rs))
    }

    static String rowSetToString(RowSet rs) {
        StringBuilder sb = new StringBuilder()
        sb.append('\n')
        def count = rs.getMetaData().getColumnCount()
        count.times {
            i ->
                sb.append(sprintf("%13s", rs.getMetaData().getColumnName(i + 1)))
        }
        sb.append('\n')
        while (rs.next()) {
            count.times {
                i ->
                    sb.append(sprintf("%13s", rs.getString(i + 1)))
            }
            sb.append('\n')
        }
        rs.beforeFirst()
        return sb.toString()
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
