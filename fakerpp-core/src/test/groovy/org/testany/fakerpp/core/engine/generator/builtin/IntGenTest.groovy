package org.testany.fakerpp.core.engine.generator.builtin

import spock.lang.Specification

class IntGenTest extends Specification {

    def "generate int in [min, max)"() {
        given:
        def min = 101
        def max = 107

        when:
        def intGen = new IntGen(min: min, max: max)

        then:
        5.times {
            def data = intGen.nextData()[0].toInteger()
            assert data >= min
            assert data < max
        }
    }

}
