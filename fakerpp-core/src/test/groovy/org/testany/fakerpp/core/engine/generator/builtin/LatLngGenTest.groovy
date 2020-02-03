package org.testany.fakerpp.core.engine.generator.builtin

import spock.lang.Specification

class LatLngGenTest extends Specification {

    def "test lat lng generator"() {
        expect:
        def latLngGenerator = new LatLngGen()
        latLngGenerator.init(2)
        5.times {
            def data = latLngGenerator.nextData()
            assert data.get(0).toDouble() >= -90
            assert data.get(0).toDouble() <= 90
            assert data.get(1).toDouble() >= -180
            assert data.get(1).toDouble() <= 180
        }
    }

}
