package org.testd.fakerpp.core.store.storers.mysql

import org.testd.fakerpp.core.parser.ast.DataSourceInfo
import org.testd.fakerpp.core.parser.ast.Table
import spock.lang.Specification

import static org.testd.fakerpp.core.store.H2DbTool.execSqlInH2
import static org.testd.fakerpp.core.store.H2DbTool.expectDbData

class DefaultStorerTest extends Specification {

    def "test mysql default store"() {
        given:
        def dbName = "DefaultStorerTest.TestMysqlDefaultStore"
        def tableName = "test"
        def dsi = new DataSourceInfo(
                "mysql0",
                "mysql",
                "default",
                3,
                "jdbc:h2:mem:${dbName}",
                "",
                ""
        )

        // prepare table
        execSqlInH2(dbName, """ 
             create table  ${ tableName} (
               id int auto_increment primary key, a varchar(255), b int)
             """)

        when:
        def dStorer = new DefaultStorer()
        dStorer.init(dsi)
        def tableStorer = dStorer.getTableStorer(tableName, ["a", "b"])
        5.times {
            i ->
                tableStorer.store(["hi ${i}".toString(), i.toString()])
        }

        then:
        expectDbData(dbName, tableName, ["id"], [["1"],
                                                 ["2"],
                                                 ["3"]])
        tableStorer.flush()
        expectDbData(dbName, tableName, ["id", "a", "b"], [["1", "hi 0", "0"],
                                                           ["2", "hi 1", "1"],
                                                           ["3", "hi 2", "2"],
                                                           ["4", "hi 3", "3"],
                                                           ["5", "hi 4", "4"]])
        def data = tableStorer.feedBackData(["id"])
        data["id"] == ["1","2","3","4","5"]
    }

    def "test mysql reverse"() {
        given:
        def dbName = "DefaultStorerTest.testMysqlReverse"
        def tableName = "test"
        def dsi = new DataSourceInfo(
                "mysql0",
                "mysql",
                "default",
                3,
                "jdbc:h2:mem:${dbName}",
                "",
                ""
        )

        // prepare table
        execSqlInH2(dbName, """ 
             create table  ${ tableName} (
               id int auto_increment primary key, a varchar(255), b int)
             """)

        when:
        def dStorer = new DefaultStorer()
        dStorer.init(dsi)
        def tables = dStorer.reverse()

        then:
        tables.size() == 1
        tables[0] == new Table("test", "mysql0", 100, Table.Joins.emptyJoins,
                          [new Table.ColFamily(["a"],
                                  [Table.GeneratorInfo.emptyBuiltInInfo("str")]),
                           new Table.ColFamily(["b"],
                                  [Table.GeneratorInfo.emptyBuiltInInfo("int")])],
                            ["id"])
    }

}
