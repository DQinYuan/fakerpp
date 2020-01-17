package org.testany.fakerpp.core.store.storers.mysql

import org.testany.fakerpp.core.parser.ast.DataSourceInfo
import spock.lang.Specification

import static org.testany.fakerpp.core.store.H2DbTool.*

class DefaultStorerTest extends Specification {

    def "test default store"() {
        given:
        def dbName = "DefaultStorerTest"
        def tableName = "test"
        def dsi = new DataSourceInfo(
                "mysql0",
                "mysql",
                "default",
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
        dStorer.init(dsi, 3)
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

}
