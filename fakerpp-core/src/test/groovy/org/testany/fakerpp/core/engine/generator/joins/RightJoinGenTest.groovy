package org.testany.fakerpp.core.engine.generator.joins

import org.testany.fakerpp.core.ERMLException
import org.testany.fakerpp.core.util.SeedableThreadLocalRandom
import spock.lang.Specification

import static org.testany.fakerpp.core.engine.generator.joins.Tools.getColExec

class RightJoinGenTest extends Specification {

    private RightJoinGen build(builder) {
        def gen = builder.build()
        gen.init()
        return gen
    }

    def expectSeq(gen, expSeq, expCount) {
        def count = 0
        def data
        while ((data = gen.nextData()) != null) {
            assert data == expSeq[count]
            count++
        }
        assert count == expCount
        return true
    }

    def addDimension(builder, prefix, cols, random) {
        def joinCols = []
        def dependCols = []
        char base = 'a'
        cols.size.times {
            i ->
                char curChar = base + i
                joinCols << getColExec("my_${prefix}_${curChar}", [])
                dependCols << getColExec("${prefix}_${curChar}", cols[i])
        }
        builder.appendDimension(new JoinDepend(joinCols, dependCols), random)
    }

    def "right join with one fixed col family"() {
        given:
        def rowData = [
                ["a1", "b1"],
                ["a2", "b2"],
        ]
        def colData = GroovyCollections.transpose(rowData)
        def builder = RightJoinGen.builder()

        when:
        addDimension(builder, "shop", colData, false)

        then:
        5.times {
            def rightJoinGen = build(builder)
            rightJoinGen.init()
            assert rightJoinGen.colOrder()*.name == ["shop_a", "shop_b"]
            assert rightJoinGen.dataNum() == 2
            expectSeq(rightJoinGen, rowData, 2)
        }
    }

    def "right join with one random col family"() {
        given:
        def rowData = [
                ["a1", "b1"],
                ["a2", "b2"],
                ["a3", "b3"]
        ]
        def colData = GroovyCollections.transpose(rowData)
        def builder = RightJoinGen.builder()


        when:
        addDimension(builder, "shop", colData, true)
        SeedableThreadLocalRandom.setSeed(0)
        // number in `data` field represent row number in rowData (to slice it)
        def seedExpect = [
                [count: 2, data: [0, 1]],
                [count: 2, data: [1, 2]],
                [count: 1, data: [2]]
        ]

        then:
        seedExpect.size().times { i ->
            def rightJoinGen = build(builder)
            def expect = seedExpect[i]
            expectSeq(rightJoinGen, rowData[expect.data], expect.count)
            assert rightJoinGen.dataNum() == expect.count
        }
    }

    def simpleCartesian(cf1, cf2) {
        return GroovyCollections.combinations(cf2, cf1).each {
            row ->
                def temp = row[0]
                row[0] = row[1]
                row[1] = temp
        }*.flatten()
    }

    def "right join with multi fixed column"() {
        given:
        def fixedCf1 = [
                ["a1", "b1", "c1"],
                ["a2", "b2", "c2"]
        ]
        def fixedCf2 = [
                ["d1", "e1"],
                ["d2", "e2"],
        ]

        def cf1Cols = GroovyCollections.transpose(fixedCf1)
        def cf2Cols = GroovyCollections.transpose(fixedCf2)
        def builder = RightJoinGen.builder()

        when:
        addDimension(builder, "shop", cf1Cols, false)
        addDimension(builder, "user", cf2Cols, false)

        then:
        def expect = [
                ["a1", "b1", "c1", "d1", "e1"],
                ["a1", "b1", "c1", "d2", "e2"],
                ["a2", "b2", "c2", "d1", "e1"],
                ["a2", "b2", "c2", "d2", "e2"]
        ]
        def rightJoinGen = build(builder)
        rightJoinGen.dataNum() == 4
        expectSeq(rightJoinGen, expect, 4)
    }

    def "right join with random and fixed column"() {
        given:
        def fixedCf1 = [
                ["a1", "b1", "c1"],
                ["a2", "b2", "c2"]
        ]
        def fixedCf2 = [
                ["d1"],
                ["d2"],
        ]
        def randomCf1 = [
                ["f1"],
                ["f2"],
                ["f3"]
        ]
        def randomCf2 = [
                ["g1", "h1"],
                ["g2", "h2"],
                ["g3", "h3"],
                ["g4", "h4"]
        ]
        def fixedCf1Cols = GroovyCollections.transpose(fixedCf1)
        def fixedCf2Cols = GroovyCollections.transpose(fixedCf2)
        def randomCf1Cols = GroovyCollections.transpose(randomCf1)
        def randomCf2Cols = GroovyCollections.transpose(randomCf2)
        def builder = RightJoinGen.builder()

        when:
        addDimension(builder, "shop", fixedCf1Cols, false)
        addDimension(builder, "dt", fixedCf2Cols, false)
        addDimension(builder, "user", randomCf1Cols, true)
        addDimension(builder, "addr", randomCf2Cols, true)

        then:
        def exp = [["f3", "g4", "h4", "a1", "b1", "c1", "d1"],
                   ["f1", "g4", "h4", "a1", "b1", "c1", "d2"],
                   ["f2", "g4", "h4", "a1", "b1", "c1", "d2"],
                   ["f1", "g2", "h2", "a2", "b2", "c2", "d1"],
                   ["f2", "g2", "h2", "a2", "b2", "c2", "d1"],
                   ["f2", "g4", "h4", "a2", "b2", "c2", "d2"]]
        SeedableThreadLocalRandom.setSeed(4)
        def rightJoinGen = build(builder)
        expectSeq(rightJoinGen, exp, 6)
    }

    def "invalid dimension"() {
        given:
        def rowData =
                [["a1", "b1"],
                 ["a2"]]
        def builder = RightJoinGen.builder()

        when:
        addDimension(builder, "invalid", [["a1", "a2"], ["b1"]], false)
        def gen = build(builder)
        gen.nextData()

        then:
        RuntimeException e = thrown()
        e.getCause() instanceof ERMLException
    }

}
