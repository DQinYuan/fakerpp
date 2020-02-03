package org.testany.fakerpp.core.engine.generator.joins

import spock.lang.Specification

import static org.testany.fakerpp.core.engine.generator.joins.Tools.getColExec

class RowListTest extends Specification {

    def "test cache"() {
        given:
        def rowData = [
                ["a1", "b1", "c1", "d1"],
                ["a2", "b2", "c2", "d2"],
                ["a3", "b3", "c3", "d3"],
                ["a4", "b4", "c4", "d4"],
                ["a5", "b5", "c5", "d5"],
        ]
        def colData = GroovyCollections.transpose(rowData)
        def colExecs = [
                getColExec("a", colData[0]),
                getColExec("b", colData[1]),
                getColExec("c", colData[2]),
                getColExec("d", colData[3]),
        ]

        when:
        def rl = new RowList(colExecs)

        then:
        rowData.eachWithIndex { List<String> entry, int i ->
            assert entry == rl[i]
        }
    }

}
