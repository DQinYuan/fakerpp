package org.testd.fakerpp.core.engine.generator.joins

import org.testd.fakerpp.core.ERMLException

import static Tools.getColExec
import org.testd.fakerpp.core.engine.generator.Generator
import spock.lang.Specification

class LeftJoinGenTest extends Specification {

    def "random rows in depend cols"() {
        given:
        def rowData = [
                ["a1", "b1", "c1", "d1"],
                ["a2", "b2", "c2", "d2"],
                ["a3", "b3", "c3", "d3"],
                ["a4", "b4", "c4", "d4"],
                ["a5", "b5", "c5", "d5"],
        ]
        def colData = GroovyCollections.transpose(rowData)
        def depenColExecs = [
                getColExec("a", colData[0]),
                getColExec("b", colData[1]),
                getColExec("c", colData[2]),
                getColExec("d", colData[3]),
        ]

        when:
        org.testd.fakerpp.core.engine.generator.Generator leftGen = new LeftJoinGen(depenColExecs)
        leftGen.init(0)

        then:
        leftGen.dataNum() == rowData.size()
        5.times {
            assert rowData.contains(leftGen.nextData())
        }
    }

    def "invalid depend cols"() {
        given:
        def depenColExecs = [
                getColExec("a", ["a1", "a2"]),
                getColExec("b", ["b1"])
        ]

        when:
        def gen = new LeftJoinGen(depenColExecs)
        gen.nextData()

        then:
        RuntimeException e = thrown()
        e.getCause() instanceof ERMLException
    }

}
