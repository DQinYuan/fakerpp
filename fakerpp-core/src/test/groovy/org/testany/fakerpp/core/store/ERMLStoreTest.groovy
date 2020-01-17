package org.testany.fakerpp.core.store

import org.testany.fakerpp.core.ERMLException
import org.testany.fakerpp.core.engine.DataFeeder
import org.testany.fakerpp.core.engine.TableIter
import org.testany.fakerpp.core.parser.ast.DataSourceInfo
import org.testany.fakerpp.core.store.storers.StorersTest
import org.testany.fakerpp.core.util.ExceptionConsumer
import spock.lang.Specification

import static org.testany.fakerpp.core.store.H2DbTool.*

class ERMLStoreTest extends Specification {

    def "test store table"() {
        given:
        def dbName = "ERMLStoreTest"
        def tableName = "storeTableTest"
        ERMLStore storeLayer = new ERMLStore(StorersTest.getStorers())
        def dataToInsert = [["a1", "b1", "c1"],
                            ["a2", "b2", "c2"],
                            ["a3", "b3", "c3"]]
        def feedData = []
        def mockTableIter = [
                name:{-> tableName},
                columns:{-> ["aa", "bb", "cc"]},
                exludes:{-> ["id"]},
                dataSourceInfo:{->
                    Optional.of(
                            new DataSourceInfo(
                                    "mysql0",
                                    "mysql",
                                    "default",
                                    "jdbc:h2:mem:${dbName}",
                                    "",
                                    ""
                            )
                    )
                },
                forEach: {ExceptionConsumer<List<String>, ERMLException> consumer ->
                    dataToInsert.each {row -> consumer.accept(row)}
                },
                feedEachExclude:{
                    ExceptionConsumer<DataFeeder, ERMLException> feedConsumer ->
                        feedConsumer.accept([getName:{-> "id"},
                                             add:{newData -> feedData.add(newData)},
                                             addAll:{datas -> feedData.addAll(datas)}] as DataFeeder)
                },
                recordNum:{-> 3}
        ] as TableIter

        // prepare table
        execSqlInH2(dbName, """ 
             create table  ${tableName} (
               id int auto_increment primary key, aa varchar(255), bb varchar(255), cc varchar(255))
             """)

        when:
        storeLayer.storeTable(mockTableIter)

        then:
        feedData == ["1", "2", "3"]
        expectDbData(dbName, tableName, ["aa", "bb", "cc"], dataToInsert)
    }

}
