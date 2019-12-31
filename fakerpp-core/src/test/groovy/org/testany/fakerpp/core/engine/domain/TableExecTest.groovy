package org.testany.fakerpp.core.engine.domain

import org.testany.fakerpp.core.ERMLException
import org.testany.fakerpp.core.engine.generator.joins.LeftJoinGen
import org.testany.fakerpp.core.engine.generator.joins.RightJoinGen
import org.testany.fakerpp.core.parser.ast.Table
import spock.lang.Specification

class TableExecTest extends Specification {

    def mockTable(...mockCols) {
        def mockTable = Mock(TableExec)
        mockCols.each {
            col ->
                mockTable.containsCol(col) >> true
                mockTable.column(col) >> new ColExec(col)
        }

        return mockTable
    }

    def mockTableWithName(name, ...mockCols) {
        def mockTable = mockTable(mockCols)
        mockTable.getName() >> name
        return mockTable
    }

    def "left join other tables"() {
        given:
        def joinInfos = [
                new TableExec.JoinInfo(
                        new Table.Join(["id":"user_id"],
                                "user",
                                false),
                        mockTable("id")
                ),
                new TableExec.JoinInfo(
                        new Table.Join(["id":"shop_id", "name":"shop_name"],
                                "user",
                                false),
                        mockTable("id", "name")
                )
        ]
        def originTable = new TableExec(
                "a",-1, null, [], [], [:], [:]
        )

        when:
        originTable.leftJoin(joinInfos)

        then:
        originTable.getColFamilies().size() == 2
        originTable.getCriticalColFamilies().size() == 0
        with(originTable.getColFamilies()[0]) {
            cols*.name == ["user_id"]
            generator instanceof LeftJoinGen
            generator.dependColExecs*.name == ["id"]
        }
        with(originTable.getColFamilies()[1]) {
            cols*.name == ["shop_id", "shop_name"]
            generator instanceof LeftJoinGen
            generator.dependColExecs*.name == ["id", "name"]
        }
    }

    def "right join other tables"() {
        given:
        def joinInfos = [
                new TableExec.JoinInfo(
                        new Table.Join(["id":"user_id"],
                                "user",
                                false),
                        mockTable("id")
                ),
                new TableExec.JoinInfo(
                        new Table.Join(["id":"shop_id", "name":"shop_name"],
                                "user",
                                true),
                        mockTable("id", "name")
                ),
                new TableExec.JoinInfo(
                        new Table.Join(["rid":"room_id"],
                                "room",
                                true),
                        mockTable("rid")
                )
        ]
        def originTable = new TableExec(
                "a",-1, null, [], [], [:], [:]
        )

        when:
        originTable.rightJoin(joinInfos)

        then:
        originTable.getColFamilies().size() == 0
        originTable.getCriticalColFamilies().size() == 1
        with(originTable.getCriticalColFamilies()[0]) {
            cols*.name == ["shop_id", "shop_name", "room_id", "user_id"]
            generator instanceof RightJoinGen
            generator.fixedDimension*.cols*.name == [["id"]]
            generator.randomDimension*.cols*.name == [["id", "name"], ["rid"]]
        }
    }

    def "join non-existent col"() {
        given:
        def joinInfos = [
                new TableExec.JoinInfo(
                        new Table.Join(["id":"user_id"],
                                "user",
                                false),
                        mockTableWithName("tableUser","uid")
                ),
        ]
        def originTable = new TableExec(
                "a",-1, null, [], [], [:], [:]
        )

        when:
        originTable.leftJoin(joinInfos)

        then:
        ERMLException e = thrown()
        e.getMessage() == "error in join, field 'id' not in table 'tableUser'"
    }

}
