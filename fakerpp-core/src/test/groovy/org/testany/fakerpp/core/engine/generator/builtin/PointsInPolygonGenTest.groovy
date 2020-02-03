package org.testany.fakerpp.core.engine.generator.builtin

import spock.lang.Specification

class PointsInPolygonGenTest extends Specification {

    def "generate points in polygon"(int colNum) {
        given:
        def generator = new PointsInPolygonGen()

        when:
        generator.input = "wkt"
        // 45 angle square
        generator.polygon = "-1 1,1 1,1 -1,-1 -1,-1 1"
        generator.init(colNum)

        then:
        5.times {
            def points = generator.nextData()
            assert points.size() == colNum
            if (colNum == 2) {
                assert points.get(0).toDouble() >= -1
                assert points.get(0).toDouble() <= 1
                assert points.get(1).toDouble() >= -1
                assert points.get(1).toDouble() <= 1
            }
        }

        where:
        colNum | _
        1      | _
        2      | _
        3      | _
    }

}
